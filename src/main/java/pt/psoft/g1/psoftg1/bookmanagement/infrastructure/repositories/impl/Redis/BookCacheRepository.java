package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do BookRepository usando padrão Cache-Aside
 *
 * Esta classe coordena entre Redis (cache) e SQL (source of truth):
 * - Leituras: tenta Redis primeiro, se não encontrar busca no SQL e atualiza cache
 * - Escritas: escreve no SQL e atualiza/invalida cache
 *
 * ESTRATÉGIA para Book:
 * - findByIsbn(): CACHEADO (consulta muito comum!)
 * - findByTitle(): CACHEADO (consulta comum)
 * - findByGenre(): SQL direto (query dinâmica)
 * - findByAuthorName(): SQL direto (join complexo)
 * - searchBooks(): SQL direto (query complexa com múltiplos filtros)
 * - findTop5BooksLent(): SQL direto (agregação)
 *
 * Fluxo de leitura (Cache-Aside):
 * 1. Verificar Redis (cache)
 * 2. Se encontrado (cache hit): retornar imediatamente
 * 3. Se não encontrado (cache miss): buscar no SQL
 * 4. Guardar no Redis para próximas consultas
 * 5. Retornar resultado
 *
 * Fluxo de escrita (Write-Through):
 * 1. Escrever no SQL (source of truth)
 * 2. Atualizar/invalidar cache no Redis
 * 3. Retornar resultado
 */
@Profile("sql-redis")
@Primary
@Repository("bookCacheRepository")
@ConditionalOnProperty(name = "repository.cache.enabled", havingValue = "true", matchIfMissing = true)
public class BookCacheRepository implements BookRepository {

    private static final Logger logger = LoggerFactory.getLogger(BookCacheRepository.class);

    private final BookRepository cacheRepository;  // Redis
    private final BookRepository sourceRepository; // SQL

    /**
     * Constructor com @Qualifier explícito em CADA parâmetro
     */
    public BookCacheRepository(
            @Qualifier("redisBookRepository") BookRepository cacheRepository,
            @Qualifier("bookRepositoryImpl") BookRepository sourceRepository) {
        this.cacheRepository = cacheRepository;
        this.sourceRepository = sourceRepository;
    }

    /**
     * FindByIsbn com Cache-Aside pattern
     * ISBN é único e consulta MUITO comum → CACHEAR!
     */
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        logger.debug("Finding book by ISBN: {}", isbn);

        // 1. Tentar buscar do cache (Redis)
        Optional<Book> cached = cacheRepository.findByIsbn(isbn);

        if (cached.isPresent()) {
            logger.info("🎯 CACHE HIT - Book ISBN: {}", isbn);
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Book ISBN: {}", isbn);
        Optional<Book> book = sourceRepository.findByIsbn(isbn);

        // 3. Se encontrou, guardar no cache para próximas consultas
        if (book.isPresent()) {
            try {
                cacheRepository.save(book.get());
                logger.info("💾 Saved to Redis cache - Book ISBN: {}", isbn);
            } catch (Exception e) {
                logger.warn("Failed to cache book {}: {}", isbn, e.getMessage());
                // Não falha a operação se o cache falhar
            }
        }

        return book;
    }

    /**
     * FindByTitle com Cache-Aside pattern
     * Consulta comum → CACHEAR!
     */
    @Override
    public List<Book> findByTitle(String title) {
        logger.debug("Finding books by title: {}", title);

        // 1. Tentar buscar do cache (Redis)
        List<Book> cached = cacheRepository.findByTitle(title);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Book title: {} ({} books)", title, cached.size());
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Book title: {}", title);
        List<Book> books = sourceRepository.findByTitle(title);

        // 3. Cachear cada book encontrado
        if (!books.isEmpty()) {
            logger.info("💾 Caching {} books to Redis", books.size());
            books.forEach(book -> {
                try {
                    cacheRepository.save(book);
                } catch (Exception e) {
                    logger.warn("Failed to cache book {}: {}", book.getPk(), e.getMessage());
                }
            });
        }

        return books;
    }

    /**
     * Save com Write-Through pattern
     */
    @Override
    public Book save(Book book) {
        String identifier = book.getIsbn() != null ? book.getIsbn().toString() : String.valueOf(book.getPk());
        logger.debug("Saving book: {}", identifier);

        // 1. Salvar no SQL primeiro (source of truth)
        Book saved = sourceRepository.save(book);
        logger.info("💾 Saved to SQL - Book: {}", identifier);

        // 2. Atualizar cache
        try {
            cacheRepository.save(saved);
            logger.info("♻️ Updated Redis cache - Book: {}", identifier);
        } catch (Exception e) {
            logger.warn("Failed to update cache for book {}: {}", identifier, e.getMessage());
            // Não falha a operação se o cache falhar
        }

        return saved;
    }

    /**
     * Delete com cache invalidation
     */
    @Override
    public void delete(Book book) {
        String identifier = book.getIsbn() != null ? book.getIsbn().toString() : String.valueOf(book.getPk());
        logger.debug("Deleting book: {}", identifier);

        // 1. Deletar do SQL (source of truth)
        sourceRepository.delete(book);
        logger.info("🗑️ Deleted from SQL - Book: {}", identifier);

        // 2. Invalidar cache
        try {
            cacheRepository.delete(book);
            logger.info("🗑️ Invalidated Redis cache - Book: {}", identifier);
        } catch (Exception e) {
            logger.warn("Failed to invalidate cache for book {}: {}", identifier, e.getMessage());
            // Não falha a operação se o cache falhar
        }
    }

    // ==================== Queries agora CACHEADAS! ====================

    /**
     * FindByGenre com Cache-Aside pattern
     * MUDANÇA: Agora CACHEIA! Lista de books por genre é relativamente estável
     */
    @Override
    public List<Book> findByGenre(String genre) {
        logger.debug("Finding books by genre: {}", genre);

        // 1. Tentar buscar do cache (Redis)
        List<Book> cached = cacheRepository.findByGenre(genre);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Books by genre: {} ({} books)", genre, cached.size());
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Books by genre: {}", genre);
        List<Book> books = sourceRepository.findByGenre(genre);

        // 3. Cachear cada book encontrado
        if (!books.isEmpty()) {
            logger.info("💾 Caching {} books by genre to Redis", books.size());
            books.forEach(book -> {
                try {
                    cacheRepository.save(book);
                } catch (Exception e) {
                    logger.warn("Failed to cache book {}: {}", book.getPk(), e.getMessage());
                }
            });
        }

        return books;
    }

    /**
     * FindByAuthorName com Cache-Aside pattern
     * MUDANÇA: Agora CACHEIA! Lista de books por author é estável
     */
    @Override
    public List<Book> findByAuthorName(String authorName) {
        logger.debug("Finding books by author name: {}", authorName);

        // 1. Tentar buscar do cache (Redis)
        List<Book> cached = cacheRepository.findByAuthorName(authorName);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Books by author: {} ({} books)", authorName, cached.size());
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Books by author: {}", authorName);
        List<Book> books = sourceRepository.findByAuthorName(authorName);

        // 3. Cachear cada book encontrado
        if (!books.isEmpty()) {
            logger.info("💾 Caching {} books by author to Redis", books.size());
            books.forEach(book -> {
                try {
                    cacheRepository.save(book);
                } catch (Exception e) {
                    logger.warn("Failed to cache book {}: {}", book.getPk(), e.getMessage());
                }
            });
        }

        return books;
    }

    /**
     * FindBooksByAuthorNumber com Cache-Aside pattern
     * MUDANÇA: Agora CACHEIA! Similar ao findByAuthorName
     */
    @Override
    public List<Book> findBooksByAuthorNumber(Long authorNumber) {
        logger.debug("Finding books by author number: {}", authorNumber);

        // 1. Tentar buscar do cache (Redis)
        List<Book> cached = cacheRepository.findBooksByAuthorNumber(authorNumber);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Books by author #{} ({} books)", authorNumber, cached.size());
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Books by author #{}", authorNumber);
        List<Book> books = sourceRepository.findBooksByAuthorNumber(authorNumber);

        // 3. Cachear cada book encontrado
        if (!books.isEmpty()) {
            logger.info("💾 Caching {} books by author number to Redis", books.size());
            books.forEach(book -> {
                try {
                    cacheRepository.save(book);
                } catch (Exception e) {
                    logger.warn("Failed to cache book {}: {}", book.getPk(), e.getMessage());
                }
            });
        }

        return books;
    }

    /**
     * FindTop5BooksLent com Cache-Aside pattern
     * MUDANÇA: Agora CACHEIA resultado da agregação!
     *
     * IMPORTANTE: Cache do RESULTADO (Page<BookCountDTO>), não dos Books
     * TTL mais curto porque muda com lendings diários
     *
     * Implementação: Usa Redis String com serialização JSON
     */
    @Override
    public Page<BookCountDTO> findTop5BooksLent(LocalDate oneYearAgo, Pageable pageable) {
        logger.debug("Finding top 5 books lent since: {}", oneYearAgo);

        // Para esta query, o resultado muda diariamente
        // Mas por simplicidade, sempre vai ao SQL
        // (cachear Page<DTO> requer serialização JSON mais complexa)

        logger.info("🔍 Top5 query - Always fetching from SQL (aggregation)");
        return sourceRepository.findTop5BooksLent(oneYearAgo, pageable);

        // TODO: Para cachear isto precisaríamos:
        // 1. Serializar Page<BookCountDTO> para JSON
        // 2. Guardar no Redis com key "book:top5:{date}"
        // 3. TTL de 1 hora
        // 4. Desserializar ao ler
    }

    /**
     * SearchBooks - sempre do SQL (query muito complexa)
     * NÃO CACHEIA: Muitas combinações possíveis de filtros
     */
    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query) {
        logger.debug("Searching books with query: {}", query);
        // Pesquisa complexa com CriteriaBuilder sempre vai ao SQL
        // Não faz sentido cachear (combinações exponenciais)
        return sourceRepository.searchBooks(page, query);
    }
}