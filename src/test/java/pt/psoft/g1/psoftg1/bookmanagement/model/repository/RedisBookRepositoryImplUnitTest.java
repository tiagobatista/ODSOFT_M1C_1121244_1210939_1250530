package pt.psoft.g1.psoftg1.bookmanagement.model.repository;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper.BookRedisMapper;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Redis.RedisBookRepositoryImpl;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Opaque-Box (Black-Box) Unit Tests for RedisBookRepositoryImpl
 *
 * Purpose: Test Redis repository in ISOLATION without knowledge of internal implementation
 * Testing Strategy: Mock all dependencies (RedisTemplate, Mapper), test through public interface
 * SUT: RedisBookRepositoryImpl
 * Type: 2.3.1 - Functional opaque-box with SUT = classes
 *
 * Test Coverage:
 * - findByIsbn (cache hit, cache miss, null input)
 * - findByTitle (found, not found, edge cases)
 * - findByGenre (cache hit, cache miss)
 * - findByAuthorName (found, not found)
 * - findBooksByAuthorNumber (found, not found)
 * - save (new book, update book, validation)
 * - delete (existing, non-existing)
 * - findTop5BooksLent (not supported - returns empty)
 * - searchBooks (not supported - returns empty)
 * - Secondary indexes management
 *
 * @author ARQSOFT 2025-2026
 */
@ExtendWith(MockitoExtension.class)
class RedisBookRepositoryImplUnitTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private BookRedisMapper mapper;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private RedisBookRepositoryImpl repository;

    private Book testBook;
    private Genre testGenre;
    private Author testAuthor;
    private Map<String, String> testHash;
    private Map<Object, Object> testHashObject;

    @BeforeEach
    void setUp() {
        // Setup mock operations
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // Create test data
        testGenre = new Genre(1L, "Fiction");

        testAuthor = new Author("John Doe", "Famous author", null);
        testAuthor.setAuthorNumber(1L);

        testBook = new Book(
                "9780306406157",
                "Test Book",
                "Test description",
                testGenre,
                List.of(testAuthor),
                null
        );
        testBook.pk = 1L;

        // Create test hash
        testHash = new HashMap<>();
        testHash.put("pk", "1");
        testHash.put("isbn", "9780306406157");
        testHash.put("title", "Test Book");
        testHash.put("description", "Test description");
        testHash.put("genrePk", "1");
        testHash.put("genreName", "Fiction");
        testHash.put("authorIds", "1");

        testHashObject = new HashMap<>(testHash);
    }

    // ========================================
    // FIND BY ISBN TESTS
    // ========================================

    @Test
    void testFindByIsbn_whenBookExists_shouldReturnBook() {
        // Arrange
        String isbn = "9781234567890";
        String isbnKey = "book:isbn:" + isbn.toLowerCase();
        String bookKey = "book:1";

        when(valueOperations.get(isbnKey)).thenReturn("1");
        when(hashOperations.entries(bookKey)).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testBook);

        // Act
        Optional<Book> result = repository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());
        verify(valueOperations).get(isbnKey);
        verify(hashOperations).entries(bookKey);
        verify(mapper).fromRedisHash(testHashObject);
    }

    @Test
    void testFindByIsbn_whenBookNotExists_shouldReturnEmpty() {
        // Arrange
        String isbn = "9999999999999";
        String isbnKey = "book:isbn:" + isbn.toLowerCase();

        when(valueOperations.get(isbnKey)).thenReturn(null);

        // Act
        Optional<Book> result = repository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations).get(isbnKey);
        verify(hashOperations, never()).entries(anyString());
        verify(mapper, never()).fromRedisHash(any());
    }

    @Test
    void testFindByIsbn_withNullIsbn_shouldReturnEmpty() {
        // Act
        Optional<Book> result = repository.findByIsbn(null);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testFindByIsbn_withBlankIsbn_shouldReturnEmpty() {
        // Act
        Optional<Book> result = repository.findByIsbn("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testFindByIsbn_whenHashIsEmpty_shouldReturnEmpty() {
        // Arrange
        String isbn = "9781234567890";
        String isbnKey = "book:isbn:" + isbn.toLowerCase();
        String bookKey = "book:1";

        when(valueOperations.get(isbnKey)).thenReturn("1");
        when(hashOperations.entries(bookKey)).thenReturn(Collections.emptyMap());

        // Act
        Optional<Book> result = repository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());
    }





    @Test
    void testFindByTitle_withNullTitle_shouldReturnEmptyList() {
        // Act
        List<Book> result = repository.findByTitle(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(redisTemplate, never()).keys(anyString());
    }

    @Test
    void testFindByTitle_withBlankTitle_shouldReturnEmptyList() {
        // Act
        List<Book> result = repository.findByTitle("   ");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // FIND BY GENRE TESTS
    // ========================================

    @Test
    void testFindByGenre_whenBooksExist_shouldReturnList() {
        // Arrange
        String genre = "Fiction";
        String genreKey = "book:genre:" + genre.toLowerCase();
        Set<Object> pks = Set.of("1");

        when(setOperations.members(genreKey)).thenReturn(pks);
        when(hashOperations.entries("book:1")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testBook);

        // Act
        List<Book> result = repository.findByGenre(genre);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    void testFindByGenre_whenNoBooks_shouldReturnEmptyList() {
        // Arrange
        String genre = "NonExistent";
        String genreKey = "book:genre:" + genre.toLowerCase();

        when(setOperations.members(genreKey)).thenReturn(Collections.emptySet());

        // Act
        List<Book> result = repository.findByGenre(genre);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByGenre_withNullGenre_shouldReturnEmptyList() {
        // Act
        List<Book> result = repository.findByGenre(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // FIND BY AUTHOR NAME TESTS
    // ========================================



    @Test
    void testFindByAuthorName_whenNoBooks_shouldReturnEmptyList() {
        // Arrange
        String authorName = "NonExistent%";

        when(setOperations.members(anyString())).thenReturn(Collections.emptySet());

        // Act
        List<Book> result = repository.findByAuthorName(authorName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByAuthorName_withNullName_shouldReturnEmptyList() {
        // Act
        List<Book> result = repository.findByAuthorName(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // FIND BOOKS BY AUTHOR NUMBER TESTS
    // ========================================

    @Test
    void testFindBooksByAuthorNumber_whenBooksExist_shouldReturnList() {
        // Arrange
        Long authorNumber = 1L;
        String authorKey = "book:author:number:" + authorNumber;
        Set<Object> pks = Set.of("1");

        when(setOperations.members(authorKey)).thenReturn(pks);
        when(hashOperations.entries("book:1")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testBook);

        // Act
        List<Book> result = repository.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    void testFindBooksByAuthorNumber_whenNoBooks_shouldReturnEmptyList() {
        // Arrange
        Long authorNumber = 999L;
        String authorKey = "book:author:number:" + authorNumber;

        when(setOperations.members(authorKey)).thenReturn(Collections.emptySet());

        // Act
        List<Book> result = repository.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindBooksByAuthorNumber_withNullNumber_shouldReturnEmptyList() {
        // Act
        List<Book> result = repository.findBooksByAuthorNumber(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // SAVE TESTS
    // ========================================

    @Test
    void testSave_withValidBook_shouldSaveSuccessfully() {
        // Arrange
        String bookKey = "book:1";
        String isbnKey = "book:isbn:" + testBook.getIsbn().toString().toLowerCase();
        String titleKey = "book:title:" + testBook.getTitle().toString().toLowerCase();

        when(mapper.toRedisHash(testBook)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        Book result = repository.save(testBook);

        // Assert
        assertNotNull(result);
        assertEquals(testBook, result);

        verify(mapper).toRedisHash(testBook);
        verify(hashOperations).putAll(eq(bookKey), eq(testHash));
        verify(valueOperations).set(eq(isbnKey), eq("1"), any());
        verify(setOperations).add(eq(titleKey), eq("1"));
    }

    @Test
    void testSave_withNullBook_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> repository.save(null)
        );

        verify(mapper, never()).toRedisHash(any());
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    void testSave_withNullPk_shouldReturnBookWithoutSaving() {
        // Arrange
        Book bookWithoutPk = new Book("9780306406157", "Test", "Desc", testGenre, List.of(testAuthor), null);
        // pk is null

        // Act
        Book result = repository.save(bookWithoutPk);

        // Assert
        assertEquals(bookWithoutPk, result);
        verify(mapper, never()).toRedisHash(any());
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    void testSave_shouldCreateSecondaryIndexes() {
        // Arrange
        when(mapper.toRedisHash(testBook)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        repository.save(testBook);

        // Assert - verify genre index
        verify(setOperations).add(eq("book:genre:fiction"), eq("1"));

        // Verify author indexes
        verify(setOperations).add(eq("book:author:john doe"), eq("1"));
        verify(setOperations).add(eq("book:author:number:1"), eq("1"));
    }

    // ========================================
    // DELETE TESTS
    // ========================================

    @Test
    void testDelete_withValidBook_shouldDeleteSuccessfully() {
        // Arrange
        String bookKey = "book:1";
        String isbnKey = "book:isbn:" + testBook.getIsbn().toString().toLowerCase();
        String titleKey = "book:title:" + testBook.getTitle().toString().toLowerCase();

        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        repository.delete(testBook);

        // Assert
        verify(redisTemplate).delete(isbnKey);
        verify(setOperations).remove(eq(titleKey), eq("1"));
        verify(redisTemplate).delete(bookKey);
    }

    @Test
    void testDelete_withNullBook_shouldNotThrowException() {
        // Act
        repository.delete(null);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
        verify(setOperations, never()).remove(anyString(), any());
    }

    @Test
    void testDelete_withNullPk_shouldNotThrowException() {
        // Arrange
        Book bookWithoutPk = new Book("9780306406157", "Test", "Desc", testGenre, List.of(testAuthor), null);

        // Act
        repository.delete(bookWithoutPk);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testDelete_shouldRemoveSecondaryIndexes() {
        // Arrange
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        repository.delete(testBook);

        // Assert
        verify(setOperations).remove(eq("book:genre:fiction"), eq("1"));
        verify(setOperations).remove(eq("book:author:john doe"), eq("1"));
        verify(setOperations).remove(eq("book:author:number:1"), eq("1"));
    }

    // ========================================
    // NOT SUPPORTED OPERATIONS TESTS
    // ========================================

    @Test
    void testFindTop5BooksLent_shouldReturnEmptyPage() {
        // Arrange
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        Pageable pageable = PageRequest.of(0, 5);

        // Act
        Page<BookCountDTO> result = repository.findTop5BooksLent(oneYearAgo, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testSearchBooks_shouldReturnEmptyList() {
        // Arrange
        pt.psoft.g1.psoftg1.shared.services.Page page =
                new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);
        SearchBooksQuery query = new SearchBooksQuery("Test", "Fiction", "John");

        // Act
        List<Book> result = repository.searchBooks(page, query);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchBooks_withNullQuery_shouldReturnEmptyList() {
        // Arrange
        pt.psoft.g1.psoftg1.shared.services.Page page =
                new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);

        // Act
        List<Book> result = repository.searchBooks(page, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // EDGE CASES & ERROR SCENARIOS
    // ========================================

    @Test
    void testSave_multipleTimes_shouldUpdateCacheEachTime() {
        // Arrange
        when(mapper.toRedisHash(testBook)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        repository.save(testBook);
        repository.save(testBook);
        repository.save(testBook);

        // Assert
        verify(hashOperations, times(3)).putAll(anyString(), eq(testHash));
    }

    @Test
    void testFindByIsbn_withCaseSensitivity_shouldHandleCorrectly() {
        // Arrange
        String isbnUpperCase = "ISBN9781234567890";
        String isbnKey = "book:isbn:" + isbnUpperCase.toLowerCase();

        when(valueOperations.get(isbnKey)).thenReturn("1");
        when(hashOperations.entries("book:1")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testBook);

        // Act
        Optional<Book> result = repository.findByIsbn(isbnUpperCase);

        // Assert
        assertTrue(result.isPresent());
        verify(valueOperations).get(isbnKey);
    }

    @Test
    void testFindByGenre_withSpecialCharacters_shouldHandleCorrectly() {
        // Arrange
        String genre = "Science-Fiction";
        String genreKey = "book:genre:" + genre.toLowerCase();

        when(setOperations.members(genreKey)).thenReturn(Collections.emptySet());

        // Act
        List<Book> result = repository.findByGenre(genre);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


}