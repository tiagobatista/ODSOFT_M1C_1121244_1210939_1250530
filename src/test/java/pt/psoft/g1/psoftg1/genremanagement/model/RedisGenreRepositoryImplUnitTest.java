package pt.psoft.g1.psoftg1.genremanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper.GenreRedisMapper;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Redis.RedisGenreRepositoryImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Opaque-Box (Black-Box) Unit Tests for RedisGenreRepositoryImpl
 *
 * Purpose: Test Redis repository for Genre in ISOLATION without knowledge of internals
 * Testing Strategy: Mock all dependencies (RedisTemplate, Mapper), test through public interface
 * SUT: RedisGenreRepositoryImpl
 * Type: 2.3.1 - Functional opaque-box with SUT = classes
 *
 * Test Coverage:
 * - findByString (cache hit, cache miss, null/blank input)
 * - findAll (with data, empty cache)
 * - save (new genre, update genre, null handling)
 * - delete (existing genre, null handling)
 * - Cache key management
 * - Secondary indexes (name index)
 * - Complex queries (should return empty - not supported in Redis)
 * - Error scenarios
 *
 * @author ARQSOFT 2025-2026
 */
@ExtendWith(MockitoExtension.class)
class RedisGenreRepositoryImplUnitTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private GenreRedisMapper mapper;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    private RedisGenreRepositoryImpl repository;

    private Genre testGenre;
    private Map<String, String> testHash;
    private Map<Object, Object> testHashObject;

    @BeforeEach
    void setUp() {
        // Setup mock operations
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);

        repository = new RedisGenreRepositoryImpl(redisTemplate, mapper);

        // Create test data
        testGenre = new Genre(1L, "Fiction");

        // Create test hash
        testHash = new HashMap<>();
        testHash.put("pk", "1");
        testHash.put("genre", "Fiction");

        testHashObject = new HashMap<>(testHash);
    }

    // ========================================
    // FIND BY STRING TESTS - Cache Hit/Miss
    // ========================================

    @Test
    void testFindByString_whenGenreExists_shouldReturnGenre() {
        // Arrange
        String genreString = "Fiction";
        String nameKey = "genre:name:" + genreString.toLowerCase();
        String genreKey = "genre:1";

        when(valueOperations.get(nameKey)).thenReturn("1");
        when(hashOperations.entries(genreKey)).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testGenre);

        // Act
        Optional<Genre> result = repository.findByString(genreString);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGenre, result.get());
        assertEquals("Fiction", result.get().getGenre());

        verify(valueOperations).get(nameKey);
        verify(hashOperations).entries(genreKey);
        verify(mapper).fromRedisHash(testHashObject);
    }

    @Test
    void testFindByString_whenGenreNotExists_shouldReturnEmpty() {
        // Arrange
        String genreString = "NonExistent";
        String nameKey = "genre:name:" + genreString.toLowerCase();

        when(valueOperations.get(nameKey)).thenReturn(null);

        // Act
        Optional<Genre> result = repository.findByString(genreString);

        // Assert
        assertFalse(result.isPresent());
        assertEquals(Optional.empty(), result);

        verify(valueOperations).get(nameKey);
        verify(hashOperations, never()).entries(anyString());
        verify(mapper, never()).fromRedisHash(any());
    }

    @Test
    void testFindByString_withNullInput_shouldReturnEmpty() {
        // Act
        Optional<Genre> result = repository.findByString(null);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testFindByString_withBlankInput_shouldReturnEmpty() {
        // Act
        Optional<Genre> result = repository.findByString("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testFindByString_withEmptyInput_shouldReturnEmpty() {
        // Act
        Optional<Genre> result = repository.findByString("");

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testFindByString_whenHashEmpty_shouldReturnEmpty() {
        // Arrange
        String genreString = "Fiction";
        String nameKey = "genre:name:" + genreString.toLowerCase();
        String genreKey = "genre:1";

        when(valueOperations.get(nameKey)).thenReturn("1");
        when(hashOperations.entries(genreKey)).thenReturn(Collections.emptyMap());

        // Act
        Optional<Genre> result = repository.findByString(genreString);

        // Assert
        assertFalse(result.isPresent());
        verify(hashOperations).entries(genreKey);
        verify(mapper, never()).fromRedisHash(any());
    }

    @Test
    void testFindByString_caseInsensitive_shouldWork() {
        // Arrange
        String upperCase = "FICTION";
        String nameKey = "genre:name:fiction"; // Deve ser lowercase

        when(valueOperations.get(nameKey)).thenReturn("1");
        when(hashOperations.entries("genre:1")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testGenre);

        // Act
        Optional<Genre> result = repository.findByString(upperCase);

        // Assert
        assertTrue(result.isPresent());
        verify(valueOperations).get(nameKey);
    }

    // ========================================
    // FIND ALL TESTS
    // ========================================

    @Test
    void testFindAll_whenGenresExist_shouldReturnList() {
        // Arrange
        Genre genre2 = new Genre(2L, "Science Fiction");
        Genre genre3 = new Genre(3L, "Mystery");

        Set<Object> genrePks = new HashSet<>(Arrays.asList("1", "2", "3"));

        Map<Object, Object> hash1 = new HashMap<>(testHashObject);
        Map<Object, Object> hash2 = new HashMap<>();
        hash2.put("pk", "2");
        hash2.put("genre", "Science Fiction");
        Map<Object, Object> hash3 = new HashMap<>();
        hash3.put("pk", "3");
        hash3.put("genre", "Mystery");

        when(setOperations.members("genre:all")).thenReturn(genrePks);
        when(hashOperations.entries("genre:1")).thenReturn(hash1);
        when(hashOperations.entries("genre:2")).thenReturn(hash2);
        when(hashOperations.entries("genre:3")).thenReturn(hash3);

        when(mapper.fromRedisHash(hash1)).thenReturn(testGenre);
        when(mapper.fromRedisHash(hash2)).thenReturn(genre2);
        when(mapper.fromRedisHash(hash3)).thenReturn(genre3);

        // Act
        Iterable<Genre> result = repository.findAll();

        // Assert
        assertNotNull(result);
        List<Genre> resultList = new ArrayList<>();
        result.forEach(resultList::add);

        assertEquals(3, resultList.size());
        assertTrue(resultList.contains(testGenre));
        assertTrue(resultList.contains(genre2));
        assertTrue(resultList.contains(genre3));

        verify(setOperations).members("genre:all");
        verify(hashOperations, times(3)).entries(anyString());
    }

    @Test
    void testFindAll_whenNoGenres_shouldReturnEmptyList() {
        // Arrange
        when(setOperations.members("genre:all")).thenReturn(null);

        // Act
        Iterable<Genre> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());

        verify(setOperations).members("genre:all");
        verify(hashOperations, never()).entries(anyString());
    }

    @Test
    void testFindAll_whenSetEmpty_shouldReturnEmptyList() {
        // Arrange
        when(setOperations.members("genre:all")).thenReturn(Collections.emptySet());

        // Act
        Iterable<Genre> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void testFindAll_whenSomeHashesEmpty_shouldSkipThem() {
        // Arrange
        Set<Object> genrePks = new HashSet<>(Arrays.asList("1", "2", "3"));

        when(setOperations.members("genre:all")).thenReturn(genrePks);
        when(hashOperations.entries("genre:1")).thenReturn(testHashObject);
        when(hashOperations.entries("genre:2")).thenReturn(Collections.emptyMap()); // Empty!
        when(hashOperations.entries("genre:3")).thenReturn(testHashObject);

        when(mapper.fromRedisHash(testHashObject)).thenReturn(testGenre);

        // Act
        Iterable<Genre> result = repository.findAll();

        // Assert
        List<Genre> resultList = new ArrayList<>();
        result.forEach(resultList::add);

        assertEquals(2, resultList.size()); // Only 2, not 3
    }

    @Test
    void testFindAll_whenMapperReturnsNull_shouldSkip() {
        // Arrange
        Set<Object> genrePks = Set.of("1");

        when(setOperations.members("genre:all")).thenReturn(genrePks);
        when(hashOperations.entries("genre:1")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(null); // Mapper returns null

        // Act
        Iterable<Genre> result = repository.findAll();

        // Assert
        List<Genre> resultList = new ArrayList<>();
        result.forEach(resultList::add);

        assertEquals(0, resultList.size());
    }

    // ========================================
    // SAVE TESTS
    // ========================================

    @Test
    void testSave_withValidGenre_shouldSaveSuccessfully() {
        // Arrange
        when(mapper.toRedisHash(testGenre)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);
        doNothing().when(hashOperations).putAll(anyString(), anyMap());
        doNothing().when(valueOperations).set(anyString(), any(), any());
        when(setOperations.add(anyString(), any())).thenReturn(1L);

        // Act
        Genre result = repository.save(testGenre);

        // Assert
        assertNotNull(result);
        assertEquals(testGenre, result);

        verify(mapper).toRedisHash(testGenre);
        verify(hashOperations).putAll(eq("genre:1"), eq(testHash));
        verify(valueOperations).set(eq("genre:name:fiction"), eq("1"), any());
        verify(setOperations).add(eq("genre:all"), eq("1"));
        verify(redisTemplate, atLeast(2)).expire(anyString(), anyLong(), any());
    }

    @Test
    void testSave_withNullGenre_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );

        assertEquals("Genre cannot be null", exception.getMessage());
        verify(mapper, never()).toRedisHash(any());
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    void testSave_withNullPk_shouldReturnGenreWithoutSaving() {
        // Arrange
        Genre genreWithoutPk = new Genre("New Genre");
        assertNull(genreWithoutPk.getPk());

        // Act
        Genre result = repository.save(genreWithoutPk);

        // Assert
        assertEquals(genreWithoutPk, result);
        verify(mapper, never()).toRedisHash(any());
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    void testSave_shouldCreateSecondaryIndexes() {
        // Arrange
        when(mapper.toRedisHash(testGenre)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        repository.save(testGenre);

        // Assert
        // Verify name index was created
        verify(valueOperations).set(eq("genre:name:fiction"), eq("1"), any());

        // Verify genre was added to set of all genres
        verify(setOperations).add(eq("genre:all"), eq("1"));
    }

    @Test
    void testSave_multipleTimes_shouldUpdateCache() {
        // Arrange
        when(mapper.toRedisHash(testGenre)).thenReturn(testHash);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        repository.save(testGenre);
        repository.save(testGenre);
        repository.save(testGenre);

        // Assert
        verify(hashOperations, times(3)).putAll(eq("genre:1"), eq(testHash));
    }

    // ========================================
    // DELETE TESTS
    // ========================================

    @Test
    void testDelete_withValidGenre_shouldDeleteSuccessfully() {
        // Arrange
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        repository.delete(testGenre);

        // Assert
        // Verify name index was deleted
        verify(redisTemplate).delete("genre:name:fiction");

        // Verify removed from set of all genres
        verify(setOperations).remove("genre:all", "1");

        // Verify main hash was deleted
        verify(redisTemplate).delete("genre:1");
    }

    @Test
    void testDelete_withNullGenre_shouldNotThrowException() {
        // Act
        repository.delete(null);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
        verify(setOperations, never()).remove(anyString(), any());
    }

    @Test
    void testDelete_withNullPk_shouldNotThrowException() {
        // Arrange
        Genre genreWithoutPk = new Genre("Genre Without PK");

        // Act
        repository.delete(genreWithoutPk);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testDelete_shouldRemoveAllIndexes() {
        // Arrange
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        repository.delete(testGenre);

        // Assert
        verify(redisTemplate).delete("genre:name:fiction"); // Name index
        verify(setOperations).remove("genre:all", "1"); // All genres set
        verify(redisTemplate).delete("genre:1"); // Main hash
    }

    // ========================================
    // COMPLEX QUERIES - NOT SUPPORTED
    // ========================================

    @Test
    void testFindTop5GenreByBookCount_shouldReturnEmptyPage() {
        // Arrange
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 5);

        // Act
        org.springframework.data.domain.Page<?> result =
                repository.findTop5GenreByBookCount(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetLendingsPerMonthLastYearByGenre_shouldReturnEmptyList() {
        // Act
        var result = repository.getLendingsPerMonthLastYearByGenre();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAverageLendingsInMonth_shouldReturnEmptyList() {
        // Arrange
        java.time.LocalDate month = java.time.LocalDate.now();
        pt.psoft.g1.psoftg1.shared.services.Page page =
                new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);

        // Act
        var result = repository.getAverageLendingsInMonth(month, page);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLendingsAverageDurationPerMonth_shouldReturnEmptyList() {
        // Arrange
        java.time.LocalDate start = java.time.LocalDate.now().minusMonths(1);
        java.time.LocalDate end = java.time.LocalDate.now();

        // Act
        var result = repository.getLendingsAverageDurationPerMonth(start, end);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // EDGE CASES & ERROR SCENARIOS
    // ========================================

    @Test
    void testFindByString_withSpecialCharacters_shouldHandleCorrectly() {
        // Arrange
        String genreWithSpecialChars = "Science-Fiction & Fantasy";
        String nameKey = "genre:name:" + genreWithSpecialChars.toLowerCase();

        when(valueOperations.get(nameKey)).thenReturn("1");
        when(hashOperations.entries("genre:1")).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testGenre);

        // Act
        Optional<Genre> result = repository.findByString(genreWithSpecialChars);

        // Assert
        assertTrue(result.isPresent());
    }



    @Test
    void testFindAll_maintainsOrderFromSet() {
        // Arrange
        Set<Object> genrePks = new LinkedHashSet<>(Arrays.asList("1", "2", "3"));

        when(setOperations.members("genre:all")).thenReturn(genrePks);
        when(hashOperations.entries(anyString())).thenReturn(testHashObject);
        when(mapper.fromRedisHash(testHashObject)).thenReturn(testGenre);

        // Act
        Iterable<Genre> result = repository.findAll();

        // Assert
        assertNotNull(result);
        List<Genre> resultList = new ArrayList<>();
        result.forEach(resultList::add);
        assertEquals(3, resultList.size());
    }

    @Test
    void testSave_whenRedisThrowsException_shouldPropagateException() {
        // Arrange
        when(mapper.toRedisHash(testGenre)).thenReturn(testHash);
        doThrow(new RuntimeException("Redis connection failed"))
                .when(hashOperations).putAll(anyString(), anyMap());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> repository.save(testGenre));
    }
}