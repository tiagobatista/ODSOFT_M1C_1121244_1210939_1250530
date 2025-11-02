package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper.BookRedisMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementação Redis pura do BookRepository
 * Esta classe apenas interage com Redis, não com SQL
 *
 * Estratégia de keys:
 * - book:{pk} → Hash com dados do Book
 * - book:isbn:{isbn} → String com pk do Book (índice)
 * - book:title:{title} → Set com pks (pode haver vários livros com título similar)
 */
@Repository("redisBookRepository")
@RequiredArgsConstructor
public class RedisBookRepositoryImpl implements BookRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BookRedisMapper mapper;

    private static final String KEY_PREFIX = "book:";
    private static final String KEY_BY_ISBN = "book:isbn:";
    private static final String KEY_BY_TITLE = "book:title:";

    // TTL de 2 horas (muda mais que Genre, menos que Lending)
    private static final Duration TTL = Duration.ofHours(2);

    /**
     * Busca Book por ISBN
     * ISBN é único, então busca direta
     */
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return Optional.empty();
        }

        // Buscar pk pelo índice de ISBN
        String isbnKey = KEY_BY_ISBN + isbn.toLowerCase();
        String pk = (String) redisTemplate.opsForValue().get(isbnKey);

        if (pk == null) {
            return Optional.empty();
        }

        // Buscar Book pelo pk
        String key = KEY_PREFIX + pk;
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

        if (hash.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(mapper.fromRedisHash(hash));
    }

    /**
     * Busca Books por Title
     * Pode retornar múltiplos livros
     */
    @Override
    public List<Book> findByTitle(String title) {
        if (title == null || title.isBlank()) {
            return Collections.emptyList();
        }

        // Buscar pks pelo índice de título
        String titleKey = KEY_BY_TITLE + title.toLowerCase();
        Set<Object> pks = redisTemplate.opsForSet().members(titleKey);

        if (pks == null || pks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Book> books = new ArrayList<>();
        for (Object pk : pks) {
            String key = KEY_PREFIX + pk.toString();
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

            if (!hash.isEmpty()) {
                Book book = mapper.fromRedisHash(hash);
                if (book != null) {
                    books.add(book);
                }
            }
        }

        return books;
    }

    /**
     * Salva Book no Redis
     */
    @Override
    public Book save(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        // Se não tem pk, não podemos salvar no cache
        if (book.getPk() == null) {
            return book;
        }

        String key = KEY_PREFIX + book.getPk();
        Map<String, String> hash = mapper.toRedisHash(book);

        // Salvar como hash
        redisTemplate.opsForHash().putAll(key, hash);
        redisTemplate.expire(key, TTL.getSeconds(), TimeUnit.SECONDS);

        // Criar índice por ISBN (único)
        if (book.getIsbn() != null) {
            String isbnKey = KEY_BY_ISBN + book.getIsbn().toString().toLowerCase();
            redisTemplate.opsForValue().set(isbnKey, book.getPk().toString(), TTL);
        }

        // Criar índice por Title (pode haver múltiplos)
        if (book.getTitle() != null) {
            String titleKey = KEY_BY_TITLE + book.getTitle().toString().toLowerCase();
            redisTemplate.opsForSet().add(titleKey, book.getPk().toString());
            redisTemplate.expire(titleKey, TTL.getSeconds(), TimeUnit.SECONDS);
        }

        // Atualizar índices secundários (genre, authors)
        updateSecondaryIndexes(book);

        return book;
    }

    /**
     * Delete Book do Redis
     */
    @Override
    public void delete(Book book) {
        if (book == null || book.getPk() == null) {
            return;
        }

        String key = KEY_PREFIX + book.getPk();

        // Deletar índice por ISBN
        if (book.getIsbn() != null) {
            String isbnKey = KEY_BY_ISBN + book.getIsbn().toString().toLowerCase();
            redisTemplate.delete(isbnKey);
        }

        // Deletar do índice por Title
        if (book.getTitle() != null) {
            String titleKey = KEY_BY_TITLE + book.getTitle().toString().toLowerCase();
            redisTemplate.opsForSet().remove(titleKey, book.getPk().toString());
        }

        // Remover índices secundários (genre, authors)
        removeSecondaryIndexes(book);

        // Deletar o hash principal
        redisTemplate.delete(key);
    }

    /**
     * Busca Books por Genre
     * Resultado cacheado como lista de PKs
     */
    @Override
    public List<Book> findByGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            return Collections.emptyList();
        }

        String genreKey = "book:genre:" + genre.toLowerCase();

        // Buscar lista de PKs do Redis
        Set<Object> pks = redisTemplate.opsForSet().members(genreKey);

        if (pks == null || pks.isEmpty()) {
            return Collections.emptyList();
        }

        // Buscar cada Book pelo PK
        List<Book> books = new ArrayList<>();
        for (Object pk : pks) {
            String key = KEY_PREFIX + pk.toString();
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

            if (!hash.isEmpty()) {
                Book book = mapper.fromRedisHash(hash);
                if (book != null) {
                    books.add(book);
                }
            }
        }

        return books;
    }

    /**
     * Busca Books por Author Name
     * Resultado cacheado como lista de PKs
     */
    @Override
    public List<Book> findByAuthorName(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            return Collections.emptyList();
        }

        String authorKey = "book:author:" + authorName.toLowerCase();

        // Buscar lista de PKs do Redis
        Set<Object> pks = redisTemplate.opsForSet().members(authorKey);

        if (pks == null || pks.isEmpty()) {
            return Collections.emptyList();
        }

        // Buscar cada Book pelo PK
        List<Book> books = new ArrayList<>();
        for (Object pk : pks) {
            String key = KEY_PREFIX + pk.toString();
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

            if (!hash.isEmpty()) {
                Book book = mapper.fromRedisHash(hash);
                if (book != null) {
                    books.add(book);
                }
            }
        }

        return books;
    }

    /**
     * Busca Books por Author Number
     * Resultado cacheado como lista de PKs
     */
    @Override
    public List<Book> findBooksByAuthorNumber(Long authorNumber) {
        if (authorNumber == null) {
            return Collections.emptyList();
        }

        String authorKey = "book:author:number:" + authorNumber;

        // Buscar lista de PKs do Redis
        Set<Object> pks = redisTemplate.opsForSet().members(authorKey);

        if (pks == null || pks.isEmpty()) {
            return Collections.emptyList();
        }

        // Buscar cada Book pelo PK
        List<Book> books = new ArrayList<>();
        for (Object pk : pks) {
            String key = KEY_PREFIX + pk.toString();
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

            if (!hash.isEmpty()) {
                Book book = mapper.fromRedisHash(hash);
                if (book != null) {
                    books.add(book);
                }
            }
        }

        return books;
    }

    /**
     * Top 5 Books Lent - Cache do RESULTADO da agregação
     *
     * ATENÇÃO: Guarda o RESULTADO (lista de DTOs), não os Books
     * TTL mais curto (1 hora) porque muda com lendings
     */
    @Override
    public Page<BookCountDTO> findTop5BooksLent(LocalDate oneYearAgo, Pageable pageable) {
        // Para Top5, o resultado da agregação seria guardado como JSON
        // Mas BookCountDTO não é um Book, é um DTO diferente
        // Melhor cachear no BookCacheRepository usando String serializada
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    /**
     * SearchBooks - NÃO suportado (muitas combinações possíveis)
     *
     * Queries com múltiplos filtros dinâmicos não são eficientes em cache
     * porque geram combinações exponenciais de keys
     */
    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query) {
        // Pesquisa complexa não suportada no Redis cache
        return Collections.emptyList();
    }

    // ==================== Métodos auxiliares para índices ====================

    /**
     * Atualiza índices secundários quando salva Book
     * Chamado internamente pelo save()
     */
    private void updateSecondaryIndexes(Book book) {
        if (book == null || book.getPk() == null) {
            return;
        }

        String pkStr = book.getPk().toString();

        // Índice por Genre
        if (book.getGenre() != null && book.getGenre().getGenre() != null) {
            String genreKey = "book:genre:" + book.getGenre().getGenre().toLowerCase();
            redisTemplate.opsForSet().add(genreKey, pkStr);
            redisTemplate.expire(genreKey, TTL.getSeconds(), TimeUnit.SECONDS);
        }

        // Índice por Authors (cada author)
        if (book.getAuthors() != null) {
            for (Author author : book.getAuthors()) {
                // Por nome
                if (author.getName() != null) {
                    String authorNameKey = "book:author:" + author.getName().toString().toLowerCase();
                    redisTemplate.opsForSet().add(authorNameKey, pkStr);
                    redisTemplate.expire(authorNameKey, TTL.getSeconds(), TimeUnit.SECONDS);
                }

                // Por número
                if (author.getAuthorNumber() != null) {
                    String authorNumberKey = "book:author:number:" + author.getAuthorNumber();
                    redisTemplate.opsForSet().add(authorNumberKey, pkStr);
                    redisTemplate.expire(authorNumberKey, TTL.getSeconds(), TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * Remove índices secundários quando deleta Book
     * Chamado internamente pelo delete()
     */
    private void removeSecondaryIndexes(Book book) {
        if (book == null || book.getPk() == null) {
            return;
        }

        String pkStr = book.getPk().toString();

        // Remover do índice por Genre
        if (book.getGenre() != null && book.getGenre().getGenre() != null) {
            String genreKey = "book:genre:" + book.getGenre().getGenre().toLowerCase();
            redisTemplate.opsForSet().remove(genreKey, pkStr);
        }

        // Remover dos índices de Authors
        if (book.getAuthors() != null) {
            for (Author author : book.getAuthors()) {
                // Por nome
                if (author.getName() != null) {
                    String authorNameKey = "book:author:" + author.getName().toString().toLowerCase();
                    redisTemplate.opsForSet().remove(authorNameKey, pkStr);
                }

                // Por número
                if (author.getAuthorNumber() != null) {
                    String authorNumberKey = "book:author:number:" + author.getAuthorNumber();
                    redisTemplate.opsForSet().remove(authorNumberKey, pkStr);
                }
            }
        }
    }
}