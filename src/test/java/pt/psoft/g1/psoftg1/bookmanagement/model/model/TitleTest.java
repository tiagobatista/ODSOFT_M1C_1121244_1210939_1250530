package pt.psoft.g1.psoftg1.bookmanagement.model.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transparent-Box (White-Box) Tests for Title domain class
 *
 * Purpose: Test Title value object with KNOWLEDGE of its internal implementation
 * Testing Strategy: Branch coverage, boundary analysis, and validation logic
 * SUT: Title class (domain model)
 * Type: 2.3.2 - Functional transparent-box with SUT = domain classes
 *
 * Test Coverage:
 * - Constructor validation (null, blank, length)
 * - Setter validation paths
 * - Boundary conditions (max length)
 * - String trimming/stripping behavior
 * - toString() functionality
 * - getTitle() method
 * - Edge cases and special characters
 *
 * @author ARQSOFT 2025-2026
 */
class TitleTest {

    private static final int TITLE_MAX_LENGTH = 128;
    private static final String VALID_TITLE = "The Great Gatsby";
    private static final String VALID_TITLE_WITH_SPACES = "   Title with spaces   ";
    private static final String VALID_TITLE_COMPLEX = "The Lord of the Rings: The Fellowship of the Ring";

    // ========================================
    // CONSTRUCTOR TESTS - Validation Paths
    // ========================================

    @Test
    void testConstructor_withValidTitle_shouldCreateSuccessfully() {
        // Arrange & Act
        Title title = new Title(VALID_TITLE);

        // Assert
        assertNotNull(title);
        assertEquals(VALID_TITLE, title.getTitle());
        assertEquals(VALID_TITLE, title.toString());
    }

    @Test
    void testConstructor_withNull_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Title(null)
        );

        assertEquals("Title cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n", "  \n  "})
    void testConstructor_withBlankValues_shouldThrowException(String blankTitle) {
        // Act & Assert
        if (blankTitle == null) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Title(blankTitle)
            );
            assertEquals("Title cannot be null", exception.getMessage());
        } else {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Title(blankTitle)
            );
            assertEquals("Title cannot be blank", exception.getMessage());
        }
    }

    @Test
    void testConstructor_withExceedingMaxLength_shouldThrowException() {
        // Arrange - Create string with 129 characters (exceeds limit)
        String tooLongTitle = "a".repeat(TITLE_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Title(tooLongTitle)
        );

        assertEquals("Title has a maximum of " + TITLE_MAX_LENGTH + " characters", exception.getMessage());
    }

    // ========================================
    // BOUNDARY TESTS - Length Validation
    // ========================================

    @Test
    void testConstructor_withExactlyMaxLength_shouldSucceed() {
        // Arrange - Create string with exactly 128 characters
        String maxLengthTitle = "a".repeat(TITLE_MAX_LENGTH);

        // Act
        Title title = new Title(maxLengthTitle);

        // Assert
        assertNotNull(title);
        assertEquals(TITLE_MAX_LENGTH, title.getTitle().length());
    }

    @Test
    void testConstructor_withOneLessThanMaxLength_shouldSucceed() {
        // Arrange - Create string with 127 characters
        String nearMaxLengthTitle = "a".repeat(TITLE_MAX_LENGTH - 1);

        // Act
        Title title = new Title(nearMaxLengthTitle);

        // Assert
        assertNotNull(title);
        assertEquals(TITLE_MAX_LENGTH - 1, title.getTitle().length());
    }

    @Test
    void testConstructor_withOneCharacter_shouldSucceed() {
        // Arrange
        String minValidTitle = "a";

        // Act
        Title title = new Title(minValidTitle);

        // Assert
        assertNotNull(title);
        assertEquals(minValidTitle, title.getTitle());
    }

    // ========================================
    // STRING TRIMMING TESTS
    // ========================================

    @Test
    void testConstructor_withLeadingSpaces_shouldStrip() {
        // Arrange
        String titleWithLeadingSpaces = "   Title";

        // Act
        Title title = new Title(titleWithLeadingSpaces);

        // Assert
        assertEquals("Title", title.getTitle());
    }

    @Test
    void testConstructor_withTrailingSpaces_shouldStrip() {
        // Arrange
        String titleWithTrailingSpaces = "Title   ";

        // Act
        Title title = new Title(titleWithTrailingSpaces);

        // Assert
        assertEquals("Title", title.getTitle());
    }

    @Test
    void testConstructor_withLeadingAndTrailingSpaces_shouldStrip() {
        // Arrange
        String titleWithSpaces = "   Title with spaces   ";

        // Act
        Title title = new Title(titleWithSpaces);

        // Assert
        assertEquals("Title with spaces", title.getTitle());
    }

    @Test
    void testConstructor_withInternalSpaces_shouldPreserve() {
        // Arrange
        String titleWithInternalSpaces = "Title  with  multiple  spaces";

        // Act
        Title title = new Title(titleWithInternalSpaces);

        // Assert
        assertEquals("Title  with  multiple  spaces", title.getTitle());
    }

    @Test
    void testConstructor_withLeadingTabs_shouldStrip() {
        // Arrange
        String titleWithTab = "\t\tTitle";

        // Act
        Title title = new Title(titleWithTab);

        // Assert
        assertEquals("Title", title.getTitle());
    }

    @Test
    void testConstructor_withLeadingNewlines_shouldStrip() {
        // Arrange
        String titleWithNewline = "\n\nTitle";

        // Act
        Title title = new Title(titleWithNewline);

        // Assert
        assertEquals("Title", title.getTitle());
    }

    // ========================================
    // SETTER TESTS - Validation Paths
    // ========================================

    @Test
    void testSetTitle_withValidValue_shouldUpdateTitle() {
        // Arrange
        Title title = new Title(VALID_TITLE);
        String newTitle = "Updated Title";

        // Act
        title.setTitle(newTitle);

        // Assert
        assertEquals(newTitle, title.getTitle());
    }

    @Test
    void testSetTitle_withNull_shouldThrowException() {
        // Arrange
        Title title = new Title(VALID_TITLE);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> title.setTitle(null)
        );

        assertEquals("Title cannot be null", exception.getMessage());
        // Original value should remain unchanged
        assertEquals(VALID_TITLE, title.getTitle());
    }

    @Test
    void testSetTitle_withBlank_shouldThrowException() {
        // Arrange
        Title title = new Title(VALID_TITLE);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> title.setTitle("   ")
        );

        assertEquals("Title cannot be blank", exception.getMessage());
        // Original value should remain unchanged
        assertEquals(VALID_TITLE, title.getTitle());
    }

    @Test
    void testSetTitle_withTooLong_shouldThrowException() {
        // Arrange
        Title title = new Title(VALID_TITLE);
        String tooLongTitle = "a".repeat(TITLE_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> title.setTitle(tooLongTitle)
        );

        assertEquals("Title has a maximum of " + TITLE_MAX_LENGTH + " characters", exception.getMessage());
        // Original value should remain unchanged
        assertEquals(VALID_TITLE, title.getTitle());
    }

    @Test
    void testSetTitle_withLeadingAndTrailingSpaces_shouldStrip() {
        // Arrange
        Title title = new Title(VALID_TITLE);
        String newTitle = "   New Title   ";

        // Act
        title.setTitle(newTitle);

        // Assert
        assertEquals("New Title", title.getTitle());
    }

    // ========================================
    // BUSINESS LOGIC TESTS
    // ========================================

    @Test
    void testToString_shouldReturnTitleValue() {
        // Arrange
        Title title = new Title(VALID_TITLE);

        // Act
        String result = title.toString();

        // Assert
        assertEquals(VALID_TITLE, result);
        assertSame(title.getTitle(), result);
    }

    @Test
    void testGetTitle_shouldReturnStoredValue() {
        // Arrange
        String expectedTitle = "Expected Title";
        Title title = new Title(expectedTitle);

        // Act
        String actualTitle = title.getTitle();

        // Assert
        assertEquals(expectedTitle, actualTitle);
    }

    // ========================================
    // EDGE CASES & SPECIAL CHARACTERS
    // ========================================

    @Test
    void testConstructor_withSpecialCharacters_shouldSucceed() {
        // Arrange
        String titleWithSpecialChars = "Title with special chars: áéíóú ñ ç €$£¥";

        // Act
        Title title = new Title(titleWithSpecialChars);

        // Assert
        assertNotNull(title);
        assertEquals(titleWithSpecialChars, title.getTitle());
    }

    @Test
    void testConstructor_withNumbers_shouldSucceed() {
        // Arrange
        String titleWithNumbers = "Book 1984 Edition 2";

        // Act
        Title title = new Title(titleWithNumbers);

        // Assert
        assertNotNull(title);
        assertEquals(titleWithNumbers, title.getTitle());
    }

    @Test
    void testConstructor_withPunctuation_shouldSucceed() {
        // Arrange
        String titleWithPunctuation = "Title: A Story! (Part 1)";

        // Act
        Title title = new Title(titleWithPunctuation);

        // Assert
        assertNotNull(title);
        assertEquals(titleWithPunctuation, title.getTitle());
    }

    @Test
    void testConstructor_withEmojis_shouldSucceed() {
        // Arrange
        String titleWithEmojis = "Title with emojis 📚 📖 ✨";

        // Act
        Title title = new Title(titleWithEmojis);

        // Assert
        assertNotNull(title);
        assertEquals(titleWithEmojis, title.getTitle());
    }

    @Test
    void testConstructor_withUnicodeCharacters_shouldSucceed() {
        // Arrange
        String titleWithUnicode = "Title with 中文 日本語 한글";

        // Act
        Title title = new Title(titleWithUnicode);

        // Assert
        assertNotNull(title);
        assertEquals(titleWithUnicode, title.getTitle());
    }

    @Test
    void testConstructor_withComplexTitle_shouldSucceed() {
        // Arrange
        String complexTitle = "The Lord of the Rings: The Fellowship of the Ring (Book 1)";

        // Act
        Title title = new Title(complexTitle);

        // Assert
        assertNotNull(title);
        assertEquals(complexTitle, title.getTitle());
    }

    // ========================================
    // MUTATION TESTING SUPPORT
    // ========================================

    @Test
    void testSetTitle_multipleConsecutiveCalls_shouldUpdateEachTime() {
        // Arrange
        Title title = new Title("Initial title");

        // Act & Assert
        title.setTitle("First update");
        assertEquals("First update", title.getTitle());

        title.setTitle("Second update");
        assertEquals("Second update", title.getTitle());

        title.setTitle("Third update");
        assertEquals("Third update", title.getTitle());
    }

    @Test
    void testConstructor_withBoundaryContent_shouldHandleCorrectly() {
        // Arrange - Test at exact boundary
        String boundaryTitle = "a".repeat(TITLE_MAX_LENGTH);

        // Act
        Title title = new Title(boundaryTitle);

        // Assert
        assertNotNull(title);
        assertEquals(TITLE_MAX_LENGTH, title.getTitle().length());

        // Verify setTitle also respects boundary
        title.setTitle("New title");
        assertEquals("New title", title.getTitle());
    }

    @Test
    void testSetTitle_replacingLongWithShort_shouldSucceed() {
        // Arrange
        Title title = new Title("a".repeat(100));

        // Act
        title.setTitle("Short title");

        // Assert
        assertEquals("Short title", title.getTitle());
        assertEquals(11, title.getTitle().length());
    }

    @Test
    void testSetTitle_replacingShortWithLong_shouldSucceed() {
        // Arrange
        Title title = new Title("Short");
        String longTitle = "a".repeat(100);

        // Act
        title.setTitle(longTitle);

        // Assert
        assertEquals(longTitle, title.getTitle());
        assertEquals(100, title.getTitle().length());
    }

    @Test
    void testConstructor_strippingDoesNotCauseBlank_shouldSucceed() {
        // Arrange
        String titleWithSpaces = "   Valid Content   ";

        // Act
        Title title = new Title(titleWithSpaces);

        // Assert
        assertEquals("Valid Content", title.getTitle());
        assertFalse(title.getTitle().isBlank());
    }

    @Test
    void testConstructor_afterStripping_ifBlank_shouldThrowException() {
        // Arrange
        String titleOnlySpaces = "     ";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Title(titleOnlySpaces)
        );

        assertEquals("Title cannot be blank", exception.getMessage());
    }

    @Test
    void testSetTitle_afterStripping_lengthValidation_shouldApply() {
        // Arrange
        Title title = new Title("Valid");
        // Create title that after stripping will still exceed max length
        String tooLongAfterStrip = "   " + "a".repeat(TITLE_MAX_LENGTH + 1) + "   ";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> title.setTitle(tooLongAfterStrip));
    }



    @Test
    void testDefaultConstructor_shouldCreateTitle() {
        // Act
        Title title = new Title();

        // Assert
        assertNotNull(title);
        // Title field should be uninitialized (null by default)
    }

    @Test
    void testConstructor_withMixedWhitespace_shouldStripAll() {
        // Arrange
        String titleWithMixedWhitespace = " \t \n Title \r\n \t ";

        // Act
        Title title = new Title(titleWithMixedWhitespace);

        // Assert
        assertEquals("Title", title.getTitle());
    }
}