package pt.psoft.g1.psoftg1.genremanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper.GenreEntityMapper;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.sql.GenreRepositoryImpl;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.SpringDataGenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreEntity;

import jakarta.persistence.EntityManager;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = GenreRepositoryImpl (SQL)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenreRepositoryImplUnitTest {

    @Mock
    private SpringDataGenreRepository springDataRepository;

    @Mock
    private GenreEntityMapper mapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private GenreRepositoryImpl repository;

    private Genre validGenre;
    private GenreEntity validGenreEntity;

    @BeforeEach
    void setUp() {
        // Setup Genre (domain model)
        validGenre = new Genre(1L, "Fantasia");

        // Setup GenreEntity (persistence model)
        validGenreEntity = mock(GenreEntity.class);
        when(validGenreEntity.getPk()).thenReturn(1L);
        when(validGenreEntity.getGenre()).thenReturn("Fantasia");
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void ensureFindAllReturnsAllGenres() {
        // Arrange
        GenreEntity entity1 = mock(GenreEntity.class);
        GenreEntity entity2 = mock(GenreEntity.class);
        List<GenreEntity> entities = List.of(entity1, entity2);

        Genre genre1 = new Genre(1L, "Fantasia");
        Genre genre2 = new Genre(2L, "Terror");

        when(springDataRepository.findAll()).thenReturn(entities);
        when(mapper.toModel(entity1)).thenReturn(genre1);
        when(mapper.toModel(entity2)).thenReturn(genre2);

        // Act
        Iterable<Genre> results = repository.findAll();

        // Assert
        List<Genre> genreList = new ArrayList<>();
        results.forEach(genreList::add);
        assertEquals(2, genreList.size());
        assertEquals(genre1, genreList.get(0));
        assertEquals(genre2, genreList.get(1));
        verify(springDataRepository, times(1)).findAll();
    }

    @Test
    void ensureFindAllReturnsEmptyWhenNoGenres() {
        // Arrange
        when(springDataRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Iterable<Genre> results = repository.findAll();

        // Assert
        List<Genre> genreList = new ArrayList<>();
        results.forEach(genreList::add);
        assertTrue(genreList.isEmpty());
    }

    // ==================== FIND BY STRING TESTS ====================

    @Test
    void ensureFindByStringReturnsGenreWhenExists() {
        // Arrange
        when(springDataRepository.findByString("Fantasia")).thenReturn(Optional.of(validGenreEntity));
        when(mapper.toModel(validGenreEntity)).thenReturn(validGenre);

        // Act
        Optional<Genre> result = repository.findByString("Fantasia");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validGenre, result.get());
        verify(springDataRepository, times(1)).findByString("Fantasia");
        verify(mapper, times(1)).toModel(validGenreEntity);
    }

    @Test
    void ensureFindByStringReturnsEmptyWhenNotFound() {
        // Arrange
        when(springDataRepository.findByString("NonExistent")).thenReturn(Optional.empty());

        // Act
        Optional<Genre> result = repository.findByString("NonExistent");

        // Assert
        assertFalse(result.isPresent());
        verify(springDataRepository, times(1)).findByString("NonExistent");
        verify(mapper, never()).toModel(any());
    }

    @Test
    void ensureFindByStringIsCaseInsensitive() {
        // Arrange
        when(springDataRepository.findByString("fantasia")).thenReturn(Optional.of(validGenreEntity));
        when(mapper.toModel(validGenreEntity)).thenReturn(validGenre);

        // Act
        Optional<Genre> result = repository.findByString("fantasia");

        // Assert
        assertTrue(result.isPresent());
        verify(springDataRepository, times(1)).findByString("fantasia");
    }

    // ==================== SAVE TESTS ====================

    @Test
    void ensureSavePersistsGenre() {
        // Arrange
        when(mapper.toEntity(validGenre)).thenReturn(validGenreEntity);
        when(springDataRepository.save(validGenreEntity)).thenReturn(validGenreEntity);
        when(mapper.toModel(validGenreEntity)).thenReturn(validGenre);

        // Act
        Genre result = repository.save(validGenre);

        // Assert
        assertNotNull(result);
        assertEquals(validGenre, result);
        verify(mapper, times(1)).toEntity(validGenre);
        verify(springDataRepository, times(1)).save(validGenreEntity);
        verify(mapper, times(1)).toModel(validGenreEntity);
    }

    @Test
    void ensureSaveUpdatesExistingGenre() {
        // Arrange - Simulate update (genre with existing pk)
        Genre updatedGenre = new Genre(1L, "Fantasia Épica");
        GenreEntity updatedEntity = mock(GenreEntity.class);

        when(mapper.toEntity(updatedGenre)).thenReturn(updatedEntity);
        when(springDataRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(mapper.toModel(updatedEntity)).thenReturn(updatedGenre);

        // Act
        Genre result = repository.save(updatedGenre);

        // Assert
        assertNotNull(result);
        assertEquals(updatedGenre, result);
        verify(springDataRepository, times(1)).save(updatedEntity);
    }

    @Test
    void ensureSaveCreatesNewGenre() {
        // Arrange - New genre without pk
        Genre newGenre = new Genre("Romance");
        GenreEntity newEntity = mock(GenreEntity.class);

        when(mapper.toEntity(newGenre)).thenReturn(newEntity);
        when(springDataRepository.save(newEntity)).thenReturn(newEntity);
        when(mapper.toModel(newEntity)).thenReturn(newGenre);

        // Act
        Genre result = repository.save(newGenre);

        // Assert
        assertNotNull(result);
        verify(springDataRepository, times(1)).save(newEntity);
    }

    // ==================== FIND TOP 5 GENRES BY BOOK COUNT TESTS ====================

    @Test
    void ensureFindTop5GenreByBookCountReturnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        GenreBookCountDTO dto = mock(GenreBookCountDTO.class);
        Page<GenreBookCountDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(springDataRepository.findTop5GenreByBookCount(pageable)).thenReturn(page);

        // Act
        Page<GenreBookCountDTO> result = repository.findTop5GenreByBookCount(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(springDataRepository, times(1)).findTop5GenreByBookCount(pageable);
    }

    @Test
    void ensureFindTop5GenreByBookCountReturnsEmptyWhenNoData() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<GenreBookCountDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(springDataRepository.findTop5GenreByBookCount(pageable)).thenReturn(emptyPage);

        // Act
        Page<GenreBookCountDTO> result = repository.findTop5GenreByBookCount(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== DELETE TESTS ====================

    @Test
    void ensureDeleteRemovesGenre() {
        // Arrange
        when(mapper.toEntity(validGenre)).thenReturn(validGenreEntity);

        // Act
        repository.delete(validGenre);

        // Assert
        verify(mapper, times(1)).toEntity(validGenre);
        verify(springDataRepository, times(1)).delete(validGenreEntity);
    }

    // ==================== COMPLEX QUERIES TESTS ====================






}