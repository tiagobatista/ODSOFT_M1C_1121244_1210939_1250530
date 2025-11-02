package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;

/**
 * Implementação do AuthorRepository usando padrão Cache-Aside
 *
 * Esta classe coordena entre Redis (cache) e SQL (source of truth):
 * - Leituras: tenta Redis primeiro, se não encontrar busca no SQL e atualiza cache
 * - Escritas: escreve no SQL e atualiza/invalida cache
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
@Repository
@ConditionalOnProperty(name = "repository.cache.enabled", havingValue = "true", matchIfMissing = true)
public class AuthorCacheRepository implements AuthorRepository {

    private static final Logger logger = LoggerFactory.getLogger(AuthorCacheRepository.class);

    private final AuthorRepository cacheRepository;  // Redis
    private final AuthorRepository sourceRepository; // SQL

    /**
     * Constructor com @Qualifier explícito em CADA parâmetro
     * IMPORTANTE: Os @Qualifier devem estar NOS PARÂMETROS, não nos campos!
     */
    public AuthorCacheRepository(
            @Qualifier("redisAuthorRepository") AuthorRepository cacheRepository,
            @Qualifier("authorRepositoryImpl") AuthorRepository sourceRepository) {
        this.cacheRepository = cacheRepository;
        this.sourceRepository = sourceRepository;
    }

    /**
     * Busca por authorNumber com Cache-Aside pattern
     */
    @Override
    public Optional<Author> findByAuthorNumber(Long authorNumber) {
        logger.debug("Finding author by number: {}", authorNumber);

        // 1. Tentar buscar do cache (Redis)
        Optional<Author> cached = cacheRepository.findByAuthorNumber(authorNumber);

        if (cached.isPresent()) {
            logger.info("🎯 CACHE HIT - Author ID: {}", authorNumber);
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Author ID: {}", authorNumber);
        Optional<Author> author = sourceRepository.findByAuthorNumber(authorNumber);

        // 3. Se encontrou, guardar no cache para próximas consultas
        if (author.isPresent()) {
            try {
                cacheRepository.save(author.get());
                logger.info("💾 Saved to Redis cache - Author ID: {}", authorNumber);
            } catch (Exception e) {
                logger.warn("Failed to cache author {}: {}", authorNumber, e.getMessage());
                // Não falha a operação se o cache falhar
            }
        }

        return author;
    }

    /**
     * Busca por nome (starts with) com cache
     */
    @Override
    public List<Author> searchByNameNameStartsWith(String name) {
        logger.debug("Searching authors by name starts with: {}", name);

        // Tentar cache primeiro
        List<Author> cached = cacheRepository.searchByNameNameStartsWith(name);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Name starts with: {}", name);
            return cached;
        }

        // Cache miss - buscar do SQL
        logger.info("❌ CACHE MISS - Fetching from SQL - Name starts with: {}", name);
        List<Author> authors = sourceRepository.searchByNameNameStartsWith(name);

        // Cachear cada author encontrado
        authors.forEach(author -> {
            try {
                cacheRepository.save(author);
            } catch (Exception e) {
                logger.warn("Failed to cache author {}: {}", author.getAuthorNumber(), e.getMessage());
            }
        });

        return authors;
    }

    /**
     * Busca por nome exato com cache
     */
    @Override
    public List<Author> searchByNameName(String name) {
        logger.debug("Searching authors by exact name: {}", name);

        // Tentar cache primeiro
        List<Author> cached = cacheRepository.searchByNameName(name);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Exact name: {}", name);
            return cached;
        }

        // Cache miss - buscar do SQL
        logger.info("❌ CACHE MISS - Fetching from SQL - Exact name: {}", name);
        List<Author> authors = sourceRepository.searchByNameName(name);

        // Cachear cada author encontrado
        authors.forEach(author -> {
            try {
                cacheRepository.save(author);
            } catch (Exception e) {
                logger.warn("Failed to cache author {}: {}", author.getAuthorNumber(), e.getMessage());
            }
        });

        return authors;
    }

    /**
     * Save com Write-Through pattern
     */
    @Override
    public Author save(Author author) {
        logger.debug("Saving author: {}", author.getAuthorNumber());

        // 1. Salvar no SQL primeiro (source of truth)
        Author saved = sourceRepository.save(author);
        logger.info("💾 Saved to SQL - Author ID: {}", saved.getAuthorNumber());

        // 2. Atualizar cache
        try {
            cacheRepository.save(saved);
            logger.info("♻️ Updated Redis cache - Author ID: {}", saved.getAuthorNumber());
        } catch (Exception e) {
            logger.warn("Failed to update cache for author {}: {}", saved.getAuthorNumber(), e.getMessage());
            // Não falha a operação se o cache falhar
        }

        return saved;
    }

    /**
     * FindAll - sempre do SQL (lista completa é pesada para cache)
     */
    @Override
    public Iterable<Author> findAll() {
        logger.debug("Finding all authors");
        // Operação pesada, sempre vai ao SQL
        return sourceRepository.findAll();
    }

    /**
     * Top authors by lendings - sempre do SQL (query complexa)
     */
    @Override
    public Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageableRules) {
        logger.debug("Finding top authors by lendings");
        // Agregações complexas sempre vão ao SQL
        return sourceRepository.findTopAuthorByLendings(pageableRules);
    }

    /**
     * Delete com cache invalidation
     */
    @Override
    public void delete(Author author) {
        logger.debug("Deleting author: {}", author.getAuthorNumber());

        // 1. Deletar do SQL (source of truth)
        sourceRepository.delete(author);
        logger.info("🗑️ Deleted from SQL - Author ID: {}", author.getAuthorNumber());

        // 2. Invalidar cache
        try {
            cacheRepository.delete(author);
            logger.info("🗑️ Invalidated Redis cache - Author ID: {}", author.getAuthorNumber());
        } catch (Exception e) {
            logger.warn("Failed to invalidate cache for author {}: {}", author.getAuthorNumber(), e.getMessage());
            // Não falha a operação se o cache falhar
        }
    }

    /**
     * Co-autores - sempre do SQL (relacionamentos complexos)
     */
    @Override
    public List<Author> findCoAuthorsByAuthorNumber(Long authorNumber) {
        logger.debug("Finding co-authors for author: {}", authorNumber);
        // Relacionamentos complexos sempre vão ao SQL
        return sourceRepository.findCoAuthorsByAuthorNumber(authorNumber);
    }
}