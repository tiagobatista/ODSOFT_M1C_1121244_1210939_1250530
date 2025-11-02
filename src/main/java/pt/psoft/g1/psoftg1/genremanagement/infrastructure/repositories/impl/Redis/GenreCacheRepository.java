package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do GenreRepository usando padrão Cache-Aside
 *
 * Esta classe coordena entre Redis (cache) e SQL (source of truth):
 * - Leituras: tenta Redis primeiro, se não encontrar busca no SQL e atualiza cache
 * - Escritas: escreve no SQL e atualiza/invalida cache
 *
 * DIFERENÇA IMPORTANTE para Author:
 * - findAll() é CACHEADO porque Genre tem poucos registos (~10-50)
 * - Genre muda raramente, então TTL pode ser maior (24h vs 1h)
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
@Repository("genreCacheRepository")
@ConditionalOnProperty(name = "repository.cache.enabled", havingValue = "true", matchIfMissing = true)
public class GenreCacheRepository implements GenreRepository {

    private static final Logger logger = LoggerFactory.getLogger(GenreCacheRepository.class);

    private final GenreRepository cacheRepository;  // Redis
    private final GenreRepository sourceRepository; // SQL

    /**
     * Constructor com @Qualifier explícito em CADA parâmetro
     * IMPORTANTE: Os @Qualifier devem estar NOS PARÂMETROS, não nos campos!
     */
    public GenreCacheRepository(
            @Qualifier("redisGenreRepository") GenreRepository cacheRepository,
            @Qualifier("genreRepositoryImpl") GenreRepository sourceRepository) {
        this.cacheRepository = cacheRepository;
        this.sourceRepository = sourceRepository;
    }

    /**
     * FindAll com Cache-Aside pattern
     *
     * IMPORTANTE: Para Genre, FAZ SENTIDO cachear findAll() porque:
     * - São poucos registos (~10-50 géneros)
     * - Mudam raramente
     * - São consultados frequentemente (cada livro tem género)
     * - Lista pequena não sobrecarrega cache
     *
     * Esta é a DIFERENÇA PRINCIPAL em relação ao Author!
     */
    @Override
    public Iterable<Genre> findAll() {
        logger.debug("Finding all genres");

        // 1. Tentar buscar lista completa do cache (Redis)
        List<Genre> cached = new ArrayList<>();
        cacheRepository.findAll().forEach(cached::add);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - All genres ({})", cached.size());
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching all genres from SQL");
        List<Genre> genres = new ArrayList<>();
        sourceRepository.findAll().forEach(genres::add);

        // 3. Cachear cada genre individualmente
        if (!genres.isEmpty()) {
            logger.info("💾 Caching {} genres to Redis", genres.size());
            genres.forEach(genre -> {
                try {
                    cacheRepository.save(genre);
                } catch (Exception e) {
                    logger.warn("Failed to cache genre {}: {}", genre.getPk(), e.getMessage());
                    // Não falha a operação se o cache falhar
                }
            });
        }

        return genres;
    }

    /**
     * Busca por nome (findByString) com Cache-Aside pattern
     */
    @Override
    public Optional<Genre> findByString(String genreName) {
        logger.debug("Finding genre by name: {}", genreName);

        // 1. Tentar buscar do cache (Redis)
        Optional<Genre> cached = cacheRepository.findByString(genreName);

        if (cached.isPresent()) {
            logger.info("🎯 CACHE HIT - Genre name: {}", genreName);
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Genre name: {}", genreName);
        Optional<Genre> genre = sourceRepository.findByString(genreName);

        // 3. Se encontrou, guardar no cache para próximas consultas
        if (genre.isPresent()) {
            try {
                cacheRepository.save(genre.get());
                logger.info("💾 Saved to Redis cache - Genre: {}", genreName);
            } catch (Exception e) {
                logger.warn("Failed to cache genre {}: {}", genreName, e.getMessage());
                // Não falha a operação se o cache falhar
            }
        }

        return genre;
    }

    /**
     * Save com Write-Through pattern
     */
    @Override
    public Genre save(Genre genre) {
        logger.debug("Saving genre: {}", genre.getGenre());

        // 1. Salvar no SQL primeiro (source of truth)
        Genre saved = sourceRepository.save(genre);
        logger.info("💾 Saved to SQL - Genre: {}", saved.getGenre());

        // 2. Atualizar cache
        try {
            cacheRepository.save(saved);
            logger.info("♻️ Updated Redis cache - Genre: {}", saved.getGenre());
        } catch (Exception e) {
            logger.warn("Failed to update cache for genre {}: {}", saved.getGenre(), e.getMessage());
            // Não falha a operação se o cache falhar
        }

        return saved;
    }

    /**
     * Delete com cache invalidation
     */
    @Override
    public void delete(Genre genre) {
        logger.debug("Deleting genre: {}", genre.getGenre());

        // 1. Deletar do SQL (source of truth)
        sourceRepository.delete(genre);
        logger.info("🗑️ Deleted from SQL - Genre: {}", genre.getGenre());

        // 2. Invalidar cache
        try {
            cacheRepository.delete(genre);
            logger.info("🗑️ Invalidated Redis cache - Genre: {}", genre.getGenre());
        } catch (Exception e) {
            logger.warn("Failed to invalidate cache for genre {}: {}", genre.getGenre(), e.getMessage());
            // Não falha a operação se o cache falhar
        }
    }

    // ==================== Queries complexas - sempre SQL ====================

    /**
     * Top 5 genres by book count - sempre do SQL (agregação complexa)
     */
    @Override
    public org.springframework.data.domain.Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable) {
        logger.debug("Finding top 5 genres by book count");
        // Agregações complexas sempre vão ao SQL
        return sourceRepository.findTop5GenreByBookCount(pageable);
    }

    /**
     * Lendings per month last year - sempre do SQL (query complexa)
     */
    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() {
        logger.debug("Getting lendings per month last year by genre");
        // Queries com joins e agregações sempre vão ao SQL
        return sourceRepository.getLendingsPerMonthLastYearByGenre();
    }

    /**
     * Average lendings in month - sempre do SQL (agregação)
     */
    @Override
    public List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month, pt.psoft.g1.psoftg1.shared.services.Page page) {
        logger.debug("Getting average lendings in month: {}", month);
        // Agregações sempre vão ao SQL
        return sourceRepository.getAverageLendingsInMonth(month, page);
    }

    /**
     * Lendings average duration per month - sempre do SQL (agregação complexa)
     */
    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate) {
        logger.debug("Getting lendings average duration per month from {} to {}", startDate, endDate);
        // Agregações complexas sempre vão ao SQL
        return sourceRepository.getLendingsAverageDurationPerMonth(startDate, endDate);
    }
}