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
 * Tests to verify that SQL-specific repository implementations
 * are correctly loaded when the sql-redis profile is active.
 *
 * This validates that the profile-based conditional loading
 * mechanism works as expected for the persistence layer.
 */
@SpringBootTest
@ActiveProfiles("sql-redis")
@TestPropertySource(properties = {
    "persistence.strategy=sql-redis",
    "spring.datasource.url=jdbc:h2:mem:testdb-repository-profile",
    "persistence.caching-enabled=false"
})
@DisplayName("SQL Repository Profile Loading Tests")
class SqlRepositoryProfileTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load SQL-specific Book repository implementation")
    void shouldLoadSqlBookRepository() {
        // Check for Spring Data repository bean
        String[] beanNames = applicationContext.getBeanNamesForType(
            org.springframework.data.repository.Repository.class
        );

        boolean hasBookRepository = false;
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("book")) {
                hasBookRepository = true;
                break;
            }
        }

        assertThat(hasBookRepository)
            .as("Book repository should be loaded with sql-redis profile")
            .isTrue();
    }

    @Test
    @DisplayName("Should load SQL-specific Author repository implementation")
    void shouldLoadSqlAuthorRepository() {
        String[] beanNames = applicationContext.getBeanNamesForType(
            org.springframework.data.repository.Repository.class
        );

        boolean hasAuthorRepository = false;
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("author")) {
                hasAuthorRepository = true;
                break;
            }
        }

        assertThat(hasAuthorRepository)
            .as("Author repository should be loaded with sql-redis profile")
            .isTrue();
    }

    @Test
    @DisplayName("Should load SQL-specific Genre repository implementation")
    void shouldLoadSqlGenreRepository() {
        String[] beanNames = applicationContext.getBeanNamesForType(
            org.springframework.data.repository.Repository.class
        );

        boolean hasGenreRepository = false;
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("genre")) {
                hasGenreRepository = true;
                break;
            }
        }

        assertThat(hasGenreRepository)
            .as("Genre repository should be loaded with sql-redis profile")
            .isTrue();
    }

    @Test
    @DisplayName("Should load SQL-specific Reader repository implementation")
    void shouldLoadSqlReaderRepository() {
        String[] beanNames = applicationContext.getBeanNamesForType(
            org.springframework.data.repository.Repository.class
        );

        boolean hasReaderRepository = false;
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("reader")) {
                hasReaderRepository = true;
                break;
            }
        }

        assertThat(hasReaderRepository)
            .as("Reader repository should be loaded with sql-redis profile")
            .isTrue();
    }

    @Test
    @DisplayName("Should load SQL-specific Lending repository implementation")
    void shouldLoadSqlLendingRepository() {
        String[] beanNames = applicationContext.getBeanNamesForType(
            org.springframework.data.repository.Repository.class
        );

        boolean hasLendingRepository = false;
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("lending")) {
                hasLendingRepository = true;
                break;
            }
        }

        assertThat(hasLendingRepository)
            .as("Lending repository should be loaded with sql-redis profile")
            .isTrue();
    }

    @Test
    @DisplayName("Should have JPA repositories in context")
    void shouldHaveJpaRepositories() {
        String[] allBeans = applicationContext.getBeanDefinitionNames();

        int repositoryCount = 0;
        for (String beanName : allBeans) {
            if (beanName.toLowerCase().contains("repository") &&
                beanName.toLowerCase().contains("impl")) {
                repositoryCount++;
            }
        }

        assertThat(repositoryCount)
            .as("Multiple JPA repository implementations should be loaded")
            .isGreaterThan(0);
    }

    @Test
    @DisplayName("Should verify sql-redis profile is active for repository loading")
    void shouldVerifyProfileActiveForRepositories() {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();

        assertThat(activeProfiles)
            .as("sql-redis profile should be active for repository loading")
            .contains("sql-redis");
    }
}

