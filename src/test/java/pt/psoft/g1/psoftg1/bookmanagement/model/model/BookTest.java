package pt.psoft.g1.psoftg1.bookmanagement.model.model;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.Description;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transparent-Box (White-Box) Tests for Book domain class
 *
 * Purpose: Test Book entity with KNOWLEDGE of its internal implementation
 * Testing Strategy: Complete branch coverage, all conditional paths, boundary analysis
 * SUT: Book class (domain model)
 * Type: 2.3.2 - Functional transparent-box with SUT = domain classes
 *
 * Test Coverage:
 * - Constructor validation (all parameters required)
 * - All validation paths (null checks, business rules)
 * - Version control and concurrency (applyPatch, removePhoto)
 * - All setter methods with validation
 * - Photo management (optional field)
 * - Authors management (list validation)
 * - Genre management (required field)
 * - Edge cases and error conditions
 * - Business logic branches
 *
 * @author ARQSOFT 2025-2026
 */
class BookTest {

    private static final String VALID_ISBN = "9780306406157";
    private static final String VALID_TITLE = "Test Book Title";
    private static final String VALID_DESCRIPTION = "This is a test book description.";
    private static final String VALID_PHOTO_URI = "book-photo.jpg";

    private Genre testGenre;
    private Author testAuthor;
    private List<Author> testAuthors;
    private Book book;

    @BeforeEach
    void setUp() {
        testGenre = new Genre(1L, "Fiction");

        testAuthor = new Author("John Doe", "Famous author biography", null);
        testAuthor.setAuthorNumber(1L);

        testAuthors = Arrays.asList(testAuthor);

        book = new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, null);
    }

    // ========================================
    // CONSTRUCTOR TESTS - All Validation Paths
    // ========================================

    @Test
    void testConstructor_withValidData_shouldCreateBook() {
        // Act
        Book newBook = new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, VALID_PHOTO_URI);

        // Assert
        assertNotNull(newBook);
        assertEquals(VALID_ISBN, newBook.getIsbn().toString());
        assertEquals(VALID_TITLE, newBook.getTitle().toString());
        assertEquals(VALID_DESCRIPTION, newBook.getDescription().toString());
        assertEquals(testGenre, newBook.getGenre());
        assertEquals(testAuthors, newBook.getAuthors());
        assertEquals(VALID_PHOTO_URI, newBook.getPhoto().getPhotoFile());
        assertEquals(0L, newBook.getVersion());
        assertNull(newBook.getPk()); // Not set yet
    }

    @Test
    void testConstructor_withNullPhoto_shouldCreateBookWithoutPhoto() {
        // Act
        Book newBook = new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, null);

        // Assert
        assertNotNull(newBook);
        assertEquals(VALID_ISBN, newBook.getIsbn().toString());
        assertNull(newBook.getPhoto());
        assertEquals(0L, newBook.getVersion());
    }

    @Test
    void testConstructor_withMultipleAuthors_shouldStoreAllAuthors() {
        // Arrange
        Author author2 = new Author("Jane Smith", "Another author", null);
        author2.setAuthorNumber(2L);
        List<Author> multipleAuthors = Arrays.asList(testAuthor, author2);

        // Act
        Book newBook = new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, multipleAuthors, null);

        // Assert
        assertNotNull(newBook);
        assertEquals(2, newBook.getAuthors().size());
        assertTrue(newBook.getAuthors().contains(testAuthor));
        assertTrue(newBook.getAuthors().contains(author2));
    }

    // ========================================
    // VALIDATION TESTS - ISBN
    // ========================================

    @Test
    void testConstructor_withInvalidIsbn_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book("invalid", VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, null)
        );

        assertTrue(exception.getMessage().contains("ISBN"));
    }

    @Test
    void testConstructor_withNullIsbn_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(null, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, null)
        );

        assertTrue(exception.getMessage().contains("Isbn cannot be null"));
    }

    // ========================================
    // VALIDATION TESTS - Title
    // ========================================

    @Test
    void testConstructor_withNullTitle_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(VALID_ISBN, null, VALID_DESCRIPTION, testGenre, testAuthors, null)
        );

        assertTrue(exception.getMessage().contains("Title cannot be null"));
    }

    @Test
    void testConstructor_withBlankTitle_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(VALID_ISBN, "   ", VALID_DESCRIPTION, testGenre, testAuthors, null)
        );

        assertTrue(exception.getMessage().contains("Title cannot be blank"));
    }

    @Test
    void testConstructor_withTooLongTitle_shouldThrowException() {
        // Arrange
        String tooLongTitle = "a".repeat(Title.TITLE_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(VALID_ISBN, tooLongTitle, VALID_DESCRIPTION, testGenre, testAuthors, null)
        );

        assertTrue(exception.getMessage().contains("maximum"));
    }

    @Test
    void testSetTitle_withValidTitle_shouldUpdateTitle() {
        // Arrange
        Title newTitle = new Title("Updated Title");

        // Act
        book.setTitle(newTitle);

        // Assert
        assertEquals("Updated Title", book.getTitle().toString());
    }

    // ========================================
    // VALIDATION TESTS - Description
    // ========================================

    @Test
    void testConstructor_withNullDescription_shouldAllowNullDescription() {
        // Act
        Book newBook = new Book(VALID_ISBN, VALID_TITLE, null, testGenre, testAuthors, null);

        // Assert
        assertNull(newBook.getDescription().getDescription());
    }

    @Test
    void testConstructor_withBlankDescription_shouldAllowNullDescription() {
        // Act
        Book newBook = new Book(VALID_ISBN, VALID_TITLE, "   ", testGenre, testAuthors, null);

        // Assert
        assertNull(newBook.getDescription().getDescription());
    }

    @Test
    void testConstructor_withTooLongDescription_shouldThrowException() {
        // Arrange
        String tooLongDesc = "a".repeat(Description.DESC_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(VALID_ISBN, VALID_TITLE, tooLongDesc, testGenre, testAuthors, null)
        );

        assertTrue(exception.getMessage().contains("maximum"));
    }

    @Test
    void testSetDescription_withValidDescription_shouldUpdateDescription() {
        // Arrange
        Description newDesc = new Description("Updated description");

        // Act
        book.setDescription(newDesc);

        // Assert
        assertEquals("Updated description", book.getDescription().toString());
    }

    // ========================================
    // VALIDATION TESTS - Genre
    // ========================================

    @Test
    void testConstructor_withNullGenre_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, null, testAuthors, null)
        );

        assertEquals("Genre cannot be null", exception.getMessage());
    }

    @Test
    void testSetGenre_withNullGenre_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> book.setGenre(null)
        );

        assertEquals("Genre cannot be null", exception.getMessage());
    }

    @Test
    void testSetGenre_withValidGenre_shouldUpdateGenre() {
        // Arrange
        Genre newGenre = new Genre(2L, "Science Fiction");

        // Act
        book.setGenre(newGenre);

        // Assert
        assertEquals(newGenre, book.getGenre());
        assertEquals("Science Fiction", book.getGenre().getGenre());
    }

    // ========================================
    // VALIDATION TESTS - Authors
    // ========================================

    @Test
    void testConstructor_withNullAuthors_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, null, null)
        );

        assertEquals("Authors cannot be empty", exception.getMessage());
    }

    @Test
    void testConstructor_withEmptyAuthors_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, Collections.emptyList(), null)
        );

        assertEquals("Authors cannot be empty", exception.getMessage());
    }

    @Test
    void testSetAuthors_withNullAuthors_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> book.setAuthors(null)
        );

        assertEquals("Authors cannot be empty", exception.getMessage());
    }

    @Test
    void testSetAuthors_withEmptyList_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> book.setAuthors(Collections.emptyList())
        );

        assertEquals("Authors cannot be empty", exception.getMessage());
    }

    @Test
    void testSetAuthors_withValidAuthors_shouldUpdateAuthors() {
        // Arrange
        Author newAuthor = new Author("New Author", "Bio", null);
        newAuthor.setAuthorNumber(2L);
        List<Author> newAuthors = Arrays.asList(newAuthor);

        // Act
        book.setAuthors(newAuthors);

        // Assert
        assertEquals(1, book.getAuthors().size());
        assertEquals(newAuthor, book.getAuthors().get(0));
    }

    // ========================================
    // GETTER/SETTER TESTS - PK & Version
    // ========================================

    @Test
    void testGetPk_shouldReturnPk() {
        // Arrange
        book.pk = 123L;

        // Act
        Long pk = book.getPk();

        // Assert
        assertEquals(123L, pk);
    }

    @Test
    void testGetVersion_shouldReturnVersion() {
        // Act
        Long version = book.getVersion();

        // Assert
        assertEquals(0L, version);
    }

    @Test
    void testVersionInitialization_shouldAlwaysBeZero() {
        // Arrange & Act
        Book newBook = new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, null);

        // Assert
        assertEquals(0L, newBook.getVersion());
    }

    // ========================================
    // APPLY PATCH TESTS - All Branches
    // ========================================

    @Test
    void testApplyPatch_withCorrectVersion_shouldUpdateAllFields() {
        // Arrange
        book.pk = 1L;
        long currentVersion = book.getVersion();

        Author newAuthor = new Author("Updated Author", "Bio", null);
        newAuthor.setAuthorNumber(2L);
        Genre newGenre = new Genre(2L, "Mystery");

        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");
        request.setGenreObj(newGenre);
        request.setAuthorObjList(Arrays.asList(newAuthor));
        request.setPhotoURI("new-photo.jpg");

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals("Updated Title", book.getTitle().toString());
        assertEquals("Updated Description", book.getDescription().toString());
        assertEquals(newGenre, book.getGenre());
        assertEquals(1, book.getAuthors().size());
        assertEquals(newAuthor, book.getAuthors().get(0));
        assertEquals("new-photo.jpg", book.getPhoto().getPhotoFile());
    }

    @Test
    void testApplyPatch_withIncorrectVersion_shouldThrowStaleObjectException() {
        // Arrange
        book.pk = 1L;

        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated Title");

        // Act & Assert
        assertThrows(StaleObjectStateException.class,
                () -> book.applyPatch(999L, request)
        );
    }

    @Test
    void testApplyPatch_withOnlyTitle_shouldUpdateOnlyTitle() {
        // Arrange
        String originalDescription = book.getDescription().toString();
        Genre originalGenre = book.getGenre();
        List<Author> originalAuthors = book.getAuthors();
        long currentVersion = book.getVersion();

        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Only Title Updated");
        // All other fields are null

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals("Only Title Updated", book.getTitle().toString());
        assertEquals(originalDescription, book.getDescription().toString()); // Unchanged
        assertEquals(originalGenre, book.getGenre()); // Unchanged
        assertEquals(originalAuthors, book.getAuthors()); // Unchanged
        assertNull(book.getPhoto()); // Unchanged
    }

    @Test
    void testApplyPatch_withOnlyDescription_shouldUpdateOnlyDescription() {
        // Arrange
        String originalTitle = book.getTitle().toString();
        long currentVersion = book.getVersion();

        UpdateBookRequest request = new UpdateBookRequest();
        request.setDescription("Only Description Updated");
        // title, genre, authors, photo are null

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalTitle, book.getTitle().toString()); // Unchanged
        assertEquals("Only Description Updated", book.getDescription().toString());
    }

    @Test
    void testApplyPatch_withOnlyGenre_shouldUpdateOnlyGenre() {
        // Arrange
        String originalTitle = book.getTitle().toString();
        long currentVersion = book.getVersion();

        Genre newGenre = new Genre(3L, "Thriller");

        UpdateBookRequest request = new UpdateBookRequest();
        request.setGenreObj(newGenre);
        // title, description, authors, photo are null

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalTitle, book.getTitle().toString()); // Unchanged
        assertEquals(newGenre, book.getGenre());
    }

    @Test
    void testApplyPatch_withOnlyAuthors_shouldUpdateOnlyAuthors() {
        // Arrange
        String originalTitle = book.getTitle().toString();
        long currentVersion = book.getVersion();

        Author newAuthor = new Author("New Author", "Bio", null);
        newAuthor.setAuthorNumber(3L);

        UpdateBookRequest request = new UpdateBookRequest();
        request.setAuthorObjList(Arrays.asList(newAuthor));
        // title, description, genre, photo are null

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalTitle, book.getTitle().toString()); // Unchanged
        assertEquals(1, book.getAuthors().size());
        assertEquals(newAuthor, book.getAuthors().get(0));
    }

    @Test
    void testApplyPatch_withOnlyPhoto_shouldUpdateOnlyPhoto() {
        // Arrange
        String originalTitle = book.getTitle().toString();
        long currentVersion = book.getVersion();

        UpdateBookRequest request = new UpdateBookRequest();
        request.setPhotoURI("only-photo-updated.jpg");
        // title, description, genre, authors are null

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalTitle, book.getTitle().toString()); // Unchanged
        assertEquals("only-photo-updated.jpg", book.getPhoto().getPhotoFile());
    }

    @Test
    void testApplyPatch_withAllNullFields_shouldNotUpdateAnything() {
        // Arrange
        String originalTitle = book.getTitle().toString();
        String originalDescription = book.getDescription().toString();
        Genre originalGenre = book.getGenre();
        List<Author> originalAuthors = book.getAuthors();
        long currentVersion = book.getVersion();

        UpdateBookRequest request = new UpdateBookRequest();
        // All fields null

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalTitle, book.getTitle().toString());
        assertEquals(originalDescription, book.getDescription().toString());
        assertEquals(originalGenre, book.getGenre());
        assertEquals(originalAuthors, book.getAuthors());
        assertNull(book.getPhoto());
    }

    @Test
    void testApplyPatch_withPhotoUpdate_shouldReplacePhoto() {
        // Arrange
        book.setPhoto(VALID_PHOTO_URI);
        assertNotNull(book.getPhoto());
        long currentVersion = book.getVersion();

        UpdateBookRequest request = new UpdateBookRequest();
        request.setPhotoURI("new-photo.jpg");

        // Act
        book.applyPatch(currentVersion, request);

        // Assert
        assertEquals("new-photo.jpg", book.getPhoto().getPhotoFile());
    }

    // ========================================
    // REMOVE PHOTO TESTS - Version Control
    // ========================================

    @Test
    void testRemovePhoto_withCorrectVersion_shouldRemovePhoto() {
        // Arrange
        book.setPhoto(VALID_PHOTO_URI);
        assertNotNull(book.getPhoto());
        long currentVersion = book.getVersion();

        // Act
        book.removePhoto(currentVersion);

        // Assert
        assertNull(book.getPhoto());
    }

    @Test
    void testRemovePhoto_withIncorrectVersion_shouldThrowConflictException() {
        // Arrange
        book.setPhoto(VALID_PHOTO_URI);

        // Act & Assert
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> book.removePhoto(999L)
        );

        assertTrue(exception.getMessage().contains("version"));
        // Photo should remain unchanged
        assertNotNull(book.getPhoto());
    }

    @Test
    void testRemovePhoto_whenNoPhoto_shouldStillSucceedWithCorrectVersion() {
        // Arrange
        assertNull(book.getPhoto());
        long currentVersion = book.getVersion();

        // Act
        book.removePhoto(currentVersion);

        // Assert
        assertNull(book.getPhoto()); // Still null
    }

    // ========================================
    // CONCURRENCY TESTS - Version Matching
    // ========================================

    @Test
    void testApplyPatch_versionMismatch_shouldNotModifyBook() {
        // Arrange
        String originalTitle = book.getTitle().toString();
        String originalDescription = book.getDescription().toString();
        book.pk = 1L;

        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Should Not Apply");
        request.setDescription("Should Not Apply");

        // Act & Assert
        assertThrows(StaleObjectStateException.class,
                () -> book.applyPatch(5L, request)
        );

        // Verify nothing changed
        assertEquals(originalTitle, book.getTitle().toString());
        assertEquals(originalDescription, book.getDescription().toString());
    }

    @Test
    void testRemovePhoto_versionMismatch_shouldNotRemovePhoto() {
        // Arrange
        book.setPhoto(VALID_PHOTO_URI);

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> book.removePhoto(5L)
        );

        // Verify photo still exists
        assertNotNull(book.getPhoto());
        assertEquals(VALID_PHOTO_URI, book.getPhoto().getPhotoFile());
    }

    // ========================================
    // EDGE CASES & SPECIAL SCENARIOS
    // ========================================

    @Test
    void testConstructor_withValidIsbn10_shouldSucceed() {
        // Arrange
        String isbn10 = "0306406152"; // Valid ISBN-10

        // Act
        Book newBook = new Book(isbn10, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, null);

        // Assert
        assertNotNull(newBook);
        assertEquals(isbn10, newBook.getIsbn().toString());
    }

    @Test
    void testConstructor_withValidIsbn13_shouldSucceed() {
        // Arrange
        String isbn13 = "9780306406157"; // Valid ISBN-13

        // Act
        Book newBook = new Book(isbn13, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, null);

        // Assert
        assertNotNull(newBook);
        assertEquals(isbn13, newBook.getIsbn().toString());
    }

    @Test
    void testApplyPatch_multipleConsecutivePatches_shouldApplyAllSuccessfully() {
        // Arrange
        long version = 0L;
        book.pk = 1L;

        // Act & Assert - First patch
        UpdateBookRequest request1 = new UpdateBookRequest();
        request1.setTitle("First Update");
        book.applyPatch(version, request1);
        assertEquals("First Update", book.getTitle().toString());

        // Second patch (version still 0 because we don't increment in domain)
        UpdateBookRequest request2 = new UpdateBookRequest();
        request2.setDescription("Second Update");
        book.applyPatch(version, request2);
        assertEquals("Second Update", book.getDescription().toString());
    }

    @Test
    void testConstructor_withComplexTitle_shouldHandleCorrectly() {
        // Arrange
        String complexTitle = "The Lord of the Rings: The Fellowship of the Ring (Book 1)";

        // Act
        Book complexBook = new Book(VALID_ISBN, complexTitle, VALID_DESCRIPTION, testGenre, testAuthors, null);

        // Assert
        assertEquals(complexTitle, complexBook.getTitle().toString());
    }

    @Test
    void testConstructor_withLongDescription_shouldHandleCorrectly() {
        // Arrange
        String longDescription = "a".repeat(4000); // Long but valid

        // Act
        Book bookWithLongDesc = new Book(VALID_ISBN, VALID_TITLE, longDescription, testGenre, testAuthors, null);

        // Assert
        assertEquals(longDescription, bookWithLongDesc.getDescription().toString());
    }

    // ========================================
    // MUTATION TESTING SUPPORT
    // ========================================

    @Test
    void testApplyPatch_withSameValuesAsCurrent_shouldStillSucceed() {
        // Arrange
        String currentTitle = book.getTitle().toString();
        String currentDescription = book.getDescription().toString();
        long currentVersion = book.getVersion();
        book.pk = 1L;

        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle(currentTitle); // Same as current
        request.setDescription(currentDescription); // Same as current

        // Act
        book.applyPatch(currentVersion, request);

        // Assert - Values should be updated even if they're the same
        assertEquals(currentTitle, book.getTitle().toString());
        assertEquals(currentDescription, book.getDescription().toString());
    }

    @Test
    void testConstructor_withEmptyStringPhoto_shouldCreatePhotoWithEmptyPath() {
        // Act
        Book bookEmptyPhoto = new Book(VALID_ISBN, VALID_TITLE, VALID_DESCRIPTION, testGenre, testAuthors, "");

        // Assert
        assertNotNull(bookEmptyPhoto.getPhoto());
        assertEquals("", bookEmptyPhoto.getPhoto().getPhotoFile());
    }

    @Test
    void testSetPhotoInternal_withNull_shouldRemovePhoto() {
        // Arrange
        book.setPhoto(VALID_PHOTO_URI);
        assertNotNull(book.getPhoto());

        // Act
        book.setPhoto(null);

        // Assert
        assertNull(book.getPhoto());
    }
}