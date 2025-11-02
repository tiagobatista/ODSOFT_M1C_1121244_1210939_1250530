package pt.psoft.g1.psoftg1.authormanagement.services;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration Test for AuthorServiceImpl
 *
 * Purpose: Test service layer WITH integration to repository and domain layers
 * Testing Strategy: Mock external dependencies (repositories), test service business logic
 * SUT: AuthorServiceImpl + Domain (Author, Bio, Name, Photo)
 * Type: 2.3.4 - Functional opaque-box with SUT = controller+service+{domain, repository, gateways}
 *
 * Test Coverage:
 * - Service business logic (photo URI validation, request handling)
 * - Repository integration (save, find, delete operations)
 * - Domain integration (Author entity creation, validation, patching)
 * - Error handling (NotFoundException, validation errors)
 * - Photo management logic
 * - Co-author and lending queries
 *
 * @author ARQSOFT 2025-2026
 */
@ExtendWith(MockitoExtension.class)
class AuthorServiceImplIntegrationTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorMapper mapper;

    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private Author testAuthor;
    private CreateAuthorRequest createRequest;
    private UpdateAuthorRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAuthor = new Author("John Doe", "Famous author biography", null);
        testAuthor.setAuthorNumber(1L);
        testAuthor.setVersion(1L);

        createRequest = new CreateAuthorRequest();
        createRequest.setName("John Doe");
        createRequest.setBio("Famous author biography");

        updateRequest = new UpdateAuthorRequest();
        updateRequest.setName("Updated Name");
    }

    // ========================================
    // FIND ALL TESTS
    // ========================================

    @Test
    void testFindAll_whenAuthorsExist_shouldReturnAllAuthors() {
        // Arrange
        Author author2 = new Author("Jane Smith", "Another author", null);
        author2.setAuthorNumber(2L);

        when(authorRepository.findAll()).thenReturn(Arrays.asList(testAuthor, author2));

        // Act
        Iterable<Author> result = authorService.findAll();

        // Assert
        assertNotNull(result);
        List<Author> authorList = new ArrayList<>();
        result.forEach(authorList::add);
        assertEquals(2, authorList.size());

        verify(authorRepository).findAll();
    }

    @Test
    void testFindAll_whenNoAuthors_shouldReturnEmptyIterable() {
        // Arrange
        when(authorRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Iterable<Author> result = authorService.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }

    // ========================================
    // FIND BY AUTHOR NUMBER TESTS
    // ========================================

    @Test
    void testFindByAuthorNumber_whenExists_shouldReturnAuthor() {
        // Arrange
        Long authorNumber = 1L;
        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = authorService.findByAuthorNumber(authorNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());
        verify(authorRepository).findByAuthorNumber(authorNumber);
    }

    @Test
    void testFindByAuthorNumber_whenNotExists_shouldReturnEmpty() {
        // Arrange
        Long authorNumber = 999L;
        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorService.findByAuthorNumber(authorNumber);

        // Assert
        assertFalse(result.isPresent());
    }

    // ========================================
    // FIND BY NAME TESTS
    // ========================================

    @Test
    void testFindByName_whenAuthorsExist_shouldReturnList() {
        // Arrange
        String name = "John";
        when(authorRepository.searchByNameNameStartsWith(name))
                .thenReturn(Arrays.asList(testAuthor));

        // Act
        List<Author> result = authorService.findByName(name);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAuthor, result.get(0));
        verify(authorRepository).searchByNameNameStartsWith(name);
    }

    @Test
    void testFindByName_whenNoAuthors_shouldReturnEmptyList() {
        // Arrange
        when(authorRepository.searchByNameNameStartsWith(anyString()))
                .thenReturn(Collections.emptyList());

        // Act
        List<Author> result = authorService.findByName("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // CREATE TESTS - Photo URI Validation Logic
    // ========================================

    @Test
    void testCreate_withPhotoAndPhotoURI_shouldCreateAuthorWithPhoto() {
        // Arrange
        MultipartFile photo = mock(MultipartFile.class);
        createRequest.setPhoto(photo);
        createRequest.setPhotoURI("photo.jpg");

        when(mapper.create(createRequest)).thenReturn(testAuthor);
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = authorService.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testAuthor, result);

        // Verify photo was not cleared (both present)
        assertNotNull(createRequest.getPhoto());
        assertNotNull(createRequest.getPhotoURI());

        verify(mapper).create(createRequest);
        verify(authorRepository).save(testAuthor);
    }

    @Test
    void testCreate_withPhotoButNoPhotoURI_shouldClearPhoto() {
        // Arrange
        MultipartFile photo = mock(MultipartFile.class);
        createRequest.setPhoto(photo);
        createRequest.setPhotoURI(null);

        when(mapper.create(createRequest)).thenReturn(testAuthor);
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = authorService.create(createRequest);

        // Assert
        assertNotNull(result);

        // Verify BOTH were cleared (inconsistent state)
        assertNull(createRequest.getPhoto());
        assertNull(createRequest.getPhotoURI());

        verify(mapper).create(createRequest);
        verify(authorRepository).save(testAuthor);
    }

    @Test
    void testCreate_withPhotoURIButNoPhoto_shouldClearPhotoURI() {
        // Arrange
        createRequest.setPhoto(null);
        createRequest.setPhotoURI("photo.jpg");

        when(mapper.create(createRequest)).thenReturn(testAuthor);
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = authorService.create(createRequest);

        // Assert
        assertNotNull(result);

        // Verify BOTH were cleared (inconsistent state)
        assertNull(createRequest.getPhoto());
        assertNull(createRequest.getPhotoURI());

        verify(mapper).create(createRequest);
    }

    @Test
    void testCreate_withoutPhotoAndPhotoURI_shouldCreateAuthorWithoutPhoto() {
        // Arrange
        createRequest.setPhoto(null);
        createRequest.setPhotoURI(null);

        when(mapper.create(createRequest)).thenReturn(testAuthor);
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = authorService.create(createRequest);

        // Assert
        assertNotNull(result);
        assertNull(createRequest.getPhoto());
        assertNull(createRequest.getPhotoURI());
    }

    // ========================================
    // PARTIAL UPDATE TESTS - Business Logic
    // ========================================

    @Test
    void testPartialUpdate_whenAuthorExists_shouldUpdateAndSave() {
        // Arrange
        Long authorNumber = 1L;
        long desiredVersion = 1L;

        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = authorService.partialUpdate(authorNumber, updateRequest, desiredVersion);

        // Assert
        assertNotNull(result);
        verify(authorRepository).findByAuthorNumber(authorNumber);
        verify(authorRepository).save(testAuthor);
    }

    @Test
    void testPartialUpdate_whenAuthorNotExists_shouldThrowNotFoundException() {
        // Arrange
        Long authorNumber = 999L;
        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authorService.partialUpdate(authorNumber, updateRequest, 1L)
        );

        verify(authorRepository).findByAuthorNumber(authorNumber);
        verify(authorRepository, never()).save(any());
    }

    @Test
    void testPartialUpdate_withPhotoAndPhotoURI_shouldUpdate() {
        // Arrange
        Long authorNumber = 1L;
        MultipartFile photo = mock(MultipartFile.class);
        updateRequest.setPhoto(photo);
        updateRequest.setPhotoURI("new-photo.jpg");

        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = authorService.partialUpdate(authorNumber, updateRequest, 1L);

        // Assert
        assertNotNull(result);
        assertNotNull(updateRequest.getPhoto());
        assertNotNull(updateRequest.getPhotoURI());
    }

    @Test
    void testPartialUpdate_withPhotoButNoPhotoURI_shouldClearBoth() {
        // Arrange
        Long authorNumber = 1L;
        MultipartFile photo = mock(MultipartFile.class);
        updateRequest.setPhoto(photo);
        updateRequest.setPhotoURI(null);

        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author result = authorService.partialUpdate(authorNumber, updateRequest, 1L);

        // Assert
        assertNotNull(result);

        // Verify inconsistent state was cleared
        assertNull(updateRequest.getPhoto());
        assertNull(updateRequest.getPhotoURI());
    }

    @Test
    void testPartialUpdate_withStaleVersion_shouldPropagateException() {
        // Arrange
        Long authorNumber = 1L;
        long wrongVersion = 999L;

        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));

        // Act & Assert
        // applyPatch will throw StaleObjectStateException
        assertThrows(StaleObjectStateException.class,
                () -> authorService.partialUpdate(authorNumber, updateRequest, wrongVersion)
        );

        verify(authorRepository, never()).save(any());
    }

    // ========================================
    // TOP AUTHORS BY LENDINGS TESTS
    // ========================================

    @Test
    void testFindTopAuthorByLendings_shouldReturnTop5() {
        // Arrange
        AuthorLendingView view1 = new AuthorLendingView("Author 1", 100L);
        AuthorLendingView view2 = new AuthorLendingView("Author 2", 90L);

        Page<AuthorLendingView> page = new PageImpl<>(Arrays.asList(view1, view2));

        when(authorRepository.findTopAuthorByLendings(any(PageRequest.class)))
                .thenReturn(page);

        // Act
        List<AuthorLendingView> result = authorService.findTopAuthorByLendings();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(view1, result.get(0));
        assertEquals(view2, result.get(1));

        // Verify PageRequest for top 5
        verify(authorRepository).findTopAuthorByLendings(PageRequest.of(0, 5));
    }

    @Test
    void testFindTopAuthorByLendings_whenNoAuthors_shouldReturnEmptyList() {
        // Arrange
        Page<AuthorLendingView> emptyPage = new PageImpl<>(Collections.emptyList());
        when(authorRepository.findTopAuthorByLendings(any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        List<AuthorLendingView> result = authorService.findTopAuthorByLendings();

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
        Book book1 = mock(Book.class);
        Book book2 = mock(Book.class);

        when(bookRepository.findBooksByAuthorNumber(authorNumber))
                .thenReturn(Arrays.asList(book1, book2));

        // Act
        List<Book> result = authorService.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository).findBooksByAuthorNumber(authorNumber);
    }

    @Test
    void testFindBooksByAuthorNumber_whenNoBooks_shouldReturnEmptyList() {
        // Arrange
        Long authorNumber = 1L;
        when(bookRepository.findBooksByAuthorNumber(authorNumber))
                .thenReturn(Collections.emptyList());

        // Act
        List<Book> result = authorService.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // FIND CO-AUTHORS TESTS
    // ========================================

    @Test
    void testFindCoAuthorsByAuthorNumber_shouldReturnCoAuthors() {
        // Arrange
        Long authorNumber = 1L;
        Author coAuthor1 = new Author("Co-Author 1", "Bio 1", null);
        coAuthor1.setAuthorNumber(2L);
        Author coAuthor2 = new Author("Co-Author 2", "Bio 2", null);
        coAuthor2.setAuthorNumber(3L);

        when(authorRepository.findCoAuthorsByAuthorNumber(authorNumber))
                .thenReturn(Arrays.asList(coAuthor1, coAuthor2));

        // Act
        List<Author> result = authorService.findCoAuthorsByAuthorNumber(authorNumber);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(authorRepository).findCoAuthorsByAuthorNumber(authorNumber);
    }

    // ========================================
    // REMOVE AUTHOR PHOTO TESTS
    // ========================================

    @Test
    void testRemoveAuthorPhoto_whenAuthorHasPhoto_shouldRemoveAndDelete() {
        // Arrange
        Long authorNumber = 1L;
        long desiredVersion = 1L;
        testAuthor.setPhoto("photo.jpg");

        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
        doNothing().when(photoRepository).deleteByPhotoFile("photo.jpg");

        // Act
        Optional<Author> result = authorService.removeAuthorPhoto(authorNumber, desiredVersion);

        // Assert
        assertTrue(result.isPresent());
        verify(authorRepository).findByAuthorNumber(authorNumber);
        verify(authorRepository).save(testAuthor);
        verify(photoRepository).deleteByPhotoFile("photo.jpg");
    }

    @Test
    void testRemoveAuthorPhoto_whenAuthorNotExists_shouldThrowNotFoundException() {
        // Arrange
        Long authorNumber = 999L;
        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authorService.removeAuthorPhoto(authorNumber, 1L)
        );

        verify(authorRepository).findByAuthorNumber(authorNumber);
        verify(authorRepository, never()).save(any());
        verify(photoRepository, never()).deleteByPhotoFile(anyString());
    }

    @Test
    void testRemoveAuthorPhoto_shouldSaveAfterRemoval() {
        // Arrange
        Long authorNumber = 1L;
        testAuthor.setPhoto("photo.jpg");

        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        authorService.removeAuthorPhoto(authorNumber, 1L);

        // Assert
        verify(authorRepository).save(testAuthor);
    }

    // ========================================
    // INTEGRATION TESTS - Multiple Layers
    // ========================================

    @Test
    void testCreateAndFind_integrationFlow_shouldWorkCorrectly() {
        // Arrange
        when(mapper.create(createRequest)).thenReturn(testAuthor);
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
        when(authorRepository.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));

        // Act
        Author created = authorService.create(createRequest);
        Optional<Author> found = authorService.findByAuthorNumber(1L);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(created, found.get());
    }

    @Test
    void testUpdateAndFind_integrationFlow_shouldReflectChanges() {
        // Arrange
        when(authorRepository.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        // Act
        Author updated = authorService.partialUpdate(1L, updateRequest, 1L);
        Optional<Author> found = authorService.findByAuthorNumber(1L);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(updated, found.get());
    }

    @Test
    void testPhotoManagement_fullCycle_shouldWork() {
        // Arrange
        Long authorNumber = 1L;
        MultipartFile photo = mock(MultipartFile.class);

        // Create with photo
        createRequest.setPhoto(photo);
        createRequest.setPhotoURI("photo.jpg");
        testAuthor.setPhoto("photo.jpg");

        when(mapper.create(createRequest)).thenReturn(testAuthor);
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));

        // Act
        Author created = authorService.create(createRequest);
        assertNotNull(created.getPhoto());

        // Remove photo
        authorService.removeAuthorPhoto(authorNumber, 1L);

        // Assert
        verify(photoRepository).deleteByPhotoFile("photo.jpg");
    }

    // ========================================
    // ERROR HANDLING & EDGE CASES
    // ========================================

    @Test
    void testPartialUpdate_saveFails_shouldPropagateException() {
        // Arrange
        Long authorNumber = 1L;
        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(any()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> authorService.partialUpdate(authorNumber, updateRequest, 1L)
        );
    }

    @Test
    void testRemoveAuthorPhoto_photoRepositoryFails_shouldPropagateException() {
        // Arrange
        Long authorNumber = 1L;
        testAuthor.setPhoto("photo.jpg");

        when(authorRepository.findByAuthorNumber(authorNumber))
                .thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
        doThrow(new RuntimeException("Storage error"))
                .when(photoRepository).deleteByPhotoFile(anyString());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> authorService.removeAuthorPhoto(authorNumber, 1L)
        );
    }
}