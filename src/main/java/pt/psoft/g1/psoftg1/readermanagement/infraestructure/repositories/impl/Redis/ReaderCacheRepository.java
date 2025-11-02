package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do ReaderRepository usando padrão Cache-Aside
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
public class ReaderCacheRepository implements ReaderRepository {

    private static final Logger logger = LoggerFactory.getLogger(ReaderCacheRepository.class);

    private final ReaderRepository cacheRepository;  // Redis
    private final ReaderRepository sourceRepository; // SQL

    /**
     * Constructor com @Qualifier explícito em CADA parâmetro
     */
    public ReaderCacheRepository(
            @Qualifier("redisReaderRepository") ReaderRepository cacheRepository,
            @Qualifier("readerDetailsRepositoryImpl") ReaderRepository sourceRepository) {
        this.cacheRepository = cacheRepository;
        this.sourceRepository = sourceRepository;
    }

    /**
     * Busca por readerNumber com Cache-Aside pattern
     */
    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        logger.debug("Finding reader by number: {}", readerNumber);

        // 1. Tentar buscar do cache (Redis)
        Optional<ReaderDetails> cached = cacheRepository.findByReaderNumber(readerNumber);

        if (cached.isPresent()) {
            logger.info("🎯 CACHE HIT - Reader Number: {}", readerNumber);
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Reader Number: {}", readerNumber);
        Optional<ReaderDetails> reader = sourceRepository.findByReaderNumber(readerNumber);

        // 3. Se encontrou, guardar no cache para próximas consultas
        if (reader.isPresent()) {
            try {
                cacheRepository.save(reader.get());
                logger.info("💾 Saved to Redis cache - Reader Number: {}", readerNumber);
            } catch (Exception e) {
                logger.warn("Failed to cache reader {}: {}", readerNumber, e.getMessage());
            }
        }

        return reader;
    }

    /**
     * Busca por phoneNumber com cache
     */
    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
        logger.debug("Finding readers by phone: {}", phoneNumber);

        // Tentar cache primeiro
        List<ReaderDetails> cached = cacheRepository.findByPhoneNumber(phoneNumber);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Phone: {}", phoneNumber);
            return cached;
        }

        // Cache miss - buscar do SQL
        logger.info("❌ CACHE MISS - Fetching from SQL - Phone: {}", phoneNumber);
        List<ReaderDetails> readers = sourceRepository.findByPhoneNumber(phoneNumber);

        // Cachear cada reader encontrado
        readers.forEach(reader -> {
            try {
                cacheRepository.save(reader);
            } catch (Exception e) {
                logger.warn("Failed to cache reader {}: {}", reader.getReaderNumber(), e.getMessage());
            }
        });

        return readers;
    }

    /**
     * Busca por username com cache
     */
    @Override
    public Optional<ReaderDetails> findByUsername(String username) {
        logger.debug("Finding reader by username: {}", username);

        // 1. Tentar cache
        Optional<ReaderDetails> cached = cacheRepository.findByUsername(username);

        if (cached.isPresent()) {
            logger.info("🎯 CACHE HIT - Username: {}", username);
            return cached;
        }

        // 2. Cache miss - buscar SQL
        logger.info("❌ CACHE MISS - Fetching from SQL - Username: {}", username);
        Optional<ReaderDetails> reader = sourceRepository.findByUsername(username);

        // 3. Cachear se encontrou
        reader.ifPresent(r -> {
            try {
                cacheRepository.save(r);
                logger.info("💾 Saved to Redis cache - Username: {}", username);
            } catch (Exception e) {
                logger.warn("Failed to cache reader {}: {}", username, e.getMessage());
            }
        });

        return reader;
    }

    /**
     * Busca por userId com cache
     */
    @Override
    public Optional<ReaderDetails> findByUserId(Long userId) {
        logger.debug("Finding reader by userId: {}", userId);

        // 1. Tentar cache
        Optional<ReaderDetails> cached = cacheRepository.findByUserId(userId);

        if (cached.isPresent()) {
            logger.info("🎯 CACHE HIT - UserId: {}", userId);
            return cached;
        }

        // 2. Cache miss - buscar SQL
        logger.info("❌ CACHE MISS - Fetching from SQL - UserId: {}", userId);
        Optional<ReaderDetails> reader = sourceRepository.findByUserId(userId);

        // 3. Cachear se encontrou
        reader.ifPresent(r -> {
            try {
                cacheRepository.save(r);
                logger.info("💾 Saved to Redis cache - UserId: {}", userId);
            } catch (Exception e) {
                logger.warn("Failed to cache reader for userId {}: {}", userId, e.getMessage());
            }
        });

        return reader;
    }

    /**
     * Count from current year - sempre do SQL (query complexa)
     */
    @Override
    public int getCountFromCurrentYear() {
        logger.debug("Getting count from current year");
        return sourceRepository.getCountFromCurrentYear();
    }

    /**
     * Save com Write-Through pattern
     */
    @Override
    public ReaderDetails save(ReaderDetails readerDetails) {
        logger.debug("Saving reader: {}", readerDetails.getReaderNumber());

        // 1. Salvar no SQL primeiro (source of truth)
        ReaderDetails saved = sourceRepository.save(readerDetails);
        logger.info("💾 Saved to SQL - Reader: {}", saved.getReaderNumber());

        // 2. Atualizar cache
        try {
            cacheRepository.save(saved);
            logger.info("♻️ Updated Redis cache - Reader: {}", saved.getReaderNumber());
        } catch (Exception e) {
            logger.warn("Failed to update cache for reader {}: {}", saved.getReaderNumber(), e.getMessage());
        }

        return saved;
    }

    /**
     * FindAll - sempre do SQL (lista completa é pesada para cache)
     */
    @Override
    public Iterable<ReaderDetails> findAll() {
        logger.debug("Finding all readers");
        return sourceRepository.findAll();
    }

    /**
     * Top readers - sempre do SQL (query complexa)
     */
    @Override
    public Page<ReaderDetails> findTopReaders(Pageable pageable) {
        logger.debug("Finding top readers");
        return sourceRepository.findTopReaders(pageable);
    }

    /**
     * Top by genre - sempre do SQL (agregação complexa)
     */
    @Override
    public Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate) {
        logger.debug("Finding top by genre");
        return sourceRepository.findTopByGenre(pageable, genre, startDate, endDate);
    }

    /**
     * Delete com cache invalidation
     */
    @Override
    public void delete(ReaderDetails readerDetails) {
        logger.debug("Deleting reader: {}", readerDetails.getReaderNumber());

        // 1. Deletar do SQL (source of truth)
        sourceRepository.delete(readerDetails);
        logger.info("🗑️ Deleted from SQL - Reader: {}", readerDetails.getReaderNumber());

        // 2. Invalidar cache
        try {
            cacheRepository.delete(readerDetails);
            logger.info("🗑️ Invalidated Redis cache - Reader: {}", readerDetails.getReaderNumber());
        } catch (Exception e) {
            logger.warn("Failed to invalidate cache for reader {}: {}", readerDetails.getReaderNumber(), e.getMessage());
        }
    }

    /**
     * Search readers - sempre do SQL (query complexa)
     */
    @Override
    public List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query) {
        logger.debug("Searching readers");
        return sourceRepository.searchReaderDetails(page, query);
    }
}