package pt.psoft.g1.psoftg1.authormanagement.model;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.Photo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transparent-Box (White-Box) Tests for Author domain class
 *
 * Purpose: Test Author entity with KNOWLEDGE of its internal implementation
 * Testing Strategy: Complete branch coverage, all conditional paths, boundary analysis
 * SUT: Author class (domain model)
 * Type: 2.3.2 - Functional transparent-box with SUT = domain classes
 *
 * Test Coverage:
 * - All constructors (3 variants + default)
 * - All validation paths (null checks, business rules)
 * - Version control and concurrency (applyPatch, removePhoto)
 * - All setter methods with validation
 * - Photo management (optional field)
 * - Edge cases and error conditions
 * - Business logic branches
 *
 * @author ARQSOFT 2025-2026
 */
class AuthorTest {

    private static final String VALID_NAME = "João Alberto";
    private static final String VALID_BIO = "O João Alberto nasceu em Chaves e foi pedreiro a maior parte da sua vida.";
    private static final String VALID_PHOTO_URI = "photo.jpg";

    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author(VALID_NAME, VALID_BIO, null);
    }

    // ========================================
    // CONSTRUCTOR TESTS - All Variants
    // ========================================

    @Test
    void testConstructor_withStrings_shouldCreateAuthorWithVersion0() {
        // Act
        Author newAuthor = new Author(VALID_NAME, VALID_BIO, VALID_PHOTO_URI);

        // Assert
        assertNotNull(newAuthor);
        assertEquals(VALID_NAME, newAuthor.getName().toString());
        assertEquals(VALID_BIO, newAuthor.getBio().toString());
        assertEquals(VALID_PHOTO_URI, newAuthor.getPhoto().getPhotoFile());
        assertEquals(0L, newAuthor.getVersion());
        assertNull(newAuthor.getAuthorNumber()); // Not set yet
    }

    @Test
    void testConstructor_withStringsAndNullPhoto_shouldCreateAuthorWithoutPhoto() {
        // Act
        Author newAuthor = new Author(VALID_NAME, VALID_BIO, null);

        // Assert
        assertNotNull(newAuthor);
        assertEquals(VALID_NAME, newAuthor.getName().toString());
        assertEquals(VALID_BIO, newAuthor.getBio().toString());
        assertNull(newAuthor.getPhoto());
        assertEquals(0L, newAuthor.getVersion());
    }

    @Test
    void testConstructor_withValueObjects_shouldCreateAuthor() {
        // Arrange
        Name name = new Name(VALID_NAME);
        Bio bio = new Bio(VALID_BIO);

        // Act
        Author newAuthor = new Author(name, bio, VALID_PHOTO_URI);

        // Assert
        assertNotNull(newAuthor);
        assertSame(name, newAuthor.getName());
        assertSame(bio, newAuthor.getBio());
        assertEquals(VALID_PHOTO_URI, newAuthor.getPhoto().getPhotoFile());
        assertEquals(0L, newAuthor.getVersion());
    }

    @Test
    void testDefaultConstructor_shouldCreateEmptyAuthor() {
        // Act
        Author emptyAuthor = new Author();

        // Assert
        assertNotNull(emptyAuthor);
        assertNull(emptyAuthor.getName());
        assertNull(emptyAuthor.getBio());
        assertNull(emptyAuthor.getPhoto());
        assertEquals(0L, emptyAuthor.getVersion());
    }

    // ========================================
    // VALIDATION TESTS - Name
    // ========================================

    @Test
    void testConstructor_withNullName_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Author((String) null, VALID_BIO, null)
        );

        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void testConstructor_withNullNameObject_shouldThrowException() {
        // Arrange
        Bio bio = new Bio(VALID_BIO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Author((Name) null, bio, null)
        );

        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void testSetName_withNullString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> author.setName((String) null)
        );

        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void testSetName_withNullNameObject_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> author.setName((Name) null)
        );

        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void testSetName_withValidString_shouldUpdateName() {
        // Arrange
        String newName = "Maria Silva";

        // Act
        author.setName(newName);

        // Assert
        assertEquals(newName, author.getName().toString());
    }

    @Test
    void testSetName_withValidNameObject_shouldUpdateName() {
        // Arrange
        Name newName = new Name("Pedro Santos");

        // Act
        author.setName(newName);

        // Assert
        assertSame(newName, author.getName());
    }

    // ========================================
    // VALIDATION TESTS - Bio
    // ========================================

    @Test
    void testConstructor_withNullBio_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Author(VALID_NAME, (String) null, null)
        );

        assertEquals("Bio cannot be null", exception.getMessage());
    }

    @Test
    void testConstructor_withNullBioObject_shouldThrowException() {
        // Arrange
        Name name = new Name(VALID_NAME);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Author(name, (Bio) null, null)
        );

        assertEquals("Bio cannot be null", exception.getMessage());
    }

    @Test
    void testSetBio_withNullString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> author.setBio((String) null)
        );

        assertEquals("Bio cannot be null", exception.getMessage());
    }

    @Test
    void testSetBio_withNullBioObject_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> author.setBio((Bio) null)
        );

        assertEquals("Bio cannot be null", exception.getMessage());
    }

    @Test
    void testSetBio_withValidString_shouldUpdateBio() {
        // Arrange
        String newBio = "Nova biografia completa.";

        // Act
        author.setBio(newBio);

        // Assert
        assertEquals(newBio, author.getBio().toString());
    }

    @Test
    void testSetBio_withValidBioObject_shouldUpdateBio() {
        // Arrange
        Bio newBio = new Bio("Biografia atualizada.");

        // Act
        author.setBio(newBio);

        // Assert
        assertSame(newBio, author.getBio());
    }

    // ========================================
    // GETTER/SETTER TESTS - AuthorNumber & Version
    // ========================================

    @Test
    void testSetAuthorNumber_shouldUpdateAuthorNumber() {
        // Arrange
        Long authorNumber = 123L;

        // Act
        author.setAuthorNumber(authorNumber);

        // Assert
        assertEquals(authorNumber, author.getAuthorNumber());
        assertEquals(authorNumber, author.getId()); // getId() returns authorNumber
    }

    @Test
    void testSetVersion_shouldUpdateVersion() {
        // Arrange
        long newVersion = 5L;

        // Act
        author.setVersion(newVersion);

        // Assert
        assertEquals(newVersion, author.getVersion());
    }

    @Test
    void testGetId_shouldReturnAuthorNumber() {
        // Arrange
        Long authorNumber = 456L;
        author.setAuthorNumber(authorNumber);

        // Act
        Long id = author.getId();

        // Assert
        assertEquals(authorNumber, id);
        assertSame(author.getAuthorNumber(), id);
    }

    // ========================================
    // PHOTO MANAGEMENT TESTS
    // ========================================

    @Test
    void testConstructor_withPhotoUri_shouldCreateAuthorWithPhoto() {
        // Act
        Author authorWithPhoto = new Author(VALID_NAME, VALID_BIO, VALID_PHOTO_URI);

        // Assert
        assertNotNull(authorWithPhoto.getPhoto());
        assertEquals(VALID_PHOTO_URI, authorWithPhoto.getPhoto().getPhotoFile());
    }

    @Test
    void testConstructor_withNullPhoto_shouldCreateAuthorWithoutPhoto() {
        // Act
        Author authorNoPhoto = new Author(VALID_NAME, VALID_BIO, null);

        // Assert
        assertNull(authorNoPhoto.getPhoto());
    }

    @Test
    void testSetPhoto_withValidUri_shouldUpdatePhoto() {
        // Arrange
        String newPhotoUri = "new-photo.png";

        // Act
        author.setPhoto(newPhotoUri);

        // Assert
        assertNotNull(author.getPhoto());
        assertEquals(newPhotoUri, author.getPhoto().getPhotoFile());
    }

    @Test
    void testSetPhoto_withNull_shouldRemovePhoto() {
        // Arrange
        author.setPhoto(VALID_PHOTO_URI);
        assertNotNull(author.getPhoto());

        // Act
        author.setPhoto(null);

        // Assert
        assertNull(author.getPhoto());
    }

    // ========================================
    // APPLY PATCH TESTS - All Branches
    // ========================================

    @Test
    void testApplyPatch_withCorrectVersion_shouldUpdateAllFields() {
        // Arrange
        author.setAuthorNumber(1L);
        long currentVersion = author.getVersion();

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName("Updated Name");
        request.setBio("Updated Bio");
        request.setPhotoURI("updated-photo.jpg");

        // Act
        author.applyPatch(currentVersion, request);

        // Assert
        assertEquals("Updated Name", author.getName().toString());
        assertEquals("Updated Bio", author.getBio().toString());
        assertEquals("updated-photo.jpg", author.getPhoto().getPhotoFile());
    }

    @Test
    void testApplyPatch_withIncorrectVersion_shouldThrowStaleObjectException() {
        // Arrange
        author.setAuthorNumber(1L);
        author.setVersion(5L);

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName("Updated Name");

        // Act & Assert
        assertThrows(StaleObjectStateException.class,
                () -> author.applyPatch(999L, request)
        );
    }

    @Test
    void testApplyPatch_withOnlyName_shouldUpdateOnlyName() {
        // Arrange
        String originalBio = author.getBio().toString();
        long currentVersion = author.getVersion();

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName("Only Name Updated");
        // bio and photo are null

        // Act
        author.applyPatch(currentVersion, request);

        // Assert
        assertEquals("Only Name Updated", author.getName().toString());
        assertEquals(originalBio, author.getBio().toString()); // Unchanged
        assertNull(author.getPhoto()); // Unchanged
    }

    @Test
    void testApplyPatch_withOnlyBio_shouldUpdateOnlyBio() {
        // Arrange
        String originalName = author.getName().toString();
        long currentVersion = author.getVersion();

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setBio("Only Bio Updated");
        // name and photo are null

        // Act
        author.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalName, author.getName().toString()); // Unchanged
        assertEquals("Only Bio Updated", author.getBio().toString());
        assertNull(author.getPhoto()); // Unchanged
    }

    @Test
    void testApplyPatch_withOnlyPhoto_shouldUpdateOnlyPhoto() {
        // Arrange
        String originalName = author.getName().toString();
        String originalBio = author.getBio().toString();
        long currentVersion = author.getVersion();

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setPhotoURI("only-photo-updated.jpg");
        // name and bio are null

        // Act
        author.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalName, author.getName().toString()); // Unchanged
        assertEquals(originalBio, author.getBio().toString()); // Unchanged
        assertEquals("only-photo-updated.jpg", author.getPhoto().getPhotoFile());
    }

    @Test
    void testApplyPatch_withAllNullFields_shouldNotUpdateAnything() {
        // Arrange
        String originalName = author.getName().toString();
        String originalBio = author.getBio().toString();
        long currentVersion = author.getVersion();

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        // All fields null

        // Act
        author.applyPatch(currentVersion, request);

        // Assert
        assertEquals(originalName, author.getName().toString());
        assertEquals(originalBio, author.getBio().toString());
        assertNull(author.getPhoto());
    }

    @Test
    void testApplyPatch_withPhotoUpdate_shouldReplacePhoto() {
        // Arrange
        author.setPhoto(VALID_PHOTO_URI);
        assertNotNull(author.getPhoto());
        long currentVersion = author.getVersion();

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setPhotoURI("new-photo.jpg");

        // Act
        author.applyPatch(currentVersion, request);

        // Assert
        assertEquals("new-photo.jpg", author.getPhoto().getPhotoFile());
    }

    // ========================================
    // REMOVE PHOTO TESTS - Version Control
    // ========================================

    @Test
    void testRemovePhoto_withCorrectVersion_shouldRemovePhoto() {
        // Arrange
        author.setPhoto(VALID_PHOTO_URI);
        assertNotNull(author.getPhoto());
        long currentVersion = author.getVersion();

        // Act
        author.removePhoto(currentVersion);

        // Assert
        assertNull(author.getPhoto());
    }

    @Test
    void testRemovePhoto_withIncorrectVersion_shouldThrowConflictException() {
        // Arrange
        author.setPhoto(VALID_PHOTO_URI);
        author.setVersion(5L);

        // Act & Assert
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> author.removePhoto(999L)
        );

        assertTrue(exception.getMessage().contains("version does not match"));
        // Photo should remain unchanged
        assertNotNull(author.getPhoto());
    }

    @Test
    void testRemovePhoto_whenNoPhoto_shouldStillSucceedWithCorrectVersion() {
        // Arrange
        assertNull(author.getPhoto());
        long currentVersion = author.getVersion();

        // Act
        author.removePhoto(currentVersion);

        // Assert
        assertNull(author.getPhoto()); // Still null
    }

    // ========================================
    // CONCURRENCY TESTS - Version Matching
    // ========================================

    @Test
    void testApplyPatch_versionMismatch_shouldNotModifyAuthor() {
        // Arrange
        String originalName = author.getName().toString();
        String originalBio = author.getBio().toString();
        author.setVersion(10L);

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName("Should Not Apply");
        request.setBio("Should Not Apply");

        // Act & Assert
        assertThrows(StaleObjectStateException.class,
                () -> author.applyPatch(5L, request)
        );

        // Verify nothing changed
        assertEquals(originalName, author.getName().toString());
        assertEquals(originalBio, author.getBio().toString());
    }

    @Test
    void testRemovePhoto_versionMismatch_shouldNotRemovePhoto() {
        // Arrange
        author.setPhoto(VALID_PHOTO_URI);
        author.setVersion(10L);

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> author.removePhoto(5L)
        );

        // Verify photo still exists
        assertNotNull(author.getPhoto());
        assertEquals(VALID_PHOTO_URI, author.getPhoto().getPhotoFile());
    }

    // ========================================
    // EDGE CASES & SPECIAL SCENARIOS
    // ========================================

    @Test
    void testApplyPatch_multipleConsecutivePatches_shouldApplyAllSuccessfully() {
        // Arrange
        long version = 0L;

        // Act & Assert - First patch
        UpdateAuthorRequest request1 = new UpdateAuthorRequest();
        request1.setName("First Update");
        author.applyPatch(version, request1);
        assertEquals("First Update", author.getName().toString());

        // Second patch (version still 0 because we don't increment in domain)
        UpdateAuthorRequest request2 = new UpdateAuthorRequest();
        request2.setBio("Second Update");
        author.applyPatch(version, request2);
        assertEquals("Second Update", author.getBio().toString());
    }

    @Test
    void testConstructor_withComplexName_shouldHandleCorrectly() {
        // Arrange
        String complexName = "José María García-Hernández Pérez";

        // Act
        Author complexAuthor = new Author(complexName, VALID_BIO, null);

        // Assert
        assertEquals(complexName, complexAuthor.getName().toString());
    }

    @Test
    void testConstructor_withLongBio_shouldHandleCorrectly() {
        // Arrange
        String longBio = "A".repeat(4000); // Long but valid

        // Act
        Author authorWithLongBio = new Author(VALID_NAME, longBio, null);

        // Assert
        assertEquals(longBio, authorWithLongBio.getBio().toString());
    }

    // ========================================
    // MUTATION TESTING SUPPORT
    // ========================================

    @Test
    void testVersionInitialization_shouldAlwaysBeZero() {
        // Arrange & Act
        Author newAuthor1 = new Author(VALID_NAME, VALID_BIO, null);
        Author newAuthor2 = new Author(VALID_NAME, VALID_BIO, VALID_PHOTO_URI);
        Author newAuthor3 = new Author();

        // Assert
        assertEquals(0L, newAuthor1.getVersion());
        assertEquals(0L, newAuthor2.getVersion());
        assertEquals(0L, newAuthor3.getVersion());
    }

    @Test
    void testSetters_chainedCalls_shouldAllSucceed() {
        // Arrange
        Author newAuthor = new Author();

        // Act - Chain multiple setters
        newAuthor.setName("Test Name");
        newAuthor.setBio("Test Bio");
        newAuthor.setAuthorNumber(999L);
        newAuthor.setVersion(5L);
        newAuthor.setPhoto("test-photo.jpg");

        // Assert
        assertEquals("Test Name", newAuthor.getName().toString());
        assertEquals("Test Bio", newAuthor.getBio().toString());
        assertEquals(999L, newAuthor.getAuthorNumber());
        assertEquals(5L, newAuthor.getVersion());
        assertEquals("test-photo.jpg", newAuthor.getPhoto().getPhotoFile());
    }

    @Test
    void testApplyPatch_withSameValuesAsCurrent_shouldStillSucceed() {
        // Arrange
        String currentName = author.getName().toString();
        String currentBio = author.getBio().toString();
        long currentVersion = author.getVersion();

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName(currentName); // Same as current
        request.setBio(currentBio);   // Same as current

        // Act
        author.applyPatch(currentVersion, request);

        // Assert - Values should be updated even if they're the same
        assertEquals(currentName, author.getName().toString());
        assertEquals(currentBio, author.getBio().toString());
    }

    @Test
    void testConstructor_withEmptyStringPhoto_shouldCreatePhotoWithEmptyPath() {
        // Act
        Author authorEmptyPhoto = new Author(VALID_NAME, VALID_BIO, "");

        // Assert
        assertNotNull(authorEmptyPhoto.getPhoto());
        assertEquals("", authorEmptyPhoto.getPhoto().getPhotoFile());
    }
}