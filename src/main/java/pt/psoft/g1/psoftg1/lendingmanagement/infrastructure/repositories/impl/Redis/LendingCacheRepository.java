package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do LendingRepository usando padrão Cache-Aside SELETIVO
 *
 * Esta classe coordena entre Redis (cache) e SQL (source of truth):
 * - Leituras individuais: tenta Redis primeiro, depois SQL
 * - Lendings ativos: cacheia para acesso rápido
 * - Queries complexas: sempre vão direto ao SQL
 * - Escritas: escreve no SQL e atualiza cache
 *
 * ESTRATÉGIA SELETIVA:
 * - Cacheia: findByLendingNumber, listOutstandingByReaderNumber
 * - NÃO cacheia: queries complexas, agregações, histórico completo
 */
@Profile("sql-redis")
@Primary
@Repository
@ConditionalOnProperty(name = "repository.cache.enabled", havingValue = "true", matchIfMissing = true)
public class LendingCacheRepository implements LendingRepository {

    private static final Logger logger = LoggerFactory.getLogger(LendingCacheRepository.class);

    private final LendingRepository cacheRepository;  // Redis
    private final LendingRepository sourceRepository; // SQL

    /**
     * Constructor com @Qualifier explícito em CADA parâmetro
     */
    public LendingCacheRepository(
            @Qualifier("redisLendingRepository") LendingRepository cacheRepository,
            @Qualifier("lendingRepositoryImpl") LendingRepository sourceRepository) {
        this.cacheRepository = cacheRepository;
        this.sourceRepository = sourceRepository;
    }

    /**
     * Busca por lendingNumber com Cache-Aside pattern
     */
    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        logger.debug("Finding lending by number: {}", lendingNumber);

        // 1. Tentar buscar do cache (Redis)
        Optional<Lending> cached = cacheRepository.findByLendingNumber(lendingNumber);

        if (cached.isPresent()) {
            logger.info("🎯 CACHE HIT - Lending: {}", lendingNumber);
            return cached;
        }

        // 2. Cache miss - buscar do SQL (source of truth)
        logger.info("❌ CACHE MISS - Fetching from SQL - Lending: {}", lendingNumber);
        Optional<Lending> lending = sourceRepository.findByLendingNumber(lendingNumber);

        // 3. Se encontrou, guardar no cache
        if (lending.isPresent()) {
            try {
                cacheRepository.save(lending.get());
                logger.info("💾 Saved to Redis cache - Lending: {}", lendingNumber);
            } catch (Exception e) {
                logger.warn("Failed to cache lending {}: {}", lendingNumber, e.getMessage());
            }
        }

        return lending;
    }

    /**
     * Lista lendings ativos por readerNumber com cache
     */
    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber) {
        logger.debug("Finding outstanding lendings for reader: {}", readerNumber);

        // 1. Tentar cache
        List<Lending> cached = cacheRepository.listOutstandingByReaderNumber(readerNumber);

        if (!cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - Outstanding lendings for reader: {}", readerNumber);
            return cached;
        }

        // 2. Cache miss - buscar SQL
        logger.info("❌ CACHE MISS - Fetching from SQL - Outstanding lendings for reader: {}", readerNumber);
        List<Lending> lendings = sourceRepository.listOutstandingByReaderNumber(readerNumber);

        // 3. Cachear cada lending encontrado
        lendings.forEach(lending -> {
            try {
                cacheRepository.save(lending);
            } catch (Exception e) {
                logger.warn("Failed to cache lending {}: {}", lending.getLendingNumber(), e.getMessage());
            }
        });

        return lendings;
    }

    /**
     * Save com Write-Through pattern
     */
    @Override
    public Lending save(Lending lending) {
        logger.debug("Saving lending: {}", lending.getLendingNumber());

        // 1. Salvar no SQL primeiro (source of truth)
        Lending saved = sourceRepository.save(lending);
        logger.info("💾 Saved to SQL - Lending: {}", saved.getLendingNumber());

        // 2. Atualizar cache
        try {
            cacheRepository.save(saved);
            logger.info("♻️ Updated Redis cache - Lending: {}", saved.getLendingNumber());
        } catch (Exception e) {
            logger.warn("Failed to update cache for lending {}: {}", saved.getLendingNumber(), e.getMessage());
        }

        return saved;
    }

    /**
     * Delete com cache invalidation
     */
    @Override
    public void delete(Lending lending) {
        logger.debug("Deleting lending: {}", lending.getLendingNumber());

        // 1. Deletar do SQL (source of truth)
        sourceRepository.delete(lending);
        logger.info("🗑️ Deleted from SQL - Lending: {}", lending.getLendingNumber());

        // 2. Invalidar cache
        try {
            cacheRepository.delete(lending);
            logger.info("🗑️ Invalidated Redis cache - Lending: {}", lending.getLendingNumber());
        } catch (Exception e) {
            logger.warn("Failed to invalidate cache for lending {}: {}", lending.getLendingNumber(), e.getMessage());
        }
    }

    // ==================== Métodos que sempre vão ao SQL ====================

    /**
     * Query complexa - sempre SQL
     */
    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) {
        logger.debug("Listing by reader and ISBN - always from SQL");
        return sourceRepository.listByReaderNumberAndIsbn(readerNumber, isbn);
    }

    /**
     * Agregação - sempre SQL
     */
    @Override
    public int getCountFromCurrentYear() {
        logger.debug("Getting count from current year - always from SQL");
        return sourceRepository.getCountFromCurrentYear();
    }

    /**
     * Agregação - sempre SQL
     */
    @Override
    public Double getAverageDuration() {
        logger.debug("Getting average duration - always from SQL");
        return sourceRepository.getAverageDuration();
    }

    /**
     * Agregação - sempre SQL
     */
    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        logger.debug("Getting avg lending duration by ISBN - always from SQL");
        return sourceRepository.getAvgLendingDurationByIsbn(isbn);
    }

    /**
     * Query complexa com paginação - sempre SQL
     */
    @Override
    public List<Lending> getOverdue(Page page) {
        logger.debug("Getting overdue lendings - always from SQL");
        return sourceRepository.getOverdue(page);
    }

    /**
     * Query complexa com múltiplos filtros - sempre SQL
     */
    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn,
                                        Boolean returned, LocalDate startDate, LocalDate endDate) {
        logger.debug("Searching lendings with filters - always from SQL");
        return sourceRepository.searchLendings(page, readerNumber, isbn, returned, startDate, endDate);
    }
}