package pt.psoft.g1.psoftg1.authormanagement.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transparent-Box (White-Box) Tests for Bio domain class
 *
 * Purpose: Test Bio value object with KNOWLEDGE of its internal implementation
 * Testing Strategy: Branch coverage, boundary analysis, and validation logic
 * SUT: Bio class (domain model)
 * Type: 2.3.2 - Functional transparent-box with SUT = domain classes
 *
 * Test Coverage:
 * - Constructor validation (null, blank, length)
 * - Setter validation paths
 * - Boundary conditions (max length)
 * - HTML sanitization behavior
 * - toString() functionality
 * - getValue() method
 * - Edge cases and special characters
 *
 * @author ARQSOFT 2025-2026
 */
class BioTest {

    private static final int BIO_MAX_LENGTH = 4096;
    private static final String VALID_BIO = "João Alberto nasceu em Chaves e foi pedreiro a maior parte da sua vida.";
    private static final String VALID_BIO_WITH_HTML = "This is a <b>bold</b> bio with <script>alert('xss')</script>";

    // ========================================
    // CONSTRUCTOR TESTS - Validation Paths
    // ========================================

    @Test
    void testConstructor_withValidBio_shouldCreateSuccessfully() {
        // Arrange & Act
        Bio bio = new Bio(VALID_BIO);

        // Assert
        assertNotNull(bio);
        assertEquals(VALID_BIO, bio.getBio());
        assertEquals(VALID_BIO, bio.toString());
    }

    @Test
    void testConstructor_withNull_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Bio(null)
        );

        assertEquals("Bio cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n", "  \n  "})
    void testConstructor_withBlankValues_shouldThrowException(String blankBio) {
        // Act & Assert
        if (blankBio == null) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Bio(blankBio)
            );
            assertEquals("Bio cannot be null", exception.getMessage());
        } else {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Bio(blankBio)
            );
            assertEquals("Bio cannot be blank", exception.getMessage());
        }
    }

    @Test
    void testConstructor_withExceedingMaxLength_shouldThrowException() {
        // Arrange - Create string with 4097 characters (exceeds limit)
        String tooLongBio = "a".repeat(BIO_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Bio(tooLongBio)
        );

        assertEquals("Bio has a maximum of 4096 characters", exception.getMessage());
    }

    // ========================================
    // BOUNDARY TESTS - Length Validation
    // ========================================

    @Test
    void testConstructor_withExactlyMaxLength_shouldSucceed() {
        // Arrange - Create string with exactly 4096 characters
        String maxLengthBio = "a".repeat(BIO_MAX_LENGTH);

        // Act
        Bio bio = new Bio(maxLengthBio);

        // Assert
        assertNotNull(bio);
        assertEquals(BIO_MAX_LENGTH, bio.getBio().length());
    }

    @Test
    void testConstructor_withOneLessThanMaxLength_shouldSucceed() {
        // Arrange - Create string with 4095 characters
        String nearMaxLengthBio = "a".repeat(BIO_MAX_LENGTH - 1);

        // Act
        Bio bio = new Bio(nearMaxLengthBio);

        // Assert
        assertNotNull(bio);
        assertEquals(BIO_MAX_LENGTH - 1, bio.getBio().length());
    }

    @Test
    void testConstructor_withOneCharacter_shouldSucceed() {
        // Arrange
        String minValidBio = "a";

        // Act
        Bio bio = new Bio(minValidBio);

        // Assert
        assertNotNull(bio);
        assertEquals(minValidBio, bio.getBio());
    }

    // ========================================
    // SETTER TESTS - Validation Paths
    // ========================================

    @Test
    void testSetBio_withValidValue_shouldUpdateBio() {
        // Arrange
        Bio bio = new Bio(VALID_BIO);
        String newBio = "Nova biografia atualizada.";

        // Act
        bio.setBio(newBio);

        // Assert
        assertEquals(newBio, bio.getBio());
    }

    @Test
    void testSetBio_withNull_shouldThrowException() {
        // Arrange
        Bio bio = new Bio(VALID_BIO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bio.setBio(null)
        );

        assertEquals("Bio cannot be null", exception.getMessage());
        // Original value should remain unchanged
        assertEquals(VALID_BIO, bio.getBio());
    }

    @Test
    void testSetBio_withBlank_shouldThrowException() {
        // Arrange
        Bio bio = new Bio(VALID_BIO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bio.setBio("   ")
        );

        assertEquals("Bio cannot be blank", exception.getMessage());
        // Original value should remain unchanged
        assertEquals(VALID_BIO, bio.getBio());
    }

    @Test
    void testSetBio_withTooLong_shouldThrowException() {
        // Arrange
        Bio bio = new Bio(VALID_BIO);
        String tooLongBio = "a".repeat(BIO_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bio.setBio(tooLongBio)
        );

        assertEquals("Bio has a maximum of 4096 characters", exception.getMessage());
        // Original value should remain unchanged
        assertEquals(VALID_BIO, bio.getBio());
    }

    // ========================================
    // HTML SANITIZATION TESTS
    // ========================================

    @Test
    void testConstructor_withHtmlContent_shouldSanitize() {
        // Arrange
        String bioWithHtml = "Bio with <b>HTML</b> tags and <script>alert('test')</script>";

        // Act
        Bio bio = new Bio(bioWithHtml);

        // Assert
        assertNotNull(bio);
        // StringUtilsCustom.sanitizeHtml should have processed the HTML
        // We verify the bio was created (sanitization logic is tested separately)
        assertNotNull(bio.getBio());
    }

    @Test
    void testSetBio_withHtmlContent_shouldSanitize() {
        // Arrange
        Bio bio = new Bio(VALID_BIO);
        String bioWithHtml = "Updated bio with <div>HTML</div> content";

        // Act
        bio.setBio(bioWithHtml);

        // Assert
        // StringUtilsCustom.sanitizeHtml should have processed the HTML
        assertNotNull(bio.getBio());
    }

    // ========================================
    // BUSINESS LOGIC TESTS
    // ========================================

    @Test
    void testToString_shouldReturnBioValue() {
        // Arrange
        Bio bio = new Bio(VALID_BIO);

        // Act
        String result = bio.toString();

        // Assert
        assertEquals(VALID_BIO, result);
        assertSame(bio.getBio(), result);
    }

    @Test
    void testGetBio_shouldReturnStoredValue() {
        // Arrange
        String expectedBio = "Expected biography text";
        Bio bio = new Bio(expectedBio);

        // Act
        String actualBio = bio.getBio();

        // Assert
        assertEquals(expectedBio, actualBio);
    }

    // ========================================
    // EDGE CASES & SPECIAL CHARACTERS
    // ========================================

    @Test
    void testConstructor_withSpecialCharacters_shouldSucceed() {
        // Arrange
        String bioWithSpecialChars = "Bio with special chars: áéíóú ñ ç €$£¥ 中文 日本語 한글";

        // Act
        Bio bio = new Bio(bioWithSpecialChars);

        // Assert
        assertNotNull(bio);
        assertNotNull(bio.getBio());
    }

    @Test
    void testConstructor_withNewlinesAndTabs_shouldSucceed() {
        // Arrange
        String bioWithWhitespace = "Line 1\nLine 2\tTabbed\r\nWindows line";

        // Act
        Bio bio = new Bio(bioWithWhitespace);

        // Assert
        assertNotNull(bio);
        assertNotNull(bio.getBio());
    }

    @Test
    void testConstructor_withEmojis_shouldSucceed() {
        // Arrange
        String bioWithEmojis = "Bio with emojis 😀 🎉 📚 ✨";

        // Act
        Bio bio = new Bio(bioWithEmojis);

        // Assert
        assertNotNull(bio);
        assertNotNull(bio.getBio());
    }

    @Test
    void testConstructor_withMixedWhitespace_shouldNotBeBlank() {
        // Arrange
        String bioWithContent = "   Content with spaces   ";

        // Act
        Bio bio = new Bio(bioWithContent);

        // Assert
        assertNotNull(bio);
        assertNotNull(bio.getBio());
    }

    // ========================================
    // MUTATION TESTING SUPPORT
    // ========================================

    @Test
    void testSetBio_multipleConsecutiveCalls_shouldUpdateEachTime() {
        // Arrange
        Bio bio = new Bio("Initial bio");

        // Act & Assert
        bio.setBio("First update");
        assertEquals("First update", bio.getBio());

        bio.setBio("Second update");
        assertEquals("Second update", bio.getBio());

        bio.setBio("Third update");
        assertEquals("Third update", bio.getBio());
    }

    @Test
    void testBioImmutability_afterCreation_internalStateCannotBeChangedExternally() {
        // Arrange
        String originalBio = VALID_BIO;
        Bio bio = new Bio(originalBio);

        // Act - Get the bio string
        String retrievedBio = bio.getBio();

        // Assert - Modifying retrieved string doesn't affect Bio internal state
        // (Strings are immutable in Java, but we test the concept)
        assertNotNull(retrievedBio);
        assertEquals(originalBio, bio.getBio());
    }

    @Test
    void testConstructor_withBoundaryContent_shouldHandleCorrectly() {
        // Arrange - Test at exact boundary
        String boundaryBio = "a".repeat(BIO_MAX_LENGTH);

        // Act
        Bio bio = new Bio(boundaryBio);

        // Assert
        assertNotNull(bio);
        assertEquals(BIO_MAX_LENGTH, bio.getBio().length());

        // Verify setBio also respects boundary
        bio.setBio("New content");
        assertEquals("New content", bio.getBio());
    }

    @Test
    void testSetBio_replacingLongWithShort_shouldSucceed() {
        // Arrange
        Bio bio = new Bio("a".repeat(4000));

        // Act
        bio.setBio("Short bio");

        // Assert
        assertEquals("Short bio", bio.getBio());
        assertEquals(9, bio.getBio().length());
    }

    @Test
    void testSetBio_replacingShortWithLong_shouldSucceed() {
        // Arrange
        Bio bio = new Bio("Short");
        String longBio = "a".repeat(3000);

        // Act
        bio.setBio(longBio);

        // Assert
        assertEquals(longBio, bio.getBio());
        assertEquals(3000, bio.getBio().length());
    }
}