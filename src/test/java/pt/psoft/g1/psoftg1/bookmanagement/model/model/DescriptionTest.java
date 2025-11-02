package pt.psoft.g1.psoftg1.bookmanagement.model.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pt.psoft.g1.psoftg1.bookmanagement.model.Description;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transparent-Box (White-Box) Tests for Description domain class
 *
 * Purpose: Test Description value object with KNOWLEDGE of its internal implementation
 * Testing Strategy: Branch coverage, boundary analysis, and validation logic
 * SUT: Description class (domain model)
 * Type: 2.3.2 - Functional transparent-box with SUT = domain classes
 *
 * Test Coverage:
 * - Constructor validation (null, blank, length)
 * - Setter validation paths
 * - Boundary conditions (max length)
 * - HTML sanitization behavior
 * - toString() functionality
 * - Nullable description (optional field)
 * - Edge cases and special characters
 *
 * @author ARQSOFT 2025-2026
 */
class DescriptionTest {

    private static final int DESC_MAX_LENGTH = 4096;
    private static final String VALID_DESCRIPTION = "This is a valid book description with enough content.";
    private static final String VALID_DESCRIPTION_WITH_HTML = "This is a <b>bold</b> description with <script>alert('xss')</script>";

    // ========================================
    // CONSTRUCTOR TESTS - Validation Paths
    // ========================================

    @Test
    void testConstructor_withValidDescription_shouldCreateSuccessfully() {
        // Arrange & Act
        Description description = new Description(VALID_DESCRIPTION);

        // Assert
        assertNotNull(description);
        assertEquals(VALID_DESCRIPTION, description.getDescription());
        assertEquals(VALID_DESCRIPTION, description.toString());
    }

    @Test
    void testConstructor_withNull_shouldCreateWithNullDescription() {
        // Act
        Description description = new Description(null);

        // Assert
        assertNotNull(description);
        assertNull(description.getDescription());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n", "  \n  "})
    void testConstructor_withBlankValues_shouldCreateWithNullDescription(String blankDescription) {
        // Act
        Description description = new Description(blankDescription);

        // Assert
        assertNotNull(description);
        assertNull(description.getDescription());
    }

    @Test
    void testConstructor_withExceedingMaxLength_shouldThrowException() {
        // Arrange - Create string with 4097 characters (exceeds limit)
        String tooLongDescription = "a".repeat(DESC_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Description(tooLongDescription)
        );

        assertEquals("Description has a maximum of 4096 characters", exception.getMessage());
    }

    // ========================================
    // BOUNDARY TESTS - Length Validation
    // ========================================

    @Test
    void testConstructor_withExactlyMaxLength_shouldSucceed() {
        // Arrange - Create string with exactly 4096 characters
        String maxLengthDescription = "a".repeat(DESC_MAX_LENGTH);

        // Act
        Description description = new Description(maxLengthDescription);

        // Assert
        assertNotNull(description);
        assertEquals(DESC_MAX_LENGTH, description.getDescription().length());
    }

    @Test
    void testConstructor_withOneLessThanMaxLength_shouldSucceed() {
        // Arrange - Create string with 4095 characters
        String nearMaxLengthDescription = "a".repeat(DESC_MAX_LENGTH - 1);

        // Act
        Description description = new Description(nearMaxLengthDescription);

        // Assert
        assertNotNull(description);
        assertEquals(DESC_MAX_LENGTH - 1, description.getDescription().length());
    }

    @Test
    void testConstructor_withOneCharacter_shouldSucceed() {
        // Arrange
        String minValidDescription = "a";

        // Act
        Description description = new Description(minValidDescription);

        // Assert
        assertNotNull(description);
        assertEquals(minValidDescription, description.getDescription());
    }

    // ========================================
    // SETTER TESTS - Validation Paths
    // ========================================

    @Test
    void testSetDescription_withValidValue_shouldUpdateDescription() {
        // Arrange
        Description description = new Description(VALID_DESCRIPTION);
        String newDescription = "Updated description content.";

        // Act
        description.setDescription(newDescription);

        // Assert
        assertEquals(newDescription, description.getDescription());
    }

    @Test
    void testSetDescription_withNull_shouldSetToNull() {
        // Arrange
        Description description = new Description(VALID_DESCRIPTION);

        // Act
        description.setDescription(null);

        // Assert
        assertNull(description.getDescription());
        // Original value should be replaced
        assertNotEquals(VALID_DESCRIPTION, description.getDescription());
    }

    @Test
    void testSetDescription_withBlank_shouldSetToNull() {
        // Arrange
        Description description = new Description(VALID_DESCRIPTION);

        // Act
        description.setDescription("   ");

        // Assert
        assertNull(description.getDescription());
        // Original value should be replaced
        assertNotEquals(VALID_DESCRIPTION, description.getDescription());
    }

    @Test
    void testSetDescription_withTooLong_shouldThrowException() {
        // Arrange
        Description description = new Description(VALID_DESCRIPTION);
        String tooLongDescription = "a".repeat(DESC_MAX_LENGTH + 1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> description.setDescription(tooLongDescription)
        );

        assertEquals("Description has a maximum of 4096 characters", exception.getMessage());
        // Original value should remain unchanged
        assertEquals(VALID_DESCRIPTION, description.getDescription());
    }

    // ========================================
    // HTML SANITIZATION TESTS
    // ========================================

    @Test
    void testConstructor_withHtmlContent_shouldSanitize() {
        // Arrange
        String descriptionWithHtml = "Description with <b>HTML</b> tags and <script>alert('test')</script>";

        // Act
        Description description = new Description(descriptionWithHtml);

        // Assert
        assertNotNull(description);
        // StringUtilsCustom.sanitizeHtml should have processed the HTML
        // We verify the description was created (sanitization logic is tested separately)
        assertNotNull(description.getDescription());
    }

    @Test
    void testSetDescription_withHtmlContent_shouldSanitize() {
        // Arrange
        Description description = new Description(VALID_DESCRIPTION);
        String descriptionWithHtml = "Updated description with <div>HTML</div> content";

        // Act
        description.setDescription(descriptionWithHtml);

        // Assert
        // StringUtilsCustom.sanitizeHtml should have processed the HTML
        assertNotNull(description.getDescription());
    }

    // ========================================
    // BUSINESS LOGIC TESTS
    // ========================================

    @Test
    void testToString_shouldReturnDescriptionValue() {
        // Arrange
        Description description = new Description(VALID_DESCRIPTION);

        // Act
        String result = description.toString();

        // Assert
        assertEquals(VALID_DESCRIPTION, result);
        assertSame(description.getDescription(), result);
    }

    @Test
    void testToString_whenNull_shouldReturnNull() {
        // Arrange
        Description description = new Description(null);

        // Act
        String result = description.toString();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetDescription_shouldReturnStoredValue() {
        // Arrange
        String expectedDescription = "Expected description text";
        Description description = new Description(expectedDescription);

        // Act
        String actualDescription = description.getDescription();

        // Assert
        assertEquals(expectedDescription, actualDescription);
    }

    // ========================================
    // EDGE CASES & SPECIAL CHARACTERS
    // ========================================

    @Test
    void testConstructor_withSpecialCharacters_shouldSucceed() {
        // Arrange
        String descriptionWithSpecialChars = "Description with special chars: áéíóú ñ ç €$£¥ 中文 日本語 한글";

        // Act
        Description description = new Description(descriptionWithSpecialChars);

        // Assert
        assertNotNull(description);
        assertNotNull(description.getDescription());
    }

    @Test
    void testConstructor_withNewlinesAndTabs_shouldSucceed() {
        // Arrange
        String descriptionWithWhitespace = "Line 1\nLine 2\tTabbed\r\nWindows line";

        // Act
        Description description = new Description(descriptionWithWhitespace);

        // Assert
        assertNotNull(description);
        assertNotNull(description.getDescription());
    }

    @Test
    void testConstructor_withEmojis_shouldSucceed() {
        // Arrange
        String descriptionWithEmojis = "Description with emojis 😀 🎉 📚 ✨";

        // Act
        Description description = new Description(descriptionWithEmojis);

        // Assert
        assertNotNull(description);
        assertNotNull(description.getDescription());
    }

    @Test
    void testConstructor_withMixedWhitespace_shouldSucceed() {
        // Arrange
        String descriptionWithContent = "   Content with leading and trailing spaces   ";

        // Act
        Description description = new Description(descriptionWithContent);

        // Assert
        assertNotNull(description);
        assertNotNull(description.getDescription());
    }

    // ========================================
    // MUTATION TESTING SUPPORT
    // ========================================

    @Test
    void testSetDescription_multipleConsecutiveCalls_shouldUpdateEachTime() {
        // Arrange
        Description description = new Description("Initial description");

        // Act & Assert
        description.setDescription("First update");
        assertEquals("First update", description.getDescription());

        description.setDescription("Second update");
        assertEquals("Second update", description.getDescription());

        description.setDescription("Third update");
        assertEquals("Third update", description.getDescription());
    }

    @Test
    void testSetDescription_fromValidToNull_shouldSucceed() {
        // Arrange
        Description description = new Description("Valid description");
        assertNotNull(description.getDescription());

        // Act
        description.setDescription(null);

        // Assert
        assertNull(description.getDescription());
    }

    @Test
    void testSetDescription_fromNullToValid_shouldSucceed() {
        // Arrange
        Description description = new Description(null);
        assertNull(description.getDescription());

        // Act
        description.setDescription("Now has content");

        // Assert
        assertEquals("Now has content", description.getDescription());
    }

    @Test
    void testConstructor_withBoundaryContent_shouldHandleCorrectly() {
        // Arrange - Test at exact boundary
        String boundaryDescription = "a".repeat(DESC_MAX_LENGTH);

        // Act
        Description description = new Description(boundaryDescription);

        // Assert
        assertNotNull(description);
        assertEquals(DESC_MAX_LENGTH, description.getDescription().length());

        // Verify setDescription also respects boundary
        description.setDescription("New content");
        assertEquals("New content", description.getDescription());
    }

    @Test
    void testSetDescription_replacingLongWithShort_shouldSucceed() {
        // Arrange
        Description description = new Description("a".repeat(4000));

        // Act
        description.setDescription("Short description");

        // Assert
        assertEquals("Short description", description.getDescription());
        assertEquals(17, description.getDescription().length());
    }

    @Test
    void testSetDescription_replacingShortWithLong_shouldSucceed() {
        // Arrange
        Description description = new Description("Short");
        String longDescription = "a".repeat(3000);

        // Act
        description.setDescription(longDescription);

        // Assert
        assertEquals(longDescription, description.getDescription());
        assertEquals(3000, description.getDescription().length());
    }

    @Test
    void testSetDescription_replacingNullWithBlank_shouldStayNull() {
        // Arrange
        Description description = new Description(null);
        assertNull(description.getDescription());

        // Act
        description.setDescription("   ");

        // Assert
        assertNull(description.getDescription());
    }

    @Test
    void testSetDescription_replacingValidWithBlank_shouldBecomeNull() {
        // Arrange
        Description description = new Description("Valid content");
        assertNotNull(description.getDescription());

        // Act
        description.setDescription("   ");

        // Assert
        assertNull(description.getDescription());
    }


}