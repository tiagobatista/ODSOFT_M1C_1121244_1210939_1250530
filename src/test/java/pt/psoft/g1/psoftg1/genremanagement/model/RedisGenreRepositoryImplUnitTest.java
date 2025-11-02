package pt.psoft.g1.psoftg1.genremanagement.model;



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
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper.GenreRedisMapper;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Redis.RedisGenreRepositoryImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = RedisGenreRepositoryImpl
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @InjectMocks
    private RedisGenreRepositoryImpl redisRepository;

    private Genre validGenre;
    private Map<String, String> validGenreHash;
    private Map<Object, Object> validGenreHashObject;

    @BeforeEach
    void setUp() {
        // Setup mocks
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // Setup Genre
        validGenre = new Genre(1L, "Fantasia");

        // Setup Redis Hash
        validGenreHash = new HashMap<>();
        validGenreHash.put("pk", "1");
        validGenreHash.put("genre", "Fantasia");

        validGenreHashObject = new HashMap<>(validGenreHash);
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void ensureFindAllReturnsAllGenres() {
        // Arrange
        Set<Object> genrePks = Set.of("1", "2");

        when(setOperations.members("genre:all")).thenReturn(genrePks);
        when(hashOperations.entries("genre:1")).thenReturn(validGenreHashObject);
        when(hashOperations.entries("genre:2")).thenReturn(validGenreHashObject);
        when(mapper.fromRedisHash(validGenreHashObject)).thenReturn(validGenre);

        // Act
        Iterable<Genre> results = redisRepository.findAll();

        // Assert
        assertNotNull(results);
        List<Genre> genreList = new ArrayList<>();
        results.forEach(genreList::add);
        assertEquals(2, genreList.size());
        verify(setOperations, times(1)).members("genre:all");
    }

    @Test
    void ensureFindAllReturnsEmptyWhenNoGenres() {
        // Arrange
        when(setOperations.members("genre:all")).thenReturn(Collections.emptySet());

        // Act
        Iterable<Genre> results = redisRepository.findAll();

        // Assert
        assertNotNull(results);
        List<Genre> genreList = new ArrayList<>();
        results.forEach(genreList::add);
        assertTrue(genreList.isEmpty());
    }

    @Test
    void ensureFindAllReturnsEmptyWhenSetIsNull() {
        // Arrange
        when(setOperations.members("genre:all")).thenReturn(null);

        // Act
        Iterable<Genre> results = redisRepository.findAll();

        // Assert
        assertNotNull(results);
        List<Genre> genreList = new ArrayList<>();
        results.forEach(genreList::add);
        assertTrue(genreList.isEmpty());
    }

    // ==================== FIND BY STRING TESTS ====================

    @Test
    void ensureFindByStringReturnsGenreWhenExists() {
        // Arrange
        String genreName = "Fantasia";
        String nameKey = "genre:name:fantasia";
        String genreKey = "genre:1";

        when(valueOperations.get(nameKey)).thenReturn("1");
        when(hashOperations.entries(genreKey)).thenReturn(validGenreHashObject);
        when(mapper.fromRedisHash(validGenreHashObject)).thenReturn(validGenre);

        // Act
        Optional<Genre> result = redisRepository.findByString(genreName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validGenre, result.get());
        verify(valueOperations, times(1)).get(nameKey);
        verify(hashOperations, times(1)).entries(genreKey);
    }

    @Test
    void ensureFindByStringReturnsEmptyWhenNotFound() {
        // Arrange
        String genreName = "NonExistent";
        String nameKey = "genre:name:nonexistent";

        when(valueOperations.get(nameKey)).thenReturn(null);

        // Act
        Optional<Genre> result = redisRepository.findByString(genreName);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, times(1)).get(nameKey);
        verify(hashOperations, never()).entries(anyString());
    }

    @Test
    void ensureFindByStringReturnsEmptyWhenGenreNameIsNull() {
        // Act
        Optional<Genre> result = redisRepository.findByString(null);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void ensureFindByStringReturnsEmptyWhenGenreNameIsBlank() {
        // Act
        Optional<Genre> result = redisRepository.findByString("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void ensureFindByStringConvertsToLowercase() {
        // Arrange
        String genreName = "FANTASIA";
        String nameKey = "genre:name:fantasia"; // lowercase

        when(valueOperations.get(nameKey)).thenReturn("1");
        when(hashOperations.entries("genre:1")).thenReturn(validGenreHashObject);
        when(mapper.fromRedisHash(validGenreHashObject)).thenReturn(validGenre);

        // Act
        Optional<Genre> result = redisRepository.findByString(genreName);

        // Assert
        assertTrue(result.isPresent());
        verify(valueOperations, times(1)).get(nameKey);
    }

    // ==================== SAVE TESTS ====================

    @Test
    void ensureSaveStoresGenreInRedis() {
        // Arrange
        when(mapper.toRedisHash(validGenre)).thenReturn(validGenreHash);

        // Act
        Genre result = redisRepository.save(validGenre);

        // Assert
        assertNotNull(result);
        assertEquals(validGenre, result);

        verify(hashOperations, times(1)).putAll(eq("genre:1"), eq(validGenreHash));
        verify(redisTemplate, times(1)).expire(eq("genre:1"), anyLong(), any());
        verify(valueOperations, times(1)).set(eq("genre:name:fantasia"), eq("1"), any());
        verify(setOperations, times(1)).add(eq("genre:all"), eq("1"));
    }

    @Test
    void ensureSaveThrowsExceptionWhenGenreIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> redisRepository.save(null));
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    void ensureSaveReturnsGenreWhenPkIsNull() {
        // Arrange
        Genre genreWithoutPk = new Genre("Terror");
        // pk is null

        // Act
        Genre result = redisRepository.save(genreWithoutPk);

        // Assert
        assertNotNull(result);
        assertEquals(genreWithoutPk, result);
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    // ==================== DELETE TESTS ====================

    @Test
    void ensureDeleteRemovesGenreAndIndices() {
        // Act
        redisRepository.delete(validGenre);

        // Assert
        verify(redisTemplate, times(1)).delete("genre:1");
        verify(redisTemplate, times(1)).delete("genre:name:fantasia");
        verify(setOperations, times(1)).remove(eq("genre:all"), eq("1"));
    }

    @Test
    void ensureDeleteDoesNothingWhenGenreIsNull() {
        // Act
        redisRepository.delete(null);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void ensureDeleteDoesNothingWhenPkIsNull() {
        // Arrange
        Genre genreWithoutPk = new Genre("Terror");
        // pk is null

        // Act
        redisRepository.delete(genreWithoutPk);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    // ==================== COMPLEX QUERIES (NOT SUPPORTED) ====================

    @Test
    void ensureFindTop5GenreByBookCountReturnsEmptyPage() {
        // Act
        var result = redisRepository.findTop5GenreByBookCount(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void ensureGetLendingsPerMonthLastYearByGenreReturnsEmptyList() {
        // Act
        List result = redisRepository.getLendingsPerMonthLastYearByGenre();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureGetAverageLendingsInMonthReturnsEmptyList() {
        // Act
        List result = redisRepository.getAverageLendingsInMonth(null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureGetLendingsAverageDurationPerMonthReturnsEmptyList() {
        // Act
        List result = redisRepository.getLendingsAverageDurationPerMonth(null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
