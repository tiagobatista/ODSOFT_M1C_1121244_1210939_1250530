package pt.psoft.g1.psoftg1.genremanagement.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper.GenreRedisMapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = GenreRedisMapper
 *
 * Testa conversão Genre ↔ Redis Hash
 */
class GenreRedisMapperUnitTest {

    private GenreRedisMapper mapper;
    private Genre validGenre;

    @BeforeEach
    void setUp() {
        mapper = new GenreRedisMapper();

        // Setup valid Genre
        validGenre = new Genre(1L, "Fantasia");
    }

    // ==================== TO REDIS HASH TESTS ====================

    @Test
    void ensureToRedisHashConvertsCompleteGenreSuccessfully() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(validGenre);

        // Assert
        assertNotNull(hash);
        assertEquals("1", hash.get("pk"));
        assertEquals("Fantasia", hash.get("genre"));
    }

    @Test
    void ensureToRedisHashReturnsNullWhenGenreIsNull() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(null);

        // Assert
        assertNull(hash);
    }

    @Test
    void ensureToRedisHashHandlesGenreWithoutPk() {
        // Arrange
        Genre genreWithoutPk = new Genre("Terror");
        // pk is null

        // Act
        Map<String, String> hash = mapper.toRedisHash(genreWithoutPk);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("pk"));
        assertEquals("Terror", hash.get("genre"));
    }

    @Test
    void ensureToRedisHashHandlesGenreWithLongName() {
        // Arrange
        String longName = "A".repeat(100); // Max length
        Genre genreWithLongName = new Genre(1L, longName);

        // Act
        Map<String, String> hash = mapper.toRedisHash(genreWithLongName);

        // Assert
        assertNotNull(hash);
        assertEquals(longName, hash.get("genre"));
    }

    @Test
    void ensureToRedisHashHandlesGenreWithSpecialCharacters() {
        // Arrange
        Genre genreWithSpecialChars = new Genre(1L, "Ficção Científica");

        // Act
        Map<String, String> hash = mapper.toRedisHash(genreWithSpecialChars);

        // Assert
        assertNotNull(hash);
        assertEquals("Ficção Científica", hash.get("genre"));
    }

    // ==================== FROM REDIS HASH TESTS ====================

    @Test
    void ensureFromRedisHashConvertsCompleteHashSuccessfully() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "1");
        hash.put("genre", "Fantasia");

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(genre);
        assertEquals(1L, genre.getPk());
        assertEquals("Fantasia", genre.getGenre());
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenHashIsNull() {
        // Act
        Genre genre = mapper.fromRedisHash(null);

        // Assert
        assertNull(genre);
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenHashIsEmpty() {
        // Arrange
        Map<Object, Object> emptyHash = new HashMap<>();

        // Act
        Genre genre = mapper.fromRedisHash(emptyHash);

        // Assert
        assertNull(genre);
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenGenreNameIsMissing() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "1");
        // genre name missing

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNull(genre); // Genre name é obrigatório
    }

    @Test
    void ensureFromRedisHashHandlesHashWithoutPk() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        // pk missing
        hash.put("genre", "Terror");

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(genre);
        assertNull(genre.getPk());
        assertEquals("Terror", genre.getGenre());
    }

    @Test
    void ensureFromRedisHashHandlesInvalidPk() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "invalid"); // Não é número
        hash.put("genre", "Terror");

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNull(genre); // Erro no parsing
    }

    @Test
    void ensureFromRedisHashHandlesLongPkValue() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "999999999");
        hash.put("genre", "Fantasia");

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(genre);
        assertEquals(999999999L, genre.getPk());
    }

    @Test
    void ensureFromRedisHashHandlesGenreWithSpecialCharacters() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "1");
        hash.put("genre", "Ficção Científica");

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(genre);
        assertEquals("Ficção Científica", genre.getGenre());
    }

    @Test
    void ensureFromRedisHashHandlesGenreWithLongName() {
        // Arrange
        String longName = "A".repeat(100);
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "1");
        hash.put("genre", longName);

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(genre);
        assertEquals(longName, genre.getGenre());
    }

    // ==================== ROUND TRIP TESTS ====================

    @Test
    void ensureRoundTripConversionPreservesData() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(validGenre);

        // Convert to Object map for fromRedisHash
        Map<Object, Object> objectHash = new HashMap<>(hash);
        Genre reconstructedGenre = mapper.fromRedisHash(objectHash);

        // Assert
        assertNotNull(reconstructedGenre);
        assertEquals(validGenre.getPk(), reconstructedGenre.getPk());
        assertEquals(validGenre.getGenre(), reconstructedGenre.getGenre());
    }

    @Test
    void ensureRoundTripWithoutPkPreservesData() {
        // Arrange
        Genre genreWithoutPk = new Genre("Terror");

        // Act
        Map<String, String> hash = mapper.toRedisHash(genreWithoutPk);
        Map<Object, Object> objectHash = new HashMap<>(hash);
        Genre reconstructedGenre = mapper.fromRedisHash(objectHash);

        // Assert
        assertNotNull(reconstructedGenre);
        assertNull(reconstructedGenre.getPk());
        assertEquals(genreWithoutPk.getGenre(), reconstructedGenre.getGenre());
    }

    // ==================== EDGE CASES ====================

    @Test
    void ensureToRedisHashHandlesGenreWithAccents() {
        // Arrange
        Genre genreWithAccents = new Genre(1L, "Poesia Épica");

        // Act
        Map<String, String> hash = mapper.toRedisHash(genreWithAccents);

        // Assert
        assertNotNull(hash);
        assertEquals("Poesia Épica", hash.get("genre"));
    }

    @Test
    void ensureFromRedisHashHandlesGenreWithAccents() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "1");
        hash.put("genre", "Poesia Épica");

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(genre);
        assertEquals("Poesia Épica", genre.getGenre());
    }

    @Test
    void ensureToRedisHashHandlesGenreWithNumbers() {
        // Arrange
        Genre genreWithNumbers = new Genre(1L, "Ficção Científica 2000");

        // Act
        Map<String, String> hash = mapper.toRedisHash(genreWithNumbers);

        // Assert
        assertNotNull(hash);
        assertEquals("Ficção Científica 2000", hash.get("genre"));
    }

    @Test
    void ensureFromRedisHashHandlesGenreWithMultipleWords() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "1");
        hash.put("genre", "Romance Histórico Medieval");

        // Act
        Genre genre = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(genre);
        assertEquals("Romance Histórico Medieval", genre.getGenre());
    }
}
