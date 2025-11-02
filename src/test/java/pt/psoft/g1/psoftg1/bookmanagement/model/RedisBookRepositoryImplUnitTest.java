package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper.BookRedisMapper;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Redis.RedisBookRepositoryImpl;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = RedisBookRepositoryImpl
 *
 * Nomenclatura: *UnitTest para testes unitários
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private RedisBookRepositoryImpl redisRepository;

    private Book validBook;
    private Map<String, String> validBookHash;
    private Map<Object, Object> validBookHashObject;
    private Genre validGenre;
    private Author validAuthor;
    private List<Author> authors;

    @BeforeEach
    void setUp() {
        // Setup mocks para RedisTemplate operations
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // Setup Genre
        validGenre = new Genre("Fantasia");
        validGenre.setPk(1L);

        // Setup Author
        validAuthor = new Author("João Alberto", "Bio", null);
        validAuthor.setAuthorNumber(1L);
        authors = new ArrayList<>();
        authors.add(validAuthor);

        // Setup Book
        validBook = new Book("9782826012092", "Encantos de contar", "Descrição", validGenre, authors, null);
        validBook.pk = 1L;

        // Setup Redis Hash
        validBookHash = new HashMap<>();
        validBookHash.put("pk", "1");
        validBookHash.put("isbn", "9782826012092");
        validBookHash.put("title", "Encantos de contar");
        validBookHash.put("description", "Descrição");
        validBookHash.put("genrePk", "1");
        validBookHash.put("genreName", "Fantasia");
        validBookHash.put("authorIds", "1");

        validBookHashObject = new HashMap<>(validBookHash);
    }

    // ==================== FIND BY ISBN TESTS ====================

    @Test
    void ensureFindByIsbnReturnsBookWhenExists() {
        // Arrange
        String isbn = "9782826012092";
        String isbnKey = "book:isbn:" + isbn.toLowerCase();
        String bookKey = "book:1";

        when(valueOperations.get(isbnKey)).thenReturn("1");
        when(hashOperations.entries(bookKey)).thenReturn(validBookHashObject);
        when(mapper.fromRedisHash(validBookHashObject)).thenReturn(validBook);

        // Act
        Optional<Book> result = redisRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validBook, result.get());
        verify(valueOperations, times(1)).get(isbnKey);
        verify(hashOperations, times(1)).entries(bookKey);
        verify(mapper, times(1)).fromRedisHash(validBookHashObject);
    }

    @Test
    void ensureFindByIsbnReturnsEmptyWhenIsbnIndexNotFound() {
        // Arrange
        String isbn = "9999999999999";
        String isbnKey = "book:isbn:" + isbn.toLowerCase();

        when(valueOperations.get(isbnKey)).thenReturn(null);

        // Act
        Optional<Book> result = redisRepository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, times(1)).get(isbnKey);
        verify(hashOperations, never()).entries(anyString());
    }

    @Test
    void ensureFindByIsbnReturnsEmptyWhenBookHashNotFound() {
        // Arrange
        String isbn = "9782826012092";
        String isbnKey = "book:isbn:" + isbn.toLowerCase();
        String bookKey = "book:1";

        when(valueOperations.get(isbnKey)).thenReturn("1");
        when(hashOperations.entries(bookKey)).thenReturn(Collections.emptyMap());

        // Act
        Optional<Book> result = redisRepository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void ensureFindByIsbnReturnsEmptyWhenIsbnIsNull() {
        // Act
        Optional<Book> result = redisRepository.findByIsbn(null);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void ensureFindByIsbnReturnsEmptyWhenIsbnIsBlank() {
        // Act
        Optional<Book> result = redisRepository.findByIsbn("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    // ==================== FIND BY TITLE TESTS ====================

    @Test
    void ensureFindByTitleReturnsBooksWhenExists() {
        // Arrange
        String title = "encantos de contar";
        String titleKey = "book:title:" + title.toLowerCase();
        Set<Object> pks = Set.of("1");

        when(setOperations.members(titleKey)).thenReturn(pks);
        when(hashOperations.entries("book:1")).thenReturn(validBookHashObject);
        when(mapper.fromRedisHash(validBookHashObject)).thenReturn(validBook);

        // Act
        List<Book> results = redisRepository.findByTitle(title);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(validBook, results.get(0));
        verify(setOperations, times(1)).members(titleKey);
    }

    @Test
    void ensureFindByTitleReturnsEmptyWhenNoBooks() {
        // Arrange
        String title = "NonExistent";
        String titleKey = "book:title:" + title.toLowerCase();

        when(setOperations.members(titleKey)).thenReturn(Collections.emptySet());

        // Act
        List<Book> results = redisRepository.findByTitle(title);

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void ensureFindByTitleReturnsEmptyWhenTitleIsNull() {
        // Act
        List<Book> results = redisRepository.findByTitle(null);

        // Assert
        assertTrue(results.isEmpty());
        verify(setOperations, never()).members(anyString());
    }

    @Test
    void ensureFindByTitleReturnsEmptyWhenTitleIsBlank() {
        // Act
        List<Book> results = redisRepository.findByTitle("   ");

        // Assert
        assertTrue(results.isEmpty());
    }

    // ==================== SAVE TESTS ====================

    @Test
    void ensureSaveStoresBookInRedis() {
        // Arrange
        when(mapper.toRedisHash(validBook)).thenReturn(validBookHash);

        // Act
        Book result = redisRepository.save(validBook);

        // Assert
        assertNotNull(result);
        assertEquals(validBook, result);

        verify(hashOperations, times(1)).putAll(eq("book:1"), eq(validBookHash));
        verify(redisTemplate, times(1)).expire(eq("book:1"), anyLong(), any());
        verify(valueOperations, times(1)).set(eq("book:isbn:9782826012092"), eq("1"), any());
        verify(setOperations, times(1)).add(eq("book:title:encantos de contar"), eq("1"));
    }

    @Test
    void ensureSaveThrowsExceptionWhenBookIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> redisRepository.save(null));
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    void ensureSaveReturnsBookWhenPkIsNull() {
        // Arrange
        Book bookWithoutPk = new Book("9782826012092", "Title", "Desc", validGenre, authors, null);
        bookWithoutPk.pk = null;

        // Act
        Book result = redisRepository.save(bookWithoutPk);

        // Assert
        assertNotNull(result);
        assertEquals(bookWithoutPk, result);
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    // ==================== DELETE TESTS ====================

    @Test
    void ensureDeleteRemovesBookAndIndices() {
        // Act
        redisRepository.delete(validBook);

        // Assert
        verify(redisTemplate, times(1)).delete("book:1");
        verify(redisTemplate, times(1)).delete("book:isbn:9782826012092");
        verify(setOperations, times(1)).remove(eq("book:title:encantos de contar"), eq("1"));
        verify(setOperations, times(1)).remove(eq("book:genre:fantasia"), eq("1"));
        verify(setOperations, times(1)).remove(eq("book:author:number:1"), eq("1"));
    }

    @Test
    void ensureDeleteDoesNothingWhenBookIsNull() {
        // Act
        redisRepository.delete(null);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void ensureDeleteDoesNothingWhenPkIsNull() {
        // Arrange
        Book bookWithoutPk = new Book("9782826012092", "Title", "Desc", validGenre, authors, null);
        bookWithoutPk.pk = null;

        // Act
        redisRepository.delete(bookWithoutPk);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }
}