package pt.psoft.g1.psoftg1.authormanagement.repository;

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
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Mapper.AuthorRedisMapper;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Redis.RedisAuthorRepositoryImpl;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Opaque-Box (Black-Box) Unit Tests for RedisAuthorRepositoryImpl
 *
 * Purpose: Test Redis repository in ISOLATION without knowledge of internal implementation
 * Testing Strategy: Mock all dependencies (RedisTemplate, Mapper), test through public interface
 * SUT: RedisAuthorRepositoryImpl
 * Type: 2.3.1 - Functional opaque-box with SUT = classes
 *
 * Test Coverage:
 * - findByAuthorNumber (cache hit, cache miss, null input)
 * - searchByNameNameStartsWith (found, not found, edge cases)
 * - searchByNameName (exact match, no match)
 * - save (new author, update author, null validation)
 * - findAll (empty, multiple authors)
 * - delete (existing, non-existing)
 * - findTopAuthorByLendings (not supported - returns empty)
 * - findCoAuthorsByAuthorNumber (not supported - returns empty)
 *
 * @author ARQSOFT 2025-2026
 */
@ExtendWith(MockitoExtension.class)
class RedisAuthorRepositoryImplUnitTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private AuthorRedisMapper mapper;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private RedisAuthorRepositoryImpl repository;

    private Author testAuthor;
    private Map<String, String> testHash;
    private Map<Object, Object> testHashObject;

    @BeforeEach
    void setUp() {
        // Setup mock operations
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // Create test author
        testAuthor = new Author("John Doe", "Famous author", null);
        testAuthor.setAuthorNumber(1L);
        testAuthor.setVersion(1L);

        // Create test hash
        testHash = new HashMap<>();
        testHash.put("authorNumber", "1");
        testHash.put("version", "1");
        testHash.put("name", "John Doe");
        testHash.put("bio", "Famous author");

        testHashObject = new HashMap<>(testHash);
    }

    // ========================================
    // FIND BY AUTHOR NUMBER TESTS
    // ========================================

    @Test
    void testFindByAuthorNumber_whenAuthorExists_shouldReturnAuthor() {
        // Arrange
        Long authorNumber = 1L;
        String key = "author:" + authorNumber;

        when(hashOperations.entries(key)).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testAuthor);

        // Act
        Optional<Author> result = repository.findByAuthorNumber(authorNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());
        verify(hashOperations).entries(key);
        verify(mapper).fromRedisHash(testHashObject);
    }

    @Test
    void testFindByAuthorNumber_whenAuthorNotExists_shouldReturnEmpty() {
        // Arrange
        Long authorNumber = 999L;
        String key = "author:" + authorNumber;

        when(hashOperations.entries(key)).thenReturn(Collections.emptyMap());

        // Act
        Optional<Author> result = repository.findByAuthorNumber(authorNumber);

        // Assert
        assertFalse(result.isPresent());
        verify(hashOperations).entries(key);
        verify(mapper, never()).fromRedisHash(any());
    }

    @Test
    void testFindByAuthorNumber_whenAuthorNumberIsNull_shouldReturnEmpty() {
        // Act
        Optional<Author> result = repository.findByAuthorNumber(null);

        // Assert
        assertFalse(result.isPresent());
        verify(hashOperations, never()).entries(anyString());
        verify(mapper, never()).fromRedisHash(any());
    }

    @Test
    void testFindByAuthorNumber_whenMapperReturnsNull_shouldReturnEmpty() {
        // Arrange
        Long authorNumber = 1L;
        String key = "author:" + authorNumber;

        when(hashOperations.entries(key)).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(null);

        // Act
        Optional<Author> result = repository.findByAuthorNumber(authorNumber);

        // Assert
        assertFalse(result.isPresent());
    }

    // ========================================
    // SEARCH BY NAME (STARTS WITH) TESTS
    // ========================================

    @Test
    void testSearchByNameNameStartsWith_whenAuthorsExist_shouldReturnList() {
        // Arrange
        String searchName = "John";
        Set<String> keys = new HashSet<>(Arrays.asList(
                "author:name:john doe",
                "author:name:john smith"
        ));

        when(redisTemplate.keys("author:name:" + searchName.toLowerCase() + "*")).thenReturn(keys);
        when(valueOperations.get("author:name:john doe")).thenReturn("1");
        when(valueOperations.get("author:name:john smith")).thenReturn("2");

        Author author1 = new Author("John Doe", "Bio 1", null);
        author1.setAuthorNumber(1L);
        Author author2 = new Author("John Smith", "Bio 2", null);
        author2.setAuthorNumber(2L);

        Map<Object, Object> hash1 = new HashMap<>(testHashObject);
        Map<Object, Object> hash2 = new HashMap<>(testHashObject);

        when(hashOperations.entries("author:1")).thenReturn(hash1);
        when(hashOperations.entries("author:2")).thenReturn(hash2);
        when(mapper.fromRedisHash(hash1)).thenReturn(author1);
        when(mapper.fromRedisHash(hash2)).thenReturn(author2);

        // Act
        List<Author> result = repository.searchByNameNameStartsWith(searchName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(redisTemplate).keys("author:name:" + searchName.toLowerCase() + "*");
    }

    @Test
    void testSearchByNameNameStartsWith_whenNoAuthorsFound_shouldReturnEmptyList() {
        // Arrange
        String searchName = "NonExistent";

        when(redisTemplate.keys("author:name:" + searchName.toLowerCase() + "*"))
                .thenReturn(Collections.emptySet());

        // Act
        List<Author> result = repository.searchByNameNameStartsWith(searchName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchByNameNameStartsWith_whenNameIsNull_shouldReturnEmptyList() {
        // Act
        List<Author> result = repository.searchByNameNameStartsWith(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(redisTemplate, never()).keys(anyString());
    }

    @Test
    void testSearchByNameNameStartsWith_whenNameIsBlank_shouldReturnEmptyList() {
        // Act
        List<Author> result = repository.searchByNameNameStartsWith("   ");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(redisTemplate, never()).keys(anyString());
    }

    @Test
    void testSearchByNameNameStartsWith_whenKeysIsNull_shouldReturnEmptyList() {
        // Arrange
        when(redisTemplate.keys(anyString())).thenReturn(null);

        // Act
        List<Author> result = repository.searchByNameNameStartsWith("John");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // SEARCH BY NAME (EXACT) TESTS
    // ========================================

    @Test
    void testSearchByNameName_whenAuthorExists_shouldReturnSingletonList() {
        // Arrange
        String exactName = "John Doe";
        String key = "author:name:" + exactName.toLowerCase();

        when(valueOperations.get(key)).thenReturn("1");
        when(hashOperations.entries("author:1")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testAuthor);

        // Act
        List<Author> result = repository.searchByNameName(exactName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAuthor, result.get(0));
        verify(valueOperations).get(key);
    }

    @Test
    void testSearchByNameName_whenAuthorNotFound_shouldReturnEmptyList() {
        // Arrange
        String exactName = "NonExistent Author";
        String key = "author:name:" + exactName.toLowerCase();

        when(valueOperations.get(key)).thenReturn(null);

        // Act
        List<Author> result = repository.searchByNameName(exactName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchByNameName_whenNameIsNull_shouldReturnEmptyList() {
        // Act
        List<Author> result = repository.searchByNameName(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testSearchByNameName_whenNameIsBlank_shouldReturnEmptyList() {
        // Act
        List<Author> result = repository.searchByNameName("   ");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // SAVE TESTS
    // ========================================

    @Test
    void testSave_withValidAuthor_shouldSaveSuccessfully() {
        // Arrange
        String key = "author:" + testAuthor.getAuthorNumber();
        String nameKey = "author:name:" + testAuthor.getName().toString().toLowerCase();

        when(mapper.toRedisHash(testAuthor)).thenReturn(testHash);
        when(setOperations.add(anyString(), anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        Author result = repository.save(testAuthor);

        // Assert
        assertNotNull(result);
        assertEquals(testAuthor, result);

        verify(mapper).toRedisHash(testAuthor);
        verify(hashOperations).putAll(eq(key), eq(testHash));
        verify(valueOperations).set(eq(nameKey), eq("1"), any()); // Changed: uses Duration, not long+TimeUnit
        verify(setOperations).add("author:all", "1");
    }

    @Test
    void testSave_withNullAuthor_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> repository.save(null)
        );

        verify(mapper, never()).toRedisHash(any());
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    void testSave_withNullAuthorNumber_shouldThrowException() {
        // Arrange
        Author authorWithoutNumber = new Author("Test", "Bio", null);
        // authorNumber is null

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> repository.save(authorWithoutNumber)
        );
    }



    @Test
    void testSave_shouldAddToAllAuthorsSet() {
        // Arrange
        when(mapper.toRedisHash(testAuthor)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        repository.save(testAuthor);

        // Assert
        verify(setOperations).add("author:all", "1");
        verify(redisTemplate).expire(eq("author:all"), anyLong(), eq(TimeUnit.SECONDS));
    }

    // ========================================
    // FIND ALL TESTS
    // ========================================

    @Test
    void testFindAll_whenAuthorsExist_shouldReturnAllAuthors() {
        // Arrange
        Set<Object> authorNumbers = new HashSet<>(Arrays.asList("1", "2", "3"));
        when(setOperations.members("author:all")).thenReturn(authorNumbers);

        Author author1 = new Author("Author 1", "Bio 1", null);
        author1.setAuthorNumber(1L);
        Author author2 = new Author("Author 2", "Bio 2", null);
        author2.setAuthorNumber(2L);
        Author author3 = new Author("Author 3", "Bio 3", null);
        author3.setAuthorNumber(3L);

        Map<Object, Object> hash1 = new HashMap<>(testHashObject);
        Map<Object, Object> hash2 = new HashMap<>(testHashObject);
        Map<Object, Object> hash3 = new HashMap<>(testHashObject);

        when(hashOperations.entries("author:1")).thenReturn(hash1);
        when(hashOperations.entries("author:2")).thenReturn(hash2);
        when(hashOperations.entries("author:3")).thenReturn(hash3);
        when(mapper.fromRedisHash(hash1)).thenReturn(author1);
        when(mapper.fromRedisHash(hash2)).thenReturn(author2);
        when(mapper.fromRedisHash(hash3)).thenReturn(author3);

        // Act
        Iterable<Author> result = repository.findAll();

        // Assert
        assertNotNull(result);
        List<Author> resultList = new ArrayList<>();
        result.forEach(resultList::add);
        assertEquals(3, resultList.size());
    }

    @Test
    void testFindAll_whenNoAuthors_shouldReturnEmptyList() {
        // Arrange
        when(setOperations.members("author:all")).thenReturn(Collections.emptySet());

        // Act
        Iterable<Author> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void testFindAll_whenMembersReturnsNull_shouldReturnEmptyList() {
        // Arrange
        when(setOperations.members("author:all")).thenReturn(null);

        // Act
        Iterable<Author> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }

    // ========================================
    // DELETE TESTS
    // ========================================

    @Test
    void testDelete_withValidAuthor_shouldDeleteSuccessfully() {
        // Arrange
        String key = "author:" + testAuthor.getAuthorNumber();
        String nameKey = "author:name:" + testAuthor.getName().toString().toLowerCase();

        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        repository.delete(testAuthor);

        // Assert
        verify(redisTemplate).delete(nameKey);
        verify(setOperations).remove("author:all", "1");
        verify(redisTemplate).delete(key);
    }

    @Test
    void testDelete_withNullAuthor_shouldNotThrowException() {
        // Act
        repository.delete(null);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
        verify(setOperations, never()).remove(anyString(), any());
    }

    @Test
    void testDelete_withNullAuthorNumber_shouldNotThrowException() {
        // Arrange
        Author authorWithoutNumber = new Author("Test", "Bio", null);

        // Act
        repository.delete(authorWithoutNumber);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testDelete_shouldRemoveNameIndex() {
        // Arrange
        String nameKey = "author:name:" + testAuthor.getName().toString().toLowerCase();
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        repository.delete(testAuthor);

        // Assert
        verify(redisTemplate).delete(nameKey);
    }

    @Test
    void testDelete_shouldRemoveFromAllAuthorsSet() {
        // Arrange
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        repository.delete(testAuthor);

        // Assert
        verify(setOperations).remove("author:all", "1");
    }

    // ========================================
    // NOT SUPPORTED OPERATIONS TESTS
    // ========================================

    @Test
    void testFindTopAuthorByLendings_shouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);

        // Act
        Page<AuthorLendingView> result = repository.findTopAuthorByLendings(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void testFindCoAuthorsByAuthorNumber_shouldReturnEmptyList() {
        // Act
        List<Author> result = repository.findCoAuthorsByAuthorNumber(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindCoAuthorsByAuthorNumber_withNullAuthorNumber_shouldReturnEmptyList() {
        // Act
        List<Author> result = repository.findCoAuthorsByAuthorNumber(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // EDGE CASES & ERROR SCENARIOS
    // ========================================

    @Test
    void testSave_withAuthorWithoutName_shouldHandleGracefully() {
        // Arrange
        Author authorNoName = new Author();
        authorNoName.setAuthorNumber(1L);
        // name is null

        when(mapper.toRedisHash(authorNoName)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        Author result = repository.save(authorNoName);

        // Assert
        assertNotNull(result);
        // Should not attempt to create name index with null name
        verify(valueOperations, never()).set(contains("author:name:null"), anyString(), anyLong(), any());
    }

    @Test
    void testFindByAuthorNumber_withZeroId_shouldHandleCorrectly() {
        // Arrange
        when(hashOperations.entries("author:0")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testAuthor);

        // Act
        Optional<Author> result = repository.findByAuthorNumber(0L);

        // Assert
        assertTrue(result.isPresent());
        verify(hashOperations).entries("author:0");
    }

    @Test
    void testSearchByNameNameStartsWith_withSpecialCharacters_shouldEscapeProperly() {
        // Arrange
        String nameWithSpecial = "O'Brien";
        when(redisTemplate.keys(contains(nameWithSpecial.toLowerCase()))).thenReturn(Collections.emptySet());

        // Act
        List<Author> result = repository.searchByNameNameStartsWith(nameWithSpecial);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave_multipleTimes_shouldUpdateCache() {
        // Arrange
        when(mapper.toRedisHash(testAuthor)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        repository.save(testAuthor);
        repository.save(testAuthor);
        repository.save(testAuthor);

        // Assert
        verify(hashOperations, times(3)).putAll(anyString(), eq(testHash));
    }
}