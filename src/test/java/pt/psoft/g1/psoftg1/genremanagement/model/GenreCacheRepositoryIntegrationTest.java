package pt.psoft.g1.psoftg1.genremanagement.model;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Redis.GenreCacheRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 2.3.4 - Functional Integration Tests
 * SUT = GenreCacheRepository
 *
 * Testa o padrão Cache-Aside para Genre:
 * - Cache Hit (dados no Redis)
 * - Cache Miss (busca SQL + atualiza Redis)
 * - Write-Through (escreve SQL + atualiza Redis)
 * - Cache Invalidation (delete SQL + delete Redis)
 *
 * DIFERENÇA vs Author: findAll() é CACHEADO (poucos registos)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenreCacheRepositoryIntegrationTest {

    @Mock
    private GenreRepository cacheRepository; // Redis

    @Mock
    private GenreRepository sourceRepository; // SQL

    private GenreCacheRepository genreCacheRepository;
    private Genre validGenre;

    @BeforeEach
    void setUp() {
        genreCacheRepository = new GenreCacheRepository(cacheRepository, sourceRepository);

        // Setup Genre
        validGenre = new Genre(1L, "Fantasia");
    }

    // ==================== FIND ALL - CACHE HIT ====================

    @Test
    void ensureFindAllReturnsCachedGenresWhenCacheHit() {
        // Arrange - Genres exist in cache
        List<Genre> cachedGenres = List.of(validGenre);
        when(cacheRepository.findAll()).thenReturn(cachedGenres);

        // Act
        Iterable<Genre> result = genreCacheRepository.findAll();

        // Assert
        assertNotNull(result);
        List<Genre> genreList = new ArrayList<>();
        result.forEach(genreList::add);
        assertFalse(genreList.isEmpty());
        assertEquals(1, genreList.size());

        // Verify cache hit: only cache was called
        verify(cacheRepository, times(1)).findAll();
        verify(sourceRepository, never()).findAll();
        verify(cacheRepository, never()).save(any());
    }

    // ==================== FIND ALL - CACHE MISS ====================

    @Test
    void ensureFindAllFetchesFromSqlWhenCacheMiss() {
        // Arrange - Cache empty, SQL has data
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());

        List<Genre> sqlGenres = List.of(validGenre);
        when(sourceRepository.findAll()).thenReturn(sqlGenres);

        // Act
        Iterable<Genre> result = genreCacheRepository.findAll();

        // Assert
        assertNotNull(result);
        List<Genre> genreList = new ArrayList<>();
        result.forEach(genreList::add);
        assertEquals(1, genreList.size());

        // Verify cache miss flow
        verify(cacheRepository, times(1)).findAll();
        verify(sourceRepository, times(1)).findAll();
        verify(cacheRepository, times(1)).save(validGenre); // Cached each genre
    }

    @Test
    void ensureFindAllReturnsEmptyWhenNotInCacheOrSql() {
        // Arrange - Empty everywhere
        when(cacheRepository.findAll()).thenReturn(Collections.emptyList());
        when(sourceRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Iterable<Genre> result = genreCacheRepository.findAll();

        // Assert
        assertNotNull(result);
        List<Genre> genreList = new ArrayList<>();
        result.forEach(genreList::add);
        assertTrue(genreList.isEmpty());

        verify(cacheRepository, times(1)).findAll();
        verify(sourceRepository, times(1)).findAll();
        verify(cacheRepository, never()).save(any()); // Nothing to cache
    }

    // ==================== FIND BY STRING - CACHE HIT ====================

    @Test
    void ensureFindByStringReturnsCachedGenreWhenCacheHit() {
        // Arrange - Genre exists in cache
        when(cacheRepository.findByString("Fantasia")).thenReturn(Optional.of(validGenre));

        // Act
        Optional<Genre> result = genreCacheRepository.findByString("Fantasia");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validGenre, result.get());

        // Verify cache hit: only cache was called
        verify(cacheRepository, times(1)).findByString("Fantasia");
        verify(sourceRepository, never()).findByString(any());
        verify(cacheRepository, never()).save(any());
    }

    // ==================== FIND BY STRING - CACHE MISS ====================

    @Test
    void ensureFindByStringFetchesFromSqlWhenCacheMiss() {
        // Arrange - Cache miss, SQL hit
        when(cacheRepository.findByString("Fantasia")).thenReturn(Optional.empty());
        when(sourceRepository.findByString("Fantasia")).thenReturn(Optional.of(validGenre));

        // Act
        Optional<Genre> result = genreCacheRepository.findByString("Fantasia");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validGenre, result.get());

        // Verify cache miss flow
        verify(cacheRepository, times(1)).findByString("Fantasia");
        verify(sourceRepository, times(1)).findByString("Fantasia");
        verify(cacheRepository, times(1)).save(validGenre); // Updated cache
    }

    @Test
    void ensureFindByStringReturnsEmptyWhenNotInCacheOrSql() {
        // Arrange - Not found anywhere
        when(cacheRepository.findByString("NonExistent")).thenReturn(Optional.empty());
        when(sourceRepository.findByString("NonExistent")).thenReturn(Optional.empty());

        // Act
        Optional<Genre> result = genreCacheRepository.findByString("NonExistent");

        // Assert
        assertFalse(result.isPresent());

        verify(cacheRepository, times(1)).findByString("NonExistent");
        verify(sourceRepository, times(1)).findByString("NonExistent");
        verify(cacheRepository, never()).save(any()); // Nothing to cache
    }

    // ==================== SAVE (WRITE-THROUGH) ====================

    @Test
    void ensureSaveWritesToSqlAndUpdatesCache() {
        // Arrange
        when(sourceRepository.save(validGenre)).thenReturn(validGenre);

        // Act
        Genre result = genreCacheRepository.save(validGenre);

        // Assert
        assertNotNull(result);
        assertEquals(validGenre, result);

        // Verify write-through flow
        verify(sourceRepository, times(1)).save(validGenre); // SQL first
        verify(cacheRepository, times(1)).save(validGenre); // Then cache
    }

    @Test
    void ensureSaveContinuesWhenCacheUpdateFails() {
        // Arrange
        when(sourceRepository.save(validGenre)).thenReturn(validGenre);
        doThrow(new RuntimeException("Redis down")).when(cacheRepository).save(validGenre);

        // Act
        Genre result = genreCacheRepository.save(validGenre);

        // Assert - Operation succeeds despite cache failure
        assertNotNull(result);
        assertEquals(validGenre, result);

        verify(sourceRepository, times(1)).save(validGenre);
        verify(cacheRepository, times(1)).save(validGenre);
    }

    // ==================== DELETE (CACHE INVALIDATION) ====================

    @Test
    void ensureDeleteRemovesFromSqlAndInvalidatesCache() {
        // Act
        genreCacheRepository.delete(validGenre);

        // Assert
        verify(sourceRepository, times(1)).delete(validGenre); // SQL first
        verify(cacheRepository, times(1)).delete(validGenre); // Then cache
    }

    @Test
    void ensureDeleteContinuesWhenCacheInvalidationFails() {
        // Arrange
        doThrow(new RuntimeException("Redis down")).when(cacheRepository).delete(validGenre);

        // Act
        genreCacheRepository.delete(validGenre);

        // Assert - Operation succeeds despite cache failure
        verify(sourceRepository, times(1)).delete(validGenre);
        verify(cacheRepository, times(1)).delete(validGenre);
    }

    // ==================== COMPLEX QUERIES (ALWAYS SQL) ====================

    @Test
    void ensureFindTop5GenreByBookCountAlwaysUsesSQL() {
        // Act
        genreCacheRepository.findTop5GenreByBookCount(null);

        // Assert - Only SQL is used (complex aggregation)
        verify(sourceRepository, times(1)).findTop5GenreByBookCount(null);
        verify(cacheRepository, never()).findTop5GenreByBookCount(any());
    }

    @Test
    void ensureGetLendingsPerMonthLastYearAlwaysUsesSQL() {
        // Act
        genreCacheRepository.getLendingsPerMonthLastYearByGenre();

        // Assert - Only SQL is used (complex query)
        verify(sourceRepository, times(1)).getLendingsPerMonthLastYearByGenre();
        verify(cacheRepository, never()).getLendingsPerMonthLastYearByGenre();
    }

    @Test
    void ensureGetAverageLendingsInMonthAlwaysUsesSQL() {
        // Act
        genreCacheRepository.getAverageLendingsInMonth(null, null);

        // Assert - Only SQL is used (aggregation)
        verify(sourceRepository, times(1)).getAverageLendingsInMonth(null, null);
        verify(cacheRepository, never()).getAverageLendingsInMonth(any(), any());
    }

    @Test
    void ensureGetLendingsAverageDurationPerMonthAlwaysUsesSQL() {
        // Act
        genreCacheRepository.getLendingsAverageDurationPerMonth(null, null);

        // Assert - Only SQL is used (complex aggregation)
        verify(sourceRepository, times(1)).getLendingsAverageDurationPerMonth(null, null);
        verify(cacheRepository, never()).getLendingsAverageDurationPerMonth(any(), any());
    }

    // ==================== CACHE HIT WITH MULTIPLE GENRES ====================

    @Test
    void ensureFindAllHandlesMultipleGenres() {
        // Arrange
        Genre genre1 = new Genre(1L, "Fantasia");
        Genre genre2 = new Genre(2L, "Terror");
        Genre genre3 = new Genre(3L, "Romance");
        List<Genre> cachedGenres = List.of(genre1, genre2, genre3);

        when(cacheRepository.findAll()).thenReturn(cachedGenres);

        // Act
        Iterable<Genre> result = genreCacheRepository.findAll();

        // Assert
        List<Genre> genreList = new ArrayList<>();
        result.forEach(genreList::add);
        assertEquals(3, genreList.size());

        verify(cacheRepository, times(1)).findAll();
        verify(sourceRepository, never()).findAll();
    }
}
