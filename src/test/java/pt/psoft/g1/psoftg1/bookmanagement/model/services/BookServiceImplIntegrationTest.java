package pt.psoft.g1.psoftg1.bookmanagement.model.services;

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
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.*;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration Test for BookServiceImpl
 *
 * Purpose: Test service layer WITH integration to repository and domain layers
 * Testing Strategy: Mock external dependencies (repositories), test service business logic
 * SUT: BookServiceImpl + Domain (Book, Title, Isbn, Description, Genre, Author)
 * Type: 2.3.4 - Functional opaque-box with SUT = controller+service+{domain, repository, gateways}
 *
 * Test Coverage:
 * - Service business logic (photo URI validation, request handling)
 * - Repository integration (save, find, delete operations)
 * - Domain integration (Book entity creation, validation, patching)
 * - Error handling (NotFoundException, ConflictException, validation errors)
 * - Photo management logic
 * - Top 5 books lent queries
 * - Book suggestions for readers
 * - Search functionality
 *
 * @author ARQSOFT 2025-2026
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplIntegrationTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private ReaderRepository readerRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private Genre testGenre;
    private Author testAuthor;
    private CreateBookRequest createRequest;
    private UpdateBookRequest updateRequest;

    @BeforeEach
    void setUp() {
        testGenre = new Genre(1L, "Fiction");

        testAuthor = new Author("John Doe", "Famous author", null);
        testAuthor.setAuthorNumber(1L);

        testBook = new Book(
                "9780306406157",
                "Test Book",
                "Test description",
                testGenre,
                Arrays.asList(testAuthor),
                null
        );
        testBook.pk = 1L;

        createRequest = new CreateBookRequest();
        createRequest.setTitle("New Book");
        createRequest.setDescription("New Description");
        createRequest.setGenre("Fiction");
        createRequest.setAuthors(Arrays.asList(1L));

        updateRequest = new UpdateBookRequest();
        updateRequest.setTitle("Updated Title");
    }

    // ========================================
    // CREATE TESTS - Photo URI Validation Logic
    // ========================================

    @Test
    void testCreate_withPhotoAndPhotoURI_shouldCreateBookWithPhoto() {
        // Arrange
        MultipartFile photo = mock(MultipartFile.class);
        createRequest.setPhoto(photo);
        createRequest.setPhotoURI("photo.jpg");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(testGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book result = bookService.create(createRequest, "9780306406157");

        // Assert
        assertNotNull(result);
        assertEquals(testBook, result);

        // Verify photo was not cleared (both present)
        assertNotNull(createRequest.getPhoto());
        assertNotNull(createRequest.getPhotoURI());

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testCreate_withPhotoButNoPhotoURI_shouldClearBoth() {
        // Arrange
        MultipartFile photo = mock(MultipartFile.class);
        createRequest.setPhoto(photo);
        createRequest.setPhotoURI(null);

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(testGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book result = bookService.create(createRequest, "9780306406157");

        // Assert
        assertNotNull(result);

        // Verify BOTH were cleared (inconsistent state)
        assertNull(createRequest.getPhoto());
        assertNull(createRequest.getPhotoURI());

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testCreate_withPhotoURIButNoPhoto_shouldClearBoth() {
        // Arrange
        createRequest.setPhoto(null);
        createRequest.setPhotoURI("photo.jpg");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(testGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book result = bookService.create(createRequest, "9780306406157");

        // Assert
        assertNotNull(result);

        // Verify BOTH were cleared (inconsistent state)
        assertNull(createRequest.getPhoto());
        assertNull(createRequest.getPhotoURI());
    }

    @Test
    void testCreate_withoutPhotoAndPhotoURI_shouldCreateBookWithoutPhoto() {
        // Arrange
        createRequest.setPhoto(null);
        createRequest.setPhotoURI(null);

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(testGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book result = bookService.create(createRequest, "9780306406157");

        // Assert
        assertNotNull(result);
        assertNull(createRequest.getPhoto());
        assertNull(createRequest.getPhotoURI());
    }

    @Test
    void testCreate_withDuplicateIsbn_shouldThrowConflictException() {
        // Arrange
        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));

        // Act & Assert
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bookService.create(createRequest, "9780306406157")
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void testCreate_withNonExistentGenre_shouldThrowNotFoundException() {
        // Arrange
        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.create(createRequest, "9780306406157")
        );

        verify(bookRepository, never()).save(any());
    }

    @Test
    void testCreate_withNonExistentAuthor_shouldSkipAuthor() {
        // Arrange
        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.empty());
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(testGenre));

        // Act & Assert
        // Should throw because no valid authors (empty list)
        assertThrows(IllegalArgumentException.class,
                () -> bookService.create(createRequest, "9780306406157")
        );
    }

    @Test
    void testCreate_withMultipleAuthors_someNotExist_shouldUseOnlyValid() {
        // Arrange
        Author author2 = new Author("Jane Smith", "Bio", null);
        author2.setAuthorNumber(2L);

        createRequest.setAuthors(Arrays.asList(1L, 999L, 2L)); // 999L doesn't exist

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.findByAuthorNumber(999L)).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(2L)).thenReturn(Optional.of(author2));
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(testGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book result = bookService.create(createRequest, "9780306406157");

        // Assert
        assertNotNull(result);
        verify(bookRepository).save(any(Book.class));
    }

    // ========================================
    // UPDATE TESTS - Business Logic
    // ========================================

    @Test
    void testUpdate_whenBookExists_shouldUpdateAndSave() {
        // Arrange
        updateRequest.setIsbn("9780306406157");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.update(updateRequest, "0");

        // Assert
        assertNotNull(result);
        verify(bookRepository).findByIsbn("9780306406157");
        verify(bookRepository).save(testBook);
    }

    @Test
    void testUpdate_whenBookNotExists_shouldThrowNotFoundException() {
        // Arrange
        updateRequest.setIsbn("9999999999999");

        when(bookRepository.findByIsbn("9999999999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.update(updateRequest, "0")
        );

        verify(bookRepository, never()).save(any());
    }

    @Test
    void testUpdate_withPhotoAndPhotoURI_shouldUpdate() {
        // Arrange
        MultipartFile photo = mock(MultipartFile.class);
        updateRequest.setIsbn("9780306406157");
        updateRequest.setPhoto(photo);
        updateRequest.setPhotoURI("new-photo.jpg");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.update(updateRequest, "0");

        // Assert
        assertNotNull(result);
        assertNotNull(updateRequest.getPhoto());
        assertNotNull(updateRequest.getPhotoURI());
    }

    @Test
    void testUpdate_withPhotoButNoPhotoURI_shouldClearBoth() {
        // Arrange
        MultipartFile photo = mock(MultipartFile.class);
        updateRequest.setIsbn("9780306406157");
        updateRequest.setPhoto(photo);
        updateRequest.setPhotoURI(null);

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.update(updateRequest, "0");

        // Assert
        assertNotNull(result);

        // Verify inconsistent state was cleared
        assertNull(updateRequest.getPhoto());
        assertNull(updateRequest.getPhotoURI());
    }

    @Test
    void testUpdate_withStaleVersion_shouldPropagateException() {
        // Arrange
        updateRequest.setIsbn("9780306406157");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));

        // Act & Assert
        // applyPatch will throw StaleObjectStateException with wrong version
        assertThrows(StaleObjectStateException.class,
                () -> bookService.update(updateRequest, "999")
        );

        verify(bookRepository, never()).save(any());
    }

    @Test
    void testUpdate_withNewGenre_shouldUpdateGenre() {
        // Arrange
        Genre newGenre = new Genre(2L, "Mystery");
        updateRequest.setIsbn("9780306406157");
        updateRequest.setGenre("Mystery");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));
        when(genreRepository.findByString("Mystery")).thenReturn(Optional.of(newGenre));
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.update(updateRequest, "0");

        // Assert
        assertNotNull(result);
        verify(genreRepository).findByString("Mystery");
    }

    @Test
    void testUpdate_withNonExistentGenre_shouldThrowNotFoundException() {
        // Arrange
        updateRequest.setIsbn("9780306406157");
        updateRequest.setGenre("NonExistent");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));
        when(genreRepository.findByString("NonExistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.update(updateRequest, "0")
        );
    }

    // ========================================
    // FIND BY ISBN TESTS
    // ========================================

    @Test
    void testFindByIsbn_whenExists_shouldReturnBook() {
        // Arrange
        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));

        // Act
        Book result = bookService.findByIsbn("9780306406157");

        // Assert
        assertNotNull(result);
        assertEquals(testBook, result);
        verify(bookRepository).findByIsbn("9780306406157");
    }

    @Test
    void testFindByIsbn_whenNotExists_shouldThrowNotFoundException() {
        // Arrange
        when(bookRepository.findByIsbn("9999999999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.findByIsbn("9999999999999")
        );
    }

    // ========================================
    // FIND BY GENRE TESTS
    // ========================================

    @Test
    void testFindByGenre_whenBooksExist_shouldReturnList() {
        // Arrange
        when(bookRepository.findByGenre("Fiction")).thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = bookService.findByGenre("Fiction");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
        verify(bookRepository).findByGenre("Fiction");
    }

    @Test
    void testFindByGenre_whenNoBooks_shouldReturnEmptyList() {
        // Arrange
        when(bookRepository.findByGenre("NonExistent")).thenReturn(Collections.emptyList());

        // Act
        List<Book> result = bookService.findByGenre("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // FIND BY TITLE TESTS
    // ========================================

    @Test
    void testFindByTitle_whenBooksExist_shouldReturnList() {
        // Arrange
        when(bookRepository.findByTitle("Test Book")).thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = bookService.findByTitle("Test Book");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findByTitle("Test Book");
    }

    @Test
    void testFindByTitle_whenNoBooks_shouldReturnEmptyList() {
        // Arrange
        when(bookRepository.findByTitle("NonExistent")).thenReturn(Collections.emptyList());

        // Act
        List<Book> result = bookService.findByTitle("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // FIND BY AUTHOR NAME TESTS
    // ========================================

    @Test
    void testFindByAuthorName_whenBooksExist_shouldReturnList() {
        // Arrange
        when(bookRepository.findByAuthorName("John%")).thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = bookService.findByAuthorName("John");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findByAuthorName("John%");
    }

    // ========================================
    // TOP 5 BOOKS LENT TESTS
    // ========================================

    @Test
    void testFindTop5BooksLent_shouldReturnTop5() {
        // Arrange
        BookCountDTO dto1 = mock(BookCountDTO.class);
        BookCountDTO dto2 = mock(BookCountDTO.class);

        Page<BookCountDTO> page = new PageImpl<>(Arrays.asList(dto1, dto2));

        when(bookRepository.findTop5BooksLent(any(LocalDate.class), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        List<BookCountDTO> result = bookService.findTop5BooksLent();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(bookRepository).findTop5BooksLent(any(LocalDate.class), eq(PageRequest.of(0, 5)));
    }

    @Test
    void testFindTop5BooksLent_whenNoBooks_shouldReturnEmptyList() {
        // Arrange
        Page<BookCountDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookRepository.findTop5BooksLent(any(LocalDate.class), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        List<BookCountDTO> result = bookService.findTop5BooksLent();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // REMOVE BOOK PHOTO TESTS
    // ========================================

    @Test
    void testRemoveBookPhoto_whenBookHasPhoto_shouldRemoveAndDelete() {
        // Arrange
        testBook.setPhoto("book-photo.jpg");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));
        when(bookRepository.save(testBook)).thenReturn(testBook);
        doNothing().when(photoRepository).deleteByPhotoFile("book-photo.jpg");

        // Act
        Book result = bookService.removeBookPhoto("9780306406157", 0L);

        // Assert
        assertNotNull(result);
        verify(bookRepository).findByIsbn("9780306406157");
        verify(bookRepository).save(testBook);
        verify(photoRepository).deleteByPhotoFile("book-photo.jpg");
    }

    @Test
    void testRemoveBookPhoto_whenBookNotExists_shouldThrowNotFoundException() {
        // Arrange
        when(bookRepository.findByIsbn("9999999999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.removeBookPhoto("9999999999999", 0L)
        );

        verify(bookRepository, never()).save(any());
        verify(photoRepository, never()).deleteByPhotoFile(anyString());
    }

    @Test
    void testRemoveBookPhoto_whenBookHasNoPhoto_shouldThrowNotFoundException() {
        // Arrange
        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.removeBookPhoto("9780306406157", 0L)
        );

        verify(photoRepository, never()).deleteByPhotoFile(anyString());
    }

    // ========================================
    // GET BOOKS SUGGESTIONS TESTS
    // ========================================



    @Test
    void testGetBooksSuggestionsForReader_whenReaderNotExists_shouldThrowNotFoundException() {
        // Arrange
        when(readerRepository.findByReaderNumber("R999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.getBooksSuggestionsForReader("R999")
        );
    }

    @Test
    void testGetBooksSuggestionsForReader_whenReaderHasNoInterests_shouldThrowNotFoundException() {
        // Arrange
        ReaderDetails reader = mock(ReaderDetails.class);
        when(reader.getInterestList()).thenReturn(Collections.emptyList());

        when(readerRepository.findByReaderNumber("R001")).thenReturn(Optional.of(reader));

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> bookService.getBooksSuggestionsForReader("R001")
        );
    }

    // ========================================
    // SEARCH BOOKS TESTS
    // ========================================

    @Test
    void testSearchBooks_withQuery_shouldReturnResults() {
        // Arrange
        pt.psoft.g1.psoftg1.shared.services.Page page =
                new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);
        SearchBooksQuery query = new SearchBooksQuery("Test", "Fiction", "John");

        when(bookRepository.searchBooks(page, query)).thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = bookService.searchBooks(page, query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).searchBooks(page, query);
    }

    @Test
    void testSearchBooks_withNullPage_shouldUseDefaultPage() {
        // Arrange
        SearchBooksQuery query = new SearchBooksQuery("Test", "Fiction", "John");

        when(bookRepository.searchBooks(any(), eq(query))).thenReturn(Arrays.asList(testBook));

        // Act
        List<Book> result = bookService.searchBooks(null, query);

        // Assert
        assertNotNull(result);
        verify(bookRepository).searchBooks(any(), eq(query));
    }

    @Test
    void testSearchBooks_withNullQuery_shouldUseDefaultQuery() {
        // Arrange
        pt.psoft.g1.psoftg1.shared.services.Page page =
                new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);

        when(bookRepository.searchBooks(eq(page), any())).thenReturn(Collections.emptyList());

        // Act
        List<Book> result = bookService.searchBooks(page, null);

        // Assert
        assertNotNull(result);
        verify(bookRepository).searchBooks(eq(page), any());
    }

    // ========================================
    // SAVE TESTS
    // ========================================

    @Test
    void testSave_shouldCallRepository() {
        // Arrange
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book result = bookService.save(testBook);

        // Assert
        assertNotNull(result);
        assertEquals(testBook, result);
        verify(bookRepository).save(testBook);
    }

    // ========================================
    // INTEGRATION TESTS - Multiple Layers
    // ========================================



    @Test
    void testPhotoManagement_fullCycle_shouldWork() {
        // Arrange
        MultipartFile photo = mock(MultipartFile.class);

        // Create with photo
        createRequest.setPhoto(photo);
        createRequest.setPhotoURI("photo.jpg");
        testBook.setPhoto("photo.jpg");

        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.empty());
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(testAuthor));
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(testGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        Book created = bookService.create(createRequest, "9780306406157");
        assertNotNull(created.getPhoto());

        // Remove photo
        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(testBook));
        bookService.removeBookPhoto("9780306406157", 0L);

        // Assert
        verify(photoRepository).deleteByPhotoFile("photo.jpg");
    }
}