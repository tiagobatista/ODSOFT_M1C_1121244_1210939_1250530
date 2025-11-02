package pt.psoft.g1.psoftg1.bookmanagement.model.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Redis.BookCacheRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration Test for BookCacheRepository (Cache-Aside Pattern)
 *
 * Purpose: Test the Cache-Aside pattern implementation for Books
 * Testing Strategy: Mock both Redis and SQL repositories, verify cache behavior
 * SUT: BookCacheRepository (integration between cache and source)
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
 * - Secondary index queries (genre, author, title)
 *
 * @author ARQSOFT 2025-2026
 */
@ExtendWith(MockitoExtension.class)
class BookCacheRepositoryIntegrationTest {

    @Mock
    private BookRepository redisRepository;

    @Mock
    private BookRepository sqlRepository;

    private BookCacheRepository cacheRepository;

    private Book testBook;
    private Genre testGenre;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        cacheRepository = new BookCacheRepository(redisRepository, sqlRepository);

        testGenre = new Genre(1L, "Fiction");

        testAuthor = new Author("John Doe", "Famous author", null);
        testAuthor.setAuthorNumber(1L);

        testBook = new Book(
                "9780306406157",
                "Test Book",
                "Test description",
                testGenre,
                Arrays.asList(testAuthor),
                null
        );
        testBook.pk = 1L;
    }

    // ========================================
    // FIND BY ISBN - Cache-Aside Read Pattern
    // ========================================

    @Test
    void testFindByIsbn_cacheHit_shouldReturnFromCacheWithoutQueryingSQL() {
        // Arrange
        String isbn = "9780306406157";
        when(redisRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(testBook));

        // Act
        Optional<Book> result = cacheRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());

        // Verify cache was checked
        verify(redisRepository).findByIsbn(isbn);

        // Verify SQL was NOT queried (cache hit)
        verify(sqlRepository, never()).findByIsbn(any());

        // Verify cache was NOT updated (already had data)
        verify(redisRepository, never()).save(any());
    }

    @Test
    void testFindByIsbn_cacheMiss_shouldFetchFromSQLAndUpdateCache() {
        // Arrange
        String isbn = "9780306406157";

        // Cache miss - Redis returns empty
        when(redisRepository.findByIsbn(isbn))
                .thenReturn(Optional.empty());

        // SQL has the data
        when(sqlRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(testBook));

        // Cache save succeeds
        when(redisRepository.save(testBook)).thenReturn(testBook);

        // Act
        Optional<Book> result = cacheRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());

        // Verify cache was checked first
        verify(redisRepository).findByIsbn(isbn);

        // Verify SQL was queried (cache miss)
        verify(sqlRepository).findByIsbn(isbn);

        // Verify cache was updated with SQL data
        verify(redisRepository).save(testBook);
    }

    @Test
    void testFindByIsbn_cacheMissAndSQLMiss_shouldReturnEmpty() {
        // Arrange
        String isbn = "9999999999999";

        when(redisRepository.findByIsbn(isbn))
                .thenReturn(Optional.empty());
        when(sqlRepository.findByIsbn(isbn))
                .thenReturn(Optional.empty());

        // Act
        Optional<Book> result = cacheRepository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());

        verify(redisRepository).findByIsbn(isbn);
        verify(sqlRepository).findByIsbn(isbn);

        // Verify cache was NOT updated (no data to cache)
        verify(redisRepository, never()).save(any());
    }

    @Test
    void testFindByIsbn_cacheSaveFails_shouldStillReturnResult() {
        // Arrange
        String isbn = "9780306406157";

        when(redisRepository.findByIsbn(isbn))
                .thenReturn(Optional.empty());
        when(sqlRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(testBook));

        // Cache save throws exception
        when(redisRepository.save(any())).thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        Optional<Book> result = cacheRepository.findByIsbn(isbn);

        // Assert
        // Operation should still succeed even if cache fails
        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());

        verify(sqlRepository).findByIsbn(isbn);
        verify(redisRepository).save(testBook);
    }

    // ========================================
    // FIND BY TITLE - Cache Pattern
    // ========================================

    @Test
    void testFindByTitle_cacheHit_shouldReturnFromCache() {
        // Arrange
        String title = "Test Book";
        List<Book> cachedBooks = Arrays.asList(testBook);

        when(redisRepository.findByTitle(title))
                .thenReturn(cachedBooks);

        // Act
        List<Book> result = cacheRepository.findByTitle(title);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));

        verify(redisRepository).findByTitle(title);
        verify(sqlRepository, never()).findByTitle(any());
    }

    @Test
    void testFindByTitle_cacheMiss_shouldFetchFromSQLAndCacheEach() {
        // Arrange
        String title = "Test Book";

        Book book1 = new Book("9780306406157", "Test Book", "Desc 1", testGenre, Arrays.asList(testAuthor), null);
        book1.pk = 1L;
        Book book2 = new Book("9780471958697", "Test Book 2", "Desc 2", testGenre, Arrays.asList(testAuthor), null);
        book2.pk = 2L;

        List<Book> sqlBooks = Arrays.asList(book1, book2);

        when(redisRepository.findByTitle(title))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.findByTitle(title))
                .thenReturn(sqlBooks);
        when(redisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        List<Book> result = cacheRepository.findByTitle(title);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(redisRepository).findByTitle(title);
        verify(sqlRepository).findByTitle(title);

        // Verify each book was cached individually
        verify(redisRepository).save(book1);
        verify(redisRepository).save(book2);
    }

    @Test
    void testFindByTitle_cacheIndividualFailures_shouldContinue() {
        // Arrange
        String title = "Test Book";

        Book book1 = new Book("9780306406157", "Test Book", "Desc", testGenre, Arrays.asList(testAuthor), null);
        book1.pk = 1L;
        Book book2 = new Book("9780471958697", "Test Book 2", "Desc", testGenre, Arrays.asList(testAuthor), null);
        book2.pk = 2L;

        when(redisRepository.findByTitle(title))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.findByTitle(title))
                .thenReturn(Arrays.asList(book1, book2));

        // First save succeeds, second fails
        when(redisRepository.save(book1)).thenReturn(book1);
        when(redisRepository.save(book2)).thenThrow(new RuntimeException("Cache failed"));

        // Act
        List<Book> result = cacheRepository.findByTitle(title);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Both cache attempts should have been made
        verify(redisRepository).save(book1);
        verify(redisRepository).save(book2);
    }

    // ========================================
    // FIND BY GENRE - Cache Pattern
    // ========================================

    @Test
    void testFindByGenre_cacheHit_shouldReturnFromCache() {
        // Arrange
        String genre = "Fiction";
        when(redisRepository.findByGenre(genre))
                .thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = cacheRepository.findByGenre(genre);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(redisRepository).findByGenre(genre);
        verify(sqlRepository, never()).findByGenre(any());
    }

    @Test
    void testFindByGenre_cacheMiss_shouldFetchFromSQL() {
        // Arrange
        String genre = "Fiction";
        when(redisRepository.findByGenre(genre))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.findByGenre(genre))
                .thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = cacheRepository.findByGenre(genre);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(redisRepository).findByGenre(genre);
        verify(sqlRepository).findByGenre(genre);
        verify(redisRepository).save(testBook);
    }

    // ========================================
    // FIND BY AUTHOR NAME - Cache Pattern
    // ========================================

    @Test
    void testFindByAuthorName_cacheHit_shouldReturnFromCache() {
        // Arrange
        String authorName = "John Doe%";
        when(redisRepository.findByAuthorName(authorName))
                .thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = cacheRepository.findByAuthorName(authorName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(redisRepository).findByAuthorName(authorName);
        verify(sqlRepository, never()).findByAuthorName(any());
    }

    @Test
    void testFindByAuthorName_cacheMiss_shouldFetchFromSQL() {
        // Arrange
        String authorName = "John Doe%";
        when(redisRepository.findByAuthorName(authorName))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.findByAuthorName(authorName))
                .thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = cacheRepository.findByAuthorName(authorName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(sqlRepository).findByAuthorName(authorName);
        verify(redisRepository).save(testBook);
    }

    // ========================================
    // FIND BOOKS BY AUTHOR NUMBER
    // ========================================

    @Test
    void testFindBooksByAuthorNumber_cacheHit_shouldReturnFromCache() {
        // Arrange
        Long authorNumber = 1L;
        when(redisRepository.findBooksByAuthorNumber(authorNumber))
                .thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = cacheRepository.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(redisRepository).findBooksByAuthorNumber(authorNumber);
        verify(sqlRepository, never()).findBooksByAuthorNumber(any());
    }

    @Test
    void testFindBooksByAuthorNumber_cacheMiss_shouldFetchFromSQL() {
        // Arrange
        Long authorNumber = 1L;
        when(redisRepository.findBooksByAuthorNumber(authorNumber))
                .thenReturn(Collections.emptyList());
        when(sqlRepository.findBooksByAuthorNumber(authorNumber))
                .thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = cacheRepository.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(sqlRepository).findBooksByAuthorNumber(authorNumber);
    }

    // ========================================
    // SAVE - Write-Through Pattern
    // ========================================

    @Test
    void testSave_shouldWriteToSQLFirstThenUpdateCache() {
        // Arrange
        when(sqlRepository.save(testBook)).thenReturn(testBook);
        when(redisRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = cacheRepository.save(testBook);

        // Assert
        assertNotNull(result);
        assertEquals(testBook, result);

        // Verify order: SQL first, then cache
        var inOrder = inOrder(sqlRepository, redisRepository);
        inOrder.verify(sqlRepository).save(testBook);
        inOrder.verify(redisRepository).save(testBook);
    }

    @Test
    void testSave_cacheFails_shouldStillSucceed() {
        // Arrange
        when(sqlRepository.save(testBook)).thenReturn(testBook);
        when(redisRepository.save(any())).thenThrow(new RuntimeException("Cache failed"));

        // Act
        Book result = cacheRepository.save(testBook);

        // Assert
        // Save should succeed even if cache fails
        assertNotNull(result);
        assertEquals(testBook, result);

        verify(sqlRepository).save(testBook);
        verify(redisRepository).save(testBook);
    }

    @Test
    void testSave_shouldReturnSQLVersion() {
        // Arrange
        Book savedBook = new Book("9780306406157", "Test", "Desc", testGenre, Arrays.asList(testAuthor), null);
        savedBook.pk = 1L;

        when(sqlRepository.save(testBook)).thenReturn(savedBook);
        when(redisRepository.save(savedBook)).thenReturn(savedBook);

        // Act
        Book result = cacheRepository.save(testBook);

        // Assert
        assertEquals(savedBook, result);
    }

    // ========================================
    // DELETE - Cache Invalidation
    // ========================================

    @Test
    void testDelete_shouldDeleteFromSQLThenInvalidateCache() {
        // Arrange
        doNothing().when(sqlRepository).delete(testBook);
        doNothing().when(redisRepository).delete(testBook);

        // Act
        cacheRepository.delete(testBook);

        // Assert
        // Verify order: SQL first, then cache invalidation
        var inOrder = inOrder(sqlRepository, redisRepository);
        inOrder.verify(sqlRepository).delete(testBook);
        inOrder.verify(redisRepository).delete(testBook);
    }

    @Test
    void testDelete_cacheInvalidationFails_shouldStillSucceed() {
        // Arrange
        doNothing().when(sqlRepository).delete(testBook);
        doThrow(new RuntimeException("Cache failed")).when(redisRepository).delete(any());

        // Act & Assert
        // Should not throw exception
        assertDoesNotThrow(() -> cacheRepository.delete(testBook));

        verify(sqlRepository).delete(testBook);
        verify(redisRepository).delete(testBook);
    }

    // ========================================
    // TOP 5 BOOKS LENT - Always SQL
    // ========================================

    @Test
    void testFindTop5BooksLent_shouldAlwaysQuerySQL() {
        // Arrange
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        Pageable pageable = PageRequest.of(0, 5);

        BookCountDTO dto = mock(BookCountDTO.class);
        Page<BookCountDTO> page = new PageImpl<>(Arrays.asList(dto));

        when(sqlRepository.findTop5BooksLent(oneYearAgo, pageable)).thenReturn(page);

        // Act
        Page<BookCountDTO> result = cacheRepository.findTop5BooksLent(oneYearAgo, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify only SQL was queried (aggregation)
        verify(sqlRepository).findTop5BooksLent(oneYearAgo, pageable);
        verify(redisRepository, never()).findTop5BooksLent(any(), any());
    }

    // ========================================
    // SEARCH BOOKS - Always SQL
    // ========================================

    @Test
    void testSearchBooks_shouldAlwaysQuerySQL() {
        // Arrange
        pt.psoft.g1.psoftg1.shared.services.Page page =
                new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);
        SearchBooksQuery query = new SearchBooksQuery("Test", "Fiction", "John");

        when(sqlRepository.searchBooks(page, query))
                .thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = cacheRepository.searchBooks(page, query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify only SQL was queried (complex query)
        verify(sqlRepository).searchBooks(page, query);
        verify(redisRepository, never()).searchBooks(any(), any());
    }

    // ========================================
    // EDGE CASES & ERROR SCENARIOS
    // ========================================

    @Test
    void testSave_multipleSequentialSaves_shouldUpdateCacheEachTime() {
        // Arrange
        when(sqlRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(redisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        cacheRepository.save(testBook);
        cacheRepository.save(testBook);
        cacheRepository.save(testBook);

        // Assert
        verify(sqlRepository, times(3)).save(testBook);
        verify(redisRepository, times(3)).save(testBook);
    }

    @Test
    void testCacheAside_readAfterWrite_shouldFindInCache() {
        // Arrange
        String isbn = "9780306406157";

        // Setup save
        when(sqlRepository.save(testBook)).thenReturn(testBook);
        when(redisRepository.save(testBook)).thenReturn(testBook);

        // Setup read (cache hit after save)
        when(redisRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(testBook));

        // Act
        cacheRepository.save(testBook);
        Optional<Book> result = cacheRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());

        // Verify cache was hit on read
        verify(redisRepository).findByIsbn(isbn);
        verify(sqlRepository, never()).findByIsbn(isbn);
    }

    @Test
    void testDelete_thenRead_shouldNotFindInCache() {
        // Arrange
        String isbn = "9780306406157";

        // Setup delete
        doNothing().when(sqlRepository).delete(testBook);
        doNothing().when(redisRepository).delete(testBook);

        // Setup read after delete (cache miss)
        when(redisRepository.findByIsbn(isbn))
                .thenReturn(Optional.empty());
        when(sqlRepository.findByIsbn(isbn))
                .thenReturn(Optional.empty());

        // Act
        cacheRepository.delete(testBook);
        Optional<Book> result = cacheRepository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());

        // Verify went to SQL after cache miss
        verify(sqlRepository).findByIsbn(isbn);
    }
}