package pt.psoft.g1.psoftg1.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PersistenceConfig to verify that the configuration
 * correctly responds to different persistence.strategy property values.
 *
 * These tests validate the configuration-driven runtime behavior:
 * - Correct beans are loaded based on persistence.strategy
 * - Profile-specific configurations are applied
 * - Infrastructure components are properly initialized
 */
@SpringBootTest
@ActiveProfiles("sql-redis")
@TestPropertySource(properties = {
    "persistence.strategy=sql-redis",
    "spring.datasource.url=jdbc:h2:mem:testdb-persistence-config",
    "persistence.caching-enabled=false"
})
@DisplayName("PersistenceConfig Configuration Tests")
class PersistenceConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load PersistenceConfig bean")
    void shouldLoadPersistenceConfigBean() {
        assertThat(applicationContext.containsBean("persistenceConfig"))
            .as("PersistenceConfig bean should be loaded")
            .isTrue();
    }

    @Test
    @DisplayName("Should have correct persistence strategy property")
    void shouldHaveCorrectPersistenceStrategy() {
        String strategy = applicationContext.getEnvironment().getProperty("persistence.strategy");

        assertThat(strategy)
            .as("Persistence strategy should match configuration")
            .isEqualTo("sql-redis");
    }

    @Test
    @DisplayName("Should have caching configuration property")
    void shouldHaveCachingConfiguration() {
        String cachingEnabled = applicationContext.getEnvironment()
            .getProperty("persistence.caching-enabled");

        assertThat(cachingEnabled)
            .as("Caching configuration should be present")
            .isNotNull();
    }

    @Test
    @DisplayName("Should load JPA configuration beans for SQL strategy")
    void shouldLoadJpaConfigurationBeans() {
        // JPA Config should be loaded
        boolean hasJpaConfig = applicationContext.containsBean("jpaConfig")
            || applicationContext.containsBean("entityManagerFactory");

        assertThat(hasJpaConfig)
            .as("JPA configuration beans should be present for SQL strategy")
            .isTrue();
    }

    @Test
    @DisplayName("Should verify DataSource is configured correctly")
    void shouldVerifyDataSourceConfiguration() {
        assertThat(applicationContext.containsBean("dataSource"))
            .as("DataSource bean should exist")
            .isTrue();

        String datasourceUrl = applicationContext.getEnvironment()
            .getProperty("spring.datasource.url");

        assertThat(datasourceUrl)
            .as("DataSource URL should be configured")
            .isNotNull()
            .contains("h2:mem:");
    }

    @Test
    @DisplayName("Should have transaction manager configured")
    void shouldHaveTransactionManager() {
        assertThat(applicationContext.containsBean("transactionManager"))
            .as("TransactionManager should be configured for SQL strategy")
            .isTrue();
    }
}

