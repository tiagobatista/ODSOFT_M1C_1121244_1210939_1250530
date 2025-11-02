package pt.psoft.g1.psoftg1.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for persistence strategy.
 * Allows selecting database type at setup time which impacts runtime behavior.
 *
 * Supported strategies:
 * - sql-redis: SQL database (H2/PostgreSQL/MySQL) with Redis caching
 * - mongodb-redis: MongoDB with Redis caching
 * - elasticsearch: ElasticSearch
 */
@Configuration
@ConfigurationProperties(prefix = "persistence")
@Data
public class PersistenceConfig {

    /**
     * The persistence strategy to use.
     * Valid values: sql-redis, mongodb-redis, elasticsearch
     */
    private String strategy = "sql-redis";

    /**
     * Whether caching is enabled (for sql-redis and mongodb-redis strategies)
     */
    private boolean cachingEnabled = true;

    /**
     * Cache TTL (Time To Live) in seconds for different entity types
     */
    private CacheTTL cacheTtl = new CacheTTL();

    @Data
    public static class CacheTTL {
        private long lendings = 900;  // 15 minutes
        private long books = 3600;    // 1 hour
        private long authors = 3600;  // 1 hour
        private long readers = 3600;  // 1 hour
        private long isbn = 86400;    // 24 hours
    }
}

