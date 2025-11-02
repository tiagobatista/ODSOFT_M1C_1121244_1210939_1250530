package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Mapper.AuthorRedisMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementação Redis pura do AuthorRepository
 * Esta classe apenas interage com Redis, não com SQL
 */
@Repository("redisAuthorRepository")
@RequiredArgsConstructor
public class RedisAuthorRepositoryImpl implements AuthorRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthorRedisMapper mapper;

    private static final String KEY_PREFIX = "author:";
    private static final String KEY_BY_NAME = "author:name:";
    private static final String KEY_ALL_AUTHORS = "author:all";
    private static final Duration TTL = Duration.ofHours(1);

    /**
     * Busca Author por authorNumber
     */
    @Override
    public Optional<Author> findByAuthorNumber(Long authorNumber) {
        if (authorNumber == null) {
            return Optional.empty();
        }

        String key = KEY_PREFIX + authorNumber;
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

        if (hash.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(mapper.fromRedisHash(hash));
    }

    /**
     * Busca Authors cujo nome começa com o texto fornecido
     */
    @Override
    public List<Author> searchByNameNameStartsWith(String name) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }

        // Busca todas as keys de nomes que começam com o prefixo
        Set<String> keys = redisTemplate.keys(KEY_BY_NAME + name.toLowerCase() + "*");

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<Author> authors = new ArrayList<>();
        for (String key : keys) {
            String authorNumber = (String) redisTemplate.opsForValue().get(key);
            if (authorNumber != null) {
                findByAuthorNumber(Long.parseLong(authorNumber))
                        .ifPresent(authors::add);
            }
        }

        return authors;
    }

    /**
     * Busca Authors por nome exato
     */
    @Override
    public List<Author> searchByNameName(String name) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }

        String key = KEY_BY_NAME + name.toLowerCase();
        String authorNumber = (String) redisTemplate.opsForValue().get(key);

        if (authorNumber == null) {
            return Collections.emptyList();
        }

        return findByAuthorNumber(Long.parseLong(authorNumber))
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    /**
     * Salva Author no Redis
     */
    @Override
    public Author save(Author author) {
        if (author == null || author.getAuthorNumber() == null) {
            throw new IllegalArgumentException("Author and AuthorNumber cannot be null");
        }

        String key = KEY_PREFIX + author.getAuthorNumber();
        Map<String, String> hash = mapper.toRedisHash(author);

        // Salvar como hash
        redisTemplate.opsForHash().putAll(key, hash);
        redisTemplate.expire(key, TTL.getSeconds(), TimeUnit.SECONDS);

        // Criar índice por nome (para buscas)
        if (author.getName() != null) {
            String nameKey = KEY_BY_NAME + author.getName().toString().toLowerCase();
            redisTemplate.opsForValue().set(nameKey, author.getAuthorNumber().toString(), TTL);
        }

        // Adicionar à lista de todos os authors
        redisTemplate.opsForSet().add(KEY_ALL_AUTHORS, author.getAuthorNumber().toString());
        redisTemplate.expire(KEY_ALL_AUTHORS, TTL.getSeconds(), TimeUnit.SECONDS);

        return author;
    }

    /**
     * Retorna todos os Authors
     */
    @Override
    public Iterable<Author> findAll() {
        Set<Object> authorNumbers = redisTemplate.opsForSet().members(KEY_ALL_AUTHORS);

        if (authorNumbers == null || authorNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Author> authors = new ArrayList<>();
        for (Object authorNumber : authorNumbers) {
            findByAuthorNumber(Long.parseLong(authorNumber.toString()))
                    .ifPresent(authors::add);
        }

        return authors;
    }

    /**
     * Top authors by lendings - não suportado diretamente no Redis
     * Retorna página vazia (deve ser buscado do SQL)
     */
    @Override
    public Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageableRules) {
        // Operações complexas de agregação não são suportadas no Redis cache
        // Estas devem sempre ir ao SQL
        return new PageImpl<>(Collections.emptyList(), pageableRules, 0);
    }

    /**
     * Delete Author do Redis
     */
    @Override
    public void delete(Author author) {
        if (author == null || author.getAuthorNumber() == null) {
            return;
        }

        String key = KEY_PREFIX + author.getAuthorNumber();

        // Deletar índice por nome
        if (author.getName() != null) {
            String nameKey = KEY_BY_NAME + author.getName().toString().toLowerCase();
            redisTemplate.delete(nameKey);
        }

        // Remover da lista de todos os authors
        redisTemplate.opsForSet().remove(KEY_ALL_AUTHORS, author.getAuthorNumber().toString());

        // Deletar o hash principal
        redisTemplate.delete(key);
    }

    /**
     * Busca co-autores - não suportado diretamente no Redis
     * Retorna lista vazia (deve ser buscado do SQL)
     */
    @Override
    public List<Author> findCoAuthorsByAuthorNumber(Long authorNumber) {
        // Relacionamentos complexos não são mantidos no Redis cache
        // Estas queries devem sempre ir ao SQL
        return Collections.emptyList();
    }
}