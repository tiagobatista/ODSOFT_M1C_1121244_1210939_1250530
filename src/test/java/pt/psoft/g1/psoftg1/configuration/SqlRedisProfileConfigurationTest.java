package pt.psoft.g1.psoftg1.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests to verify that the SQL-Redis persistence strategy
 * is correctly configured and loaded based on active Spring profiles.
 *
 * These tests validate that:
 * 1. The correct profile is active
 * 2. SQL-specific beans are loaded
 * 3. JPA/Hibernate configuration is present
 * 4. Redis configuration is available
 *
 * This demonstrates configuration-driven runtime behavior as required by ADD.
 */
@SpringBootTest
@ActiveProfiles({"sql-redis"})
@TestPropertySource(properties = {
    "persistence.strategy=sql-redis",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb-profile-test",
    "spring.redis.host=localhost",
    "spring.redis.port=6379",
    "persistence.caching-enabled=false"
})
@DisplayName("SQL-Redis Profile Configuration Tests")
class SqlRedisProfileConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load application context with sql-redis profile")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Should have sql-redis profile active")
    void shouldHaveSqlRedisProfileActive() {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();

        assertThat(activeProfiles)
            .as("Active profiles should contain 'sql-redis'")
            .contains("sql-redis");
    }

    @Test
    @DisplayName("Should have configured DataSource bean for SQL database")
    void shouldHaveDataSourceBean() {
        assertThat(applicationContext.containsBean("dataSource"))
            .as("DataSource bean should be present for SQL persistence")
            .isTrue();

        DataSource dataSource = applicationContext.getBean(DataSource.class);
        assertThat(dataSource)
            .as("DataSource should be properly initialized")
            .isNotNull();
    }

    @Test
    @DisplayName("Should have JPA EntityManagerFactory configured")
    void shouldHaveEntityManagerFactory() {
        assertThat(applicationContext.containsBean("entityManagerFactory"))
            .as("EntityManagerFactory should be present for JPA/SQL")
            .isTrue();
    }

    @Test
    @DisplayName("Should have Redis configuration available")
    void shouldHaveRedisConfiguration() {
        // Redis config beans exist even when caching is disabled
        boolean hasRedisConfig = applicationContext.containsBean("redisConfig")
            || applicationContext.containsBean("redisConnectionFactory")
            || applicationContext.containsBean("embeddedRedisConfig");

        assertThat(hasRedisConfig)
            .as("At least one Redis configuration bean should be present")
            .isTrue();
    }

    @Test
    @DisplayName("Should verify persistence.strategy property is set correctly")
    void shouldHaveCorrectPersistenceStrategy() {
        String strategy = applicationContext.getEnvironment().getProperty("persistence.strategy");

        assertThat(strategy)
            .as("persistence.strategy should be set to 'sql-redis'")
            .isEqualTo("sql-redis");
    }

    @Test
    @DisplayName("Should have SQL-specific repository beans loaded")
    void shouldHaveSqlRepositoryBeans() {
        // Check for at least one SQL repository implementation
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        boolean hasSqlRepositories = false;
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("repository") &&
                !beanName.toLowerCase().contains("mongo") &&
                !beanName.toLowerCase().contains("elastic")) {
                hasSqlRepositories = true;
                break;
            }
        }

        assertThat(hasSqlRepositories)
            .as("At least one SQL repository implementation should be loaded")
            .isTrue();
    }

    @Test
    @DisplayName("Should NOT have MongoDB beans when sql-redis profile is active")
    void shouldNotHaveMongoBeansWithSqlProfile() {
        boolean hasMongoClient = applicationContext.containsBean("mongoClient");
        boolean hasMongoTemplate = applicationContext.containsBean("mongoTemplate");

        assertThat(hasMongoClient || hasMongoTemplate)
            .as("MongoDB beans should NOT be loaded with sql-redis profile")
            .isFalse();
    }

    @Test
    @DisplayName("Should NOT have ElasticSearch beans when sql-redis profile is active")
    void shouldNotHaveElasticSearchBeansWithSqlProfile() {
        boolean hasElasticClient = applicationContext.containsBean("elasticsearchClient");
        boolean hasElasticTemplate = applicationContext.containsBean("elasticsearchTemplate");

        assertThat(hasElasticClient || hasElasticTemplate)
            .as("ElasticSearch beans should NOT be loaded with sql-redis profile")
            .isFalse();
    }
}

