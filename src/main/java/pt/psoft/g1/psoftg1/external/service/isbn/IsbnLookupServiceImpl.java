package pt.psoft.g1.psoftg1.external.service.isbn;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.external.service.isbn.providers.ExternalIsbnProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de busca de ISBN com cache Redis
 */
@Service
@RequiredArgsConstructor
public class IsbnLookupServiceImpl implements IsbnLookupService {

    private static final Logger logger = LoggerFactory.getLogger(IsbnLookupServiceImpl.class);
    private static final String CACHE_PREFIX = "isbn:search:";
    private static final Duration CACHE_TTL = Duration.ofHours(24); // 24 horas

    private final List<ExternalIsbnProvider> providers;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<IsbnSearchResult> searchIsbnByTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }

        String normalizedTitle = title.trim().toLowerCase();
        String cacheKey = CACHE_PREFIX + normalizedTitle;

        // 1. Tentar buscar do cache Redis
        List<IsbnSearchResult> cached = getCachedResults(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - ISBN search for title: {}", title);
            return cached;
        }

        logger.info("❌ CACHE MISS - Searching external APIs for title: {}", title);

        // 2. Buscar dos providers em ordem de prioridade
        List<ExternalIsbnProvider> sortedProviders = providers.stream()
                .filter(ExternalIsbnProvider::isAvailable)
                .sorted(Comparator.comparingInt(ExternalIsbnProvider::getPriority))
                .collect(Collectors.toList());

        List<IsbnSearchResult> allResults = new ArrayList<>();

        for (ExternalIsbnProvider provider : sortedProviders) {
            try {
                logger.info("🔍 Trying provider: {}", provider.getProviderName());
                List<IsbnSearchResult> results = provider.searchByTitle(title);

                if (!results.isEmpty()) {
                    allResults.addAll(results);
                    logger.info("✅ Found {} results from {}", results.size(), provider.getProviderName());
                }

            } catch (Exception e) {
                logger.warn("⚠️ Provider {} failed: {}", provider.getProviderName(), e.getMessage());
                // Continua para o próximo provider (fallback)
            }
        }

        // 3. Guardar no cache se encontrou resultados
        if (!allResults.isEmpty()) {
            cacheResults(cacheKey, allResults);
        }

        return allResults;
    }

    @Override
    public List<IsbnSearchResult> searchIsbnByTitleWithProvider(String title, String providerName) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }

        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Provider name cannot be null or blank");
        }

        String normalizedTitle = title.trim().toLowerCase();
        String cacheKey = CACHE_PREFIX + providerName.toLowerCase() + ":" + normalizedTitle;

        // 1. Tentar cache
        List<IsbnSearchResult> cached = getCachedResults(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            logger.info("🎯 CACHE HIT - ISBN search for title: {} with provider: {}", title, providerName);
            return cached;
        }

        // 2. Buscar do provider específico
        ExternalIsbnProvider provider = providers.stream()
                .filter(p -> p.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerName));

        if (!provider.isAvailable()) {
            throw new IllegalStateException("Provider is not available: " + providerName);
        }

        try {
            logger.info("🔍 Searching with provider: {}", providerName);
            List<IsbnSearchResult> results = provider.searchByTitle(title);

            if (!results.isEmpty()) {
                cacheResults(cacheKey, results);
            }

            return results;

        } catch (Exception e) {
            logger.error("Error searching with provider {}: {}", providerName, e.getMessage());
            throw new RuntimeException("Failed to search with provider: " + providerName, e);
        }
    }

    @Override
    public List<String> getAvailableProviders() {
        return providers.stream()
                .filter(ExternalIsbnProvider::isAvailable)
                .map(ExternalIsbnProvider::getProviderName)
                .collect(Collectors.toList());
    }

    // ==================== Cache helpers ====================

    @SuppressWarnings("unchecked")
    private List<IsbnSearchResult> getCachedResults(String key) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                return (List<IsbnSearchResult>) cached;
            }
        } catch (Exception e) {
            logger.warn("Failed to get cached results: {}", e.getMessage());
        }
        return null;
    }

    private void cacheResults(String key, List<IsbnSearchResult> results) {
        try {
            redisTemplate.opsForValue().set(key, results, CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
            logger.info("💾 Cached {} ISBN results", results.size());
        } catch (Exception e) {
            logger.warn("Failed to cache results: {}", e.getMessage());
        }
    }
}