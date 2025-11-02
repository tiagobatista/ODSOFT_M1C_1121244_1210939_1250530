package pt.psoft.g1.psoftg1.authormanagement.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Mapper.AuthorEntityMapper;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.sql.AuthorRepositoryImpl;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.SpringDataAuthorRepository;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Opaque-Box (Black-Box) Unit Tests for AuthorRepositoryImpl (SQL)
 *
 * Purpose: Test SQL repository in ISOLATION without knowledge of internal implementation
 * Testing Strategy: Mock dependencies (SpringDataAuthorRepository, Mapper), test through public interface
 * SUT: AuthorRepositoryImpl
 * Type: 2.3.1 - Functional opaque-box with SUT = classes
 *
 * Test Coverage:
 * - findByAuthorNumber (found, not found)
 * - searchByNameNameStartsWith (found, not found, multiple results)
 * - searchByNameName (exact match, no match)
 * - findAll (empty, multiple authors)
 * - save (new author, update author)
 * - delete (existing author)
 * - findTopAuthorByLendings (with results, empty)
 * - findCoAuthorsByAuthorNumber (found, not found)
 *
 * @author ARQSOFT 2025-2026
 */
class AuthorRepositoryImplUnitTest {

    @InjectMocks
    private AuthorRepositoryImpl authorRepository;

    @Mock
    private SpringDataAuthorRepository sqlRepository;

    @Mock
    private AuthorEntityMapper authorEntityMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===============================================
    // FIND BY AUTHOR NUMBER TESTS
    // ===============================================

    @Test
    void testFindByAuthorNumber_whenAuthorExists_shouldReturnAuthor() {
        // Arrange
        Long authorNumber = 1L;
        AuthorEntity mockEntity = mock(AuthorEntity.class);
        Author mockAuthor = mock(Author.class);

        when(sqlRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.of(mockEntity));
        when(authorEntityMapper.toModel(mockEntity)).thenReturn(mockAuthor);

        // Act
        Optional<Author> result = authorRepository.findByAuthorNumber(authorNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockAuthor, result.get());
        verify(sqlRepository).findByAuthorNumber(authorNumber);
        verify(authorEntityMapper).toModel(mockEntity);
    }

    @Test
    void testFindByAuthorNumber_whenAuthorNotExists_shouldReturnEmpty() {
        // Arrange
        Long authorNumber = 999L;
        when(sqlRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorRepository.findByAuthorNumber(authorNumber);

        // Assert
        assertFalse(result.isPresent());
        assertEquals(Optional.empty(), result);
        verify(sqlRepository).findByAuthorNumber(authorNumber);
        verify(authorEntityMapper, never()).toModel(any());
    }

    @Test
    void testFindByAuthorNumber_withNullNumber_shouldReturnEmpty() {
        // Arrange
        when(sqlRepository.findByAuthorNumber(null)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorRepository.findByAuthorNumber(null);

        // Assert
        assertFalse(result.isPresent());
    }

    // ===============================================
    // SEARCH BY NAME (STARTS WITH) TESTS
    // ===============================================

    @Test
    void testSearchByNameNameStartsWith_whenAuthorsExist_shouldReturnList() {
        // Arrange
        String searchName = "John";
        List<AuthorEntity> entities = new ArrayList<>();
        AuthorEntity mockEntity = mock(AuthorEntity.class);
        entities.add(mockEntity);

        Author mockAuthor = mock(Author.class);

        when(sqlRepository.searchByNameNameStartsWith(searchName)).thenReturn(entities);
        when(authorEntityMapper.toModel(mockEntity)).thenReturn(mockAuthor);

        // Act
        List<Author> result = authorRepository.searchByNameNameStartsWith(searchName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockAuthor, result.get(0));
        verify(sqlRepository).searchByNameNameStartsWith(searchName);
        verify(authorEntityMapper).toModel(mockEntity);
    }

    @Test
    void testSearchByNameNameStartsWith_whenNoAuthors_shouldReturnEmptyList() {
        // Arrange
        String searchName = "NonExistent";
        when(sqlRepository.searchByNameNameStartsWith(searchName)).thenReturn(new ArrayList<>());

        // Act
        List<Author> result = authorRepository.searchByNameNameStartsWith(searchName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(sqlRepository).searchByNameNameStartsWith(searchName);
        verify(authorEntityMapper, never()).toModel(any());
    }

    @Test
    void testSearchByNameNameStartsWith_withMultipleResults_shouldReturnAll() {
        // Arrange
        String searchName = "J";
        List<AuthorEntity> entities = new ArrayList<>();
        AuthorEntity entity1 = mock(AuthorEntity.class);
        AuthorEntity entity2 = mock(AuthorEntity.class);
        AuthorEntity entity3 = mock(AuthorEntity.class);
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);

        Author author1 = mock(Author.class);
        Author author2 = mock(Author.class);
        Author author3 = mock(Author.class);

        when(sqlRepository.searchByNameNameStartsWith(searchName)).thenReturn(entities);
        when(authorEntityMapper.toModel(entity1)).thenReturn(author1);
        when(authorEntityMapper.toModel(entity2)).thenReturn(author2);
        when(authorEntityMapper.toModel(entity3)).thenReturn(author3);

        // Act
        List<Author> result = authorRepository.searchByNameNameStartsWith(searchName);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(author1));
        assertTrue(result.contains(author2));
        assertTrue(result.contains(author3));
    }

    // ===============================================
    // SEARCH BY NAME (EXACT) TESTS
    // ===============================================

    @Test
    void testSearchByNameName_whenAuthorExists_shouldReturnList() {
        // Arrange
        String exactName = "John Doe";
        List<AuthorEntity> entities = new ArrayList<>();
        AuthorEntity mockEntity = mock(AuthorEntity.class);
        entities.add(mockEntity);

        Author mockAuthor = mock(Author.class);

        when(sqlRepository.searchByNameName(exactName)).thenReturn(entities);
        when(authorEntityMapper.toModel(mockEntity)).thenReturn(mockAuthor);

        // Act
        List<Author> result = authorRepository.searchByNameName(exactName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockAuthor, result.get(0));
        verify(sqlRepository).searchByNameName(exactName);
    }

    @Test
    void testSearchByNameName_whenAuthorNotExists_shouldReturnEmptyList() {
        // Arrange
        String exactName = "NonExistent Author";
        when(sqlRepository.searchByNameName(exactName)).thenReturn(new ArrayList<>());

        // Act
        List<Author> result = authorRepository.searchByNameName(exactName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sqlRepository).searchByNameName(exactName);
    }

    // ===============================================
    // FIND ALL TESTS
    // ===============================================

    @Test
    void testFindAll_whenAuthorsExist_shouldReturnAllAuthors() {
        // Arrange
        List<AuthorEntity> entities = new ArrayList<>();
        AuthorEntity entity1 = mock(AuthorEntity.class);
        AuthorEntity entity2 = mock(AuthorEntity.class);
        entities.add(entity1);
        entities.add(entity2);

        Author author1 = mock(Author.class);
        Author author2 = mock(Author.class);

        when(sqlRepository.findAll()).thenReturn(entities);
        when(authorEntityMapper.toModel(entity1)).thenReturn(author1);
        when(authorEntityMapper.toModel(entity2)).thenReturn(author2);

        // Act
        Iterable<Author> result = authorRepository.findAll();

        // Assert
        assertNotNull(result);
        List<Author> resultList = StreamSupport
                .stream(result.spliterator(), false)
                .toList();

        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(author1));
        assertTrue(resultList.contains(author2));
        verify(sqlRepository).findAll();
    }

    @Test
    void testFindAll_whenNoAuthors_shouldReturnEmptyList() {
        // Arrange
        when(sqlRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        Iterable<Author> result = authorRepository.findAll();

        // Assert
        assertNotNull(result);
        List<Author> resultList = StreamSupport
                .stream(result.spliterator(), false)
                .toList();

        assertEquals(0, resultList.size());
        assertTrue(resultList.isEmpty());
    }

    // ===============================================
    // SAVE TESTS
    // ===============================================

    @Test
    void testSave_withValidAuthor_shouldSaveAndReturnAuthor() {
        // Arrange
        Author mockAuthor = mock(Author.class);
        AuthorEntity mockEntity = mock(AuthorEntity.class);
        AuthorEntity savedEntity = mock(AuthorEntity.class);
        Author savedAuthor = mock(Author.class);

        when(authorEntityMapper.toEntity(mockAuthor)).thenReturn(mockEntity);
        when(sqlRepository.save(mockEntity)).thenReturn(savedEntity);
        when(authorEntityMapper.toModel(savedEntity)).thenReturn(savedAuthor);

        // Act
        Author result = authorRepository.save(mockAuthor);

        // Assert
        assertNotNull(result);
        assertEquals(savedAuthor, result);
        verify(authorEntityMapper).toEntity(mockAuthor);
        verify(sqlRepository).save(mockEntity);
        verify(authorEntityMapper).toModel(savedEntity);
    }

    @Test
    void testSave_multipleTimes_shouldCallRepositoryEachTime() {
        // Arrange
        Author mockAuthor = mock(Author.class);
        AuthorEntity mockEntity = mock(AuthorEntity.class);

        when(authorEntityMapper.toEntity(mockAuthor)).thenReturn(mockEntity);
        when(sqlRepository.save(mockEntity)).thenReturn(mockEntity);
        when(authorEntityMapper.toModel(mockEntity)).thenReturn(mockAuthor);

        // Act
        authorRepository.save(mockAuthor);
        authorRepository.save(mockAuthor);
        authorRepository.save(mockAuthor);

        // Assert
        verify(sqlRepository, times(3)).save(mockEntity);
        verify(authorEntityMapper, times(3)).toEntity(mockAuthor);
        verify(authorEntityMapper, times(3)).toModel(mockEntity);
    }

    // ===============================================
    // DELETE TESTS
    // ===============================================

    @Test
    void testDelete_withValidAuthor_shouldDeleteFromRepository() {
        // Arrange
        Author mockAuthor = mock(Author.class);
        AuthorEntity mockEntity = mock(AuthorEntity.class);

        when(authorEntityMapper.toEntity(mockAuthor)).thenReturn(mockEntity);
        doNothing().when(sqlRepository).delete(mockEntity);

        // Act
        authorRepository.delete(mockAuthor);

        // Assert
        verify(authorEntityMapper).toEntity(mockAuthor);
        verify(sqlRepository).delete(mockEntity);
    }

    // ===============================================
    // FIND TOP AUTHORS BY LENDINGS TESTS
    // ===============================================

    @Test
    void testFindTopAuthorByLendings_whenAuthorsExist_shouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        AuthorLendingView mockView = mock(AuthorLendingView.class);
        List<AuthorLendingView> content = List.of(mockView);
        Page<AuthorLendingView> mockPage = new PageImpl<>(content, pageable, 1);

        when(sqlRepository.findTopAuthorByLendings(pageable)).thenReturn(mockPage);

        // Act
        Page<AuthorLendingView> result = authorRepository.findTopAuthorByLendings(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(mockView, result.getContent().get(0));
        verify(sqlRepository).findTopAuthorByLendings(pageable);
    }

    @Test
    void testFindTopAuthorByLendings_whenNoAuthors_shouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<AuthorLendingView> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(sqlRepository.findTopAuthorByLendings(pageable)).thenReturn(emptyPage);

        // Act
        Page<AuthorLendingView> result = authorRepository.findTopAuthorByLendings(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ===============================================
    // FIND CO-AUTHORS TESTS
    // ===============================================

    @Test
    void testFindCoAuthorsByAuthorNumber_whenCoAuthorsExist_shouldReturnList() {
        // Arrange
        Long authorNumber = 1L;
        List<AuthorEntity> entities = new ArrayList<>();
        AuthorEntity coAuthorEntity = mock(AuthorEntity.class);
        entities.add(coAuthorEntity);

        Author coAuthor = mock(Author.class);

        when(sqlRepository.findCoAuthorsByAuthorNumber(authorNumber)).thenReturn(entities);
        when(authorEntityMapper.toModel(coAuthorEntity)).thenReturn(coAuthor);

        // Act
        List<Author> result = authorRepository.findCoAuthorsByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(coAuthor, result.get(0));
        verify(sqlRepository).findCoAuthorsByAuthorNumber(authorNumber);
    }

    @Test
    void testFindCoAuthorsByAuthorNumber_whenNoCoAuthors_shouldReturnEmptyList() {
        // Arrange
        Long authorNumber = 1L;
        when(sqlRepository.findCoAuthorsByAuthorNumber(authorNumber)).thenReturn(new ArrayList<>());

        // Act
        List<Author> result = authorRepository.findCoAuthorsByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sqlRepository).findCoAuthorsByAuthorNumber(authorNumber);
    }

    @Test
    void testFindCoAuthorsByAuthorNumber_withMultipleCoAuthors_shouldReturnAll() {
        // Arrange
        Long authorNumber = 1L;
        List<AuthorEntity> entities = new ArrayList<>();
        AuthorEntity entity1 = mock(AuthorEntity.class);
        AuthorEntity entity2 = mock(AuthorEntity.class);
        entities.add(entity1);
        entities.add(entity2);

        Author coAuthor1 = mock(Author.class);
        Author coAuthor2 = mock(Author.class);

        when(sqlRepository.findCoAuthorsByAuthorNumber(authorNumber)).thenReturn(entities);
        when(authorEntityMapper.toModel(entity1)).thenReturn(coAuthor1);
        when(authorEntityMapper.toModel(entity2)).thenReturn(coAuthor2);

        // Act
        List<Author> result = authorRepository.findCoAuthorsByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(coAuthor1));
        assertTrue(result.contains(coAuthor2));
    }

    // ===============================================
    // EDGE CASES & ERROR SCENARIOS
    // ===============================================

    @Test
    void testSearchByNameNameStartsWith_withEmptyString_shouldReturnEmptyList() {
        // Arrange
        when(sqlRepository.searchByNameNameStartsWith("")).thenReturn(new ArrayList<>());

        // Act
        List<Author> result = authorRepository.searchByNameNameStartsWith("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_shouldMaintainOrder() {
        // Arrange
        List<AuthorEntity> entities = new ArrayList<>();
        AuthorEntity entity1 = mock(AuthorEntity.class);
        AuthorEntity entity2 = mock(AuthorEntity.class);
        AuthorEntity entity3 = mock(AuthorEntity.class);
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);

        Author author1 = mock(Author.class);
        Author author2 = mock(Author.class);
        Author author3 = mock(Author.class);

        when(sqlRepository.findAll()).thenReturn(entities);
        when(authorEntityMapper.toModel(entity1)).thenReturn(author1);
        when(authorEntityMapper.toModel(entity2)).thenReturn(author2);
        when(authorEntityMapper.toModel(entity3)).thenReturn(author3);

        // Act
        Iterable<Author> result = authorRepository.findAll();

        // Assert
        List<Author> resultList = StreamSupport
                .stream(result.spliterator(), false)
                .toList();

        assertEquals(author1, resultList.get(0));
        assertEquals(author2, resultList.get(1));
        assertEquals(author3, resultList.get(2));
    }
}