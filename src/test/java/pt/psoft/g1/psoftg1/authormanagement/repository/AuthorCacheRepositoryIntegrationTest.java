package pt.psoft.g1.psoftg1.authormanagement.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Redis.AuthorCacheRepository;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration Test for AuthorCacheRepository (Cache-Aside Pattern)
 *
 * Purpose: Test the Cache-Aside pattern implementation
 * Testing Strategy: Mock both Redis and SQL repositories, verify cache behavior
 * SUT: AuthorCacheRepository (integration between cache and source)
 * Type: 2.3.4 - Functional opaque-box with SUT = controller+service+{domain, repository, gateways}
 *
 * Cache-Aside Pattern Testing:
 * - READ: Check cache first, if miss → read from SQL → update cache
 * - WRITE: Write to SQL first → update cache
 * - DELETE: Delete from SQL → invalidate cache
 *
 * Test Coverage:
 * - Cache hits (data found in Redis)
 * - Cache misses (data not in Redis, fetch from SQL)
 * - Cache updates on write operations
 * - Cache invalidation on delete
 * - Error handling (cache failures should not break operations)
 * - Complex queries (always go to SQL)
 *
 * @author ARQSOFT 2025-2026
 */
@ExtendWith(MockitoExtension.class)
class AuthorCacheRepositoryIntegrationTest {

    @Mock
    private AuthorRepository redisRepository;

    @Mock
    private AuthorRepository sqlRepository;

    private AuthorCacheRepository cacheRepository;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        cacheRepository = new AuthorCacheRepository(redisRepository, sqlRepository);

        testAuthor = new Author("John Doe", "Famous author biography", null);
        testAuthor.setAuthorNumber(1L);
        testAuthor.setVersion(1L);
    }

    // ========================================
    // FIND BY AUTHOR NUMBER - Cache-Aside Read Pattern
    // ========================================

    @Test
    void testFindByAuthorNumber_cacheHit_shouldReturnFromCacheWithoutQueryingSQL() {
        // Arrange
        Long authorNumber = 1L;
        when(redisRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = cacheRepository.findByAuthorNumber(authorNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());

        // Verify cache was checked
        verify(redisRepository).findByAuthorNumber(authorNumber);

        // Verify SQL was NOT queried (cache hit)
        verify(sqlRepository, never()).findByAuthorNumber(any());

        // Verify cache was NOT updated (already had data)
        verify(redisRepository, never()).save(any());
    }

    @Test
    void testFindByAuthorNumber_cacheMiss_shouldFetchFromSQLAndUpdateCache() {
        // Arrange
        Long authorNumber = 1L;

        // Cache miss - Redis returns empty
        when(redisRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());

        // SQL has the data
        when(sqlRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));

        // Cache save succeeds
        when(redisRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Optional<Author> result = cacheRepository.findByAuthorNumber(authorNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());

        // Verify cache was checked first
        verify(redisRepository).findByAuthorNumber(authorNumber);

        // Verify SQL was queried (cache miss)
        verify(sqlRepository).findByAuthorNumber(authorNumber);

        // Verify cache was updated with SQL data
        verify(redisRepository).save(testAuthor);
    }

    @Test
    void testFindByAuthorNumber_cacheMissAndSQLMiss_shouldReturnEmpty() {
        // Arrange
        Long authorNumber = 999L;

        when(redisRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());
        when(sqlRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());

        // Act
        Optional<Author> result = cacheRepository.findByAuthorNumber(authorNumber);

        // Assert
        assertFalse(result.isPresent());

        verify(redisRepository).findByAuthorNumber(authorNumber);
        verify(sqlRepository).findByAuthorNumber(authorNumber);

        // Verify cache was NOT updated (no data to cache)
        verify(redisRepository, never()).save(any());
    }

    @Test
    void testFindByAuthorNumber_cacheSaveFails_shouldStillReturnResult() {
        // Arrange
        Long authorNumber = 1L;

        when(redisRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());
        when(sqlRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));

        // Cache save throws exception
        when(redisRepository.save(any())).thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        Optional<Author> result = cacheRepository.findByAuthorNumber(authorNumber);

        // Assert
        // Operation should still succeed even if cache fails
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());

        verify(sqlRepository).findByAuthorNumber(authorNumber);
        verify(redisRepository).save(testAuthor);
    }

    // ========================================
    // SEARCH BY NAME STARTS WITH - Cache Pattern
    // ========================================

    @Test
    void testSearchByNameNameStartsWith_cacheHit_shouldReturnFromCache() {
        // Arrange
        String name = "John";
        List<Author> cachedAuthors = Arrays.asList(testAuthor);

        when(redisRepository.searchByNameNameStartsWith(name))
                .thenReturn(cachedAuthors);

        // Act
        List<Author> result = cacheRepository.searchByNameNameStartsWith(name);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAuthor, result.get(0));

        verify(redisRepository).searchByNameNameStartsWith(name);
        verify(sqlRepository, never()).searchByNameNameStartsWith(any());
    }

    @Test
    void testSearchByNameNameStartsWith_cacheMiss_shouldFetchFromSQLAndCacheEach() {
        // Arrange
        String name = "John";

        Author author1 = new Author("John Doe", "Bio 1", null);
        author1.setAuthorNumber(1L);
        Author author2 = new Author("John Smith", "Bio 2", null);
        author2.setAuthorNumber(2L);

        List<Author> sqlAuthors = Arrays.asList(author1, author2);

        when(redisRepository.searchByNameNameStartsWith(name))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.searchByNameNameStartsWith(name))
                .thenReturn(sqlAuthors);
        when(redisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        List<Author> result = cacheRepository.searchByNameNameStartsWith(name);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(redisRepository).searchByNameNameStartsWith(name);
        verify(sqlRepository).searchByNameNameStartsWith(name);

        // Verify each author was cached individually
        verify(redisRepository).save(author1);
        verify(redisRepository).save(author2);
    }

    @Test
    void testSearchByNameNameStartsWith_cacheIndividualFailures_shouldContinue() {
        // Arrange
        String name = "John";

        Author author1 = new Author("John Doe", "Bio 1", null);
        author1.setAuthorNumber(1L);
        Author author2 = new Author("John Smith", "Bio 2", null);
        author2.setAuthorNumber(2L);

        when(redisRepository.searchByNameNameStartsWith(name))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.searchByNameNameStartsWith(name))
                .thenReturn(Arrays.asList(author1, author2));

        // First save succeeds, second fails
        when(redisRepository.save(author1)).thenReturn(author1);
        when(redisRepository.save(author2)).thenThrow(new RuntimeException("Cache failed"));

        // Act
        List<Author> result = cacheRepository.searchByNameNameStartsWith(name);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Both cache attempts should have been made
        verify(redisRepository).save(author1);
        verify(redisRepository).save(author2);
    }

    // ========================================
    // SEARCH BY EXACT NAME - Cache Pattern
    // ========================================

    @Test
    void testSearchByNameName_cacheHit_shouldReturnFromCache() {
        // Arrange
        String name = "John Doe";
        when(redisRepository.searchByNameName(name))
                .thenReturn(Arrays.asList(testAuthor));

        // Act
        List<Author> result = cacheRepository.searchByNameName(name);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(redisRepository).searchByNameName(name);
        verify(sqlRepository, never()).searchByNameName(any());
    }

    @Test
    void testSearchByNameName_cacheMiss_shouldFetchFromSQL() {
        // Arrange
        String name = "John Doe";
        when(redisRepository.searchByNameName(name))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.searchByNameName(name))
                .thenReturn(Arrays.asList(testAuthor));

        // Act
        List<Author> result = cacheRepository.searchByNameName(name);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(redisRepository).searchByNameName(name);
        verify(sqlRepository).searchByNameName(name);
        verify(redisRepository).save(testAuthor);
    }

    // ========================================
    // SAVE - Write-Through Pattern
    // ========================================

    @Test
    void testSave_shouldWriteToSQLFirstThenUpdateCache() {
        // Arrange
        when(sqlRepository.save(testAuthor)).thenReturn(testAuthor);
        when(redisRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = cacheRepository.save(testAuthor);

        // Assert
        assertNotNull(result);
        assertEquals(testAuthor, result);

        // Verify order: SQL first, then cache
        var inOrder = inOrder(sqlRepository, redisRepository);
        inOrder.verify(sqlRepository).save(testAuthor);
        inOrder.verify(redisRepository).save(testAuthor);
    }

    @Test
    void testSave_cacheFails_shouldStillSucceed() {
        // Arrange
        when(sqlRepository.save(testAuthor)).thenReturn(testAuthor);
        when(redisRepository.save(any())).thenThrow(new RuntimeException("Cache failed"));

        // Act
        Author result = cacheRepository.save(testAuthor);

        // Assert
        // Save should succeed even if cache fails
        assertNotNull(result);
        assertEquals(testAuthor, result);

        verify(sqlRepository).save(testAuthor);
        verify(redisRepository).save(testAuthor);
    }

    @Test
    void testSave_shouldReturnSQLVersion() {
        // Arrange
        Author savedAuthor = new Author("John Doe", "Bio", null);
        savedAuthor.setAuthorNumber(1L);
        savedAuthor.setVersion(2L); // SQL incremented version

        when(sqlRepository.save(testAuthor)).thenReturn(savedAuthor);
        when(redisRepository.save(savedAuthor)).thenReturn(savedAuthor);

        // Act
        Author result = cacheRepository.save(testAuthor);

        // Assert
        assertEquals(savedAuthor, result);
        assertEquals(2L, result.getVersion());
    }

    // ========================================
    // DELETE - Cache Invalidation
    // ========================================

    @Test
    void testDelete_shouldDeleteFromSQLThenInvalidateCache() {
        // Arrange
        doNothing().when(sqlRepository).delete(testAuthor);
        doNothing().when(redisRepository).delete(testAuthor);

        // Act
        cacheRepository.delete(testAuthor);

        // Assert
        // Verify order: SQL first, then cache invalidation
        var inOrder = inOrder(sqlRepository, redisRepository);
        inOrder.verify(sqlRepository).delete(testAuthor);
        inOrder.verify(redisRepository).delete(testAuthor);
    }

    @Test
    void testDelete_cacheInvalidationFails_shouldStillSucceed() {
        // Arrange
        doNothing().when(sqlRepository).delete(testAuthor);
        doThrow(new RuntimeException("Cache failed")).when(redisRepository).delete(any());

        // Act & Assert
        // Should not throw exception
        assertDoesNotThrow(() -> cacheRepository.delete(testAuthor));

        verify(sqlRepository).delete(testAuthor);
        verify(redisRepository).delete(testAuthor);
    }

    // ========================================
    // FIND ALL - Always SQL (Heavy Operation)
    // ========================================

    @Test
    void testFindAll_shouldAlwaysQuerySQL() {
        // Arrange
        List<Author> allAuthors = Arrays.asList(testAuthor);
        when(sqlRepository.findAll()).thenReturn(allAuthors);

        // Act
        Iterable<Author> result = cacheRepository.findAll();

        // Assert
        assertNotNull(result);

        // Verify only SQL was queried (heavy operation)
        verify(sqlRepository).findAll();
        verify(redisRepository, never()).findAll();
    }

    // ========================================
    // TOP AUTHORS - Always SQL (Complex Query)
    // ========================================

    @Test
    void testFindTopAuthorByLendings_shouldAlwaysQuerySQL() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        AuthorLendingView view = new AuthorLendingView("John Doe", 10L);
        Page<AuthorLendingView> page = new PageImpl<>(Arrays.asList(view));

        when(sqlRepository.findTopAuthorByLendings(pageable)).thenReturn(page);

        // Act
        Page<AuthorLendingView> result = cacheRepository.findTopAuthorByLendings(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify only SQL was queried (aggregation)
        verify(sqlRepository).findTopAuthorByLendings(pageable);
        verify(redisRepository, never()).findTopAuthorByLendings(any());
    }

    // ========================================
    // CO-AUTHORS - Always SQL (Complex Relationship)
    // ========================================

    @Test
    void testFindCoAuthorsByAuthorNumber_shouldAlwaysQuerySQL() {
        // Arrange
        Long authorNumber = 1L;
        Author coAuthor = new Author("Jane Smith", "Co-author", null);
        coAuthor.setAuthorNumber(2L);

        when(sqlRepository.findCoAuthorsByAuthorNumber(authorNumber))
                .thenReturn(Arrays.asList(coAuthor));

        // Act
        List<Author> result = cacheRepository.findCoAuthorsByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify only SQL was queried (complex relationship)
        verify(sqlRepository).findCoAuthorsByAuthorNumber(authorNumber);
        verify(redisRepository, never()).findCoAuthorsByAuthorNumber(any());
    }

    // ========================================
    // EDGE CASES & ERROR SCENARIOS
    // ========================================

    @Test
    void testFindByAuthorNumber_cacheThrowsOnRead_shouldPropagateException() {
        // Arrange
        Long authorNumber = 1L;

        // Cache throws exception on read
        when(redisRepository.findByAuthorNumber(authorNumber))
                .thenThrow(new RuntimeException("Cache read failed"));

        // Act & Assert
        // Exception should propagate (no fallback in current implementation)
        assertThrows(RuntimeException.class,
                () -> cacheRepository.findByAuthorNumber(authorNumber)
        );

        verify(redisRepository).findByAuthorNumber(authorNumber);
        // SQL is never called because exception was thrown
        verify(sqlRepository, never()).findByAuthorNumber(any());
    }

    @Test
    void testSave_multipleSequentialSaves_shouldUpdateCacheEachTime() {
        // Arrange
        when(sqlRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(redisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        cacheRepository.save(testAuthor);
        cacheRepository.save(testAuthor);
        cacheRepository.save(testAuthor);

        // Assert
        verify(sqlRepository, times(3)).save(testAuthor);
        verify(redisRepository, times(3)).save(testAuthor);
    }

    @Test
    void testCacheAside_readAfterWrite_shouldFindInCache() {
        // Arrange
        Long authorNumber = 1L;
        testAuthor.setAuthorNumber(authorNumber);

        // Setup save
        when(sqlRepository.save(testAuthor)).thenReturn(testAuthor);
        when(redisRepository.save(testAuthor)).thenReturn(testAuthor);

        // Setup read (cache hit after save)
        when(redisRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));

        // Act
        cacheRepository.save(testAuthor);
        Optional<Author> result = cacheRepository.findByAuthorNumber(authorNumber);

        // Assert
        assertTrue(result.isPresent());

        // Verify cache was hit on read
        verify(redisRepository).findByAuthorNumber(authorNumber);
        verify(sqlRepository, never()).findByAuthorNumber(authorNumber);
    }

    @Test
    void testDelete_thenRead_shouldNotFindInCache() {
        // Arrange
        Long authorNumber = 1L;
        testAuthor.setAuthorNumber(authorNumber);

        // Setup delete
        doNothing().when(sqlRepository).delete(testAuthor);
        doNothing().when(redisRepository).delete(testAuthor);

        // Setup read after delete (cache miss)
        when(redisRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());
        when(sqlRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());

        // Act
        cacheRepository.delete(testAuthor);
        Optional<Author> result = cacheRepository.findByAuthorNumber(authorNumber);

        // Assert
        assertFalse(result.isPresent());

        // Verify went to SQL after cache miss
        verify(sqlRepository).findByAuthorNumber(authorNumber);
    }
}