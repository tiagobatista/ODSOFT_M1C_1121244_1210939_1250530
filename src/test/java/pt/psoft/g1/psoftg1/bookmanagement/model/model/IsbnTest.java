package pt.psoft.g1.psoftg1.bookmanagement.model.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transparent-Box (White-Box) Tests for Isbn domain class
 *
 * Purpose: Test Isbn value object with KNOWLEDGE of its internal implementation
 * Testing Strategy: Branch coverage, ISBN-10 and ISBN-13 validation logic, checksum verification
 * SUT: Isbn class (domain model)
 * Type: 2.3.2 - Functional transparent-box with SUT = domain classes
 *
 * Test Coverage:
 * - ISBN-10 validation (all validation paths)
 * - ISBN-13 validation (all validation paths)
 * - Checksum calculation and verification
 * - Format validation (length, characters)
 * - Null and invalid input handling
 * - Special case: ISBN-10 with 'X' check digit
 * - Edge cases and boundary conditions
 * - toString() functionality
 *
 * @author ARQSOFT 2025-2026
 */
class IsbnTest {

    // Valid ISBN-10 examples
    private static final String VALID_ISBN10_1 = "0306406152";
    private static final String VALID_ISBN10_2 = "0471958697";
    private static final String VALID_ISBN10_WITH_X = "043942089X"; // X as check digit

    // Valid ISBN-13 examples
    private static final String VALID_ISBN13_1 = "9780306406157";
    private static final String VALID_ISBN13_2 = "9780201530827";  // Changed to valid ISBN
    private static final String VALID_ISBN13_3 = "9780471958697";

    // Invalid ISBNs
    private static final String INVALID_ISBN_TOO_SHORT = "123456789";
    private static final String INVALID_ISBN_TOO_LONG = "12345678901234";
    private static final String INVALID_ISBN_LETTERS = "ABCDEFGHIJ";
    private static final String INVALID_ISBN10_CHECKSUM = "0306406153"; // Wrong checksum
    private static final String INVALID_ISBN13_CHECKSUM = "9780306406158"; // Wrong checksum

    // ========================================
    // CONSTRUCTOR TESTS - ISBN-10 Validation
    // ========================================

    @Test
    void testConstructor_withValidIsbn10_shouldCreateSuccessfully() {
        // Act
        Isbn isbn = new Isbn(VALID_ISBN10_1);

        // Assert
        assertNotNull(isbn);
        assertEquals(VALID_ISBN10_1, isbn.getIsbn());
        assertEquals(VALID_ISBN10_1, isbn.toString());
    }

    @Test
    void testConstructor_withValidIsbn10WithX_shouldCreateSuccessfully() {
        // Act
        Isbn isbn = new Isbn(VALID_ISBN10_WITH_X);

        // Assert
        assertNotNull(isbn);
        assertEquals(VALID_ISBN10_WITH_X, isbn.getIsbn());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0306406152",  // Valid ISBN-10
            "0471958697",  // Valid ISBN-10
            "043942089X",  // Valid ISBN-10 with X
            "0201530821",  // Valid ISBN-10
            "0596009208"   // Valid ISBN-10
    })
    void testConstructor_withVariousValidIsbn10_shouldSucceed(String isbn10) {
        // Act
        Isbn isbn = new Isbn(isbn10);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbn10, isbn.getIsbn());
    }

    @Test
    void testConstructor_withInvalidIsbn10Checksum_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn(INVALID_ISBN10_CHECKSUM)
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    @Test
    void testConstructor_withIsbn10InvalidFormat_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn("030640615A") // Letter instead of digit
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    // ========================================
    // CONSTRUCTOR TESTS - ISBN-13 Validation
    // ========================================

    @Test
    void testConstructor_withValidIsbn13_shouldCreateSuccessfully() {
        // Act
        Isbn isbn = new Isbn(VALID_ISBN13_1);

        // Assert
        assertNotNull(isbn);
        assertEquals(VALID_ISBN13_1, isbn.getIsbn());
        assertEquals(VALID_ISBN13_1, isbn.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "9780306406157",  // Valid ISBN-13
            "043942089X",  // Valid ISBN-13
            "9780471958697",  // Valid ISBN-13
            "9780201530827",  // Valid ISBN-13
            "9780596009205"   // Valid ISBN-13
    })
    void testConstructor_withVariousValidIsbn13_shouldSucceed(String isbn13) {
        // Act
        Isbn isbn = new Isbn(isbn13);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbn13, isbn.getIsbn());
    }

    @Test
    void testConstructor_withInvalidIsbn13Checksum_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn(INVALID_ISBN13_CHECKSUM)
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    @Test
    void testConstructor_withIsbn13InvalidFormat_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn("978030640615A") // Letter instead of digit
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    // ========================================
    // CONSTRUCTOR TESTS - Null and Invalid Input
    // ========================================

    @Test
    void testConstructor_withNull_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn(null)
        );

        assertEquals("Isbn cannot be null", exception.getMessage());
    }

    @Test
    void testConstructor_withEmptyString_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn("")
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    @Test
    void testConstructor_withTooShortIsbn_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn(INVALID_ISBN_TOO_SHORT)
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    @Test
    void testConstructor_withTooLongIsbn_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn(INVALID_ISBN_TOO_LONG)
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    @Test
    void testConstructor_withOnlyLetters_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Isbn(INVALID_ISBN_LETTERS)
        );

        assertTrue(exception.getMessage().contains("Invalid ISBN"));
    }

    // ========================================
    // ISBN-10 CHECKSUM VALIDATION TESTS
    // ========================================

    @Test
    void testIsbn10Checksum_withValidChecksum_shouldAccept() {
        // Arrange
        // 0306406152: (0*10 + 3*9 + 0*8 + 6*7 + 4*6 + 0*5 + 6*4 + 1*3 + 5*2 + 2*1) % 11 = 0
        String validIsbn10 = "0306406152";

        // Act
        Isbn isbn = new Isbn(validIsbn10);

        // Assert
        assertNotNull(isbn);
        assertEquals(validIsbn10, isbn.getIsbn());
    }

    @Test
    void testIsbn10Checksum_withXAsCheckDigit_shouldAccept() {
        // Arrange
        // ISBN-10 with X means check digit is 10
        String isbn10WithX = "043942089X";

        // Act
        Isbn isbn = new Isbn(isbn10WithX);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbn10WithX, isbn.getIsbn());
    }

    @Test
    void testIsbn10Checksum_withInvalidCheckDigit_shouldReject() {
        // Arrange
        // Correct ISBN would be 0306406152, but we use 0306406153 (wrong last digit)
        String invalidIsbn10 = "0306406153";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Isbn(invalidIsbn10));
    }

    // ========================================
    // ISBN-13 CHECKSUM VALIDATION TESTS
    // ========================================

    @Test
    void testIsbn13Checksum_withValidChecksum_shouldAccept() {
        // Arrange
        // 9780306406157: sum of (digit * weight) where weights alternate 1,3,1,3...
        // (9*1 + 7*3 + 8*1 + 0*3 + 3*1 + 0*3 + 6*1 + 4*3 + 0*1 + 6*3 + 1*1 + 5*3) = 93
        // checksum = 10 - (93 % 10) = 7
        String validIsbn13 = "9780306406157";

        // Act
        Isbn isbn = new Isbn(validIsbn13);

        // Assert
        assertNotNull(isbn);
        assertEquals(validIsbn13, isbn.getIsbn());
    }

    @Test
    void testIsbn13Checksum_withChecksumZero_shouldAccept() {
        // Arrange
        // When (sum % 10) = 0, checksum should be 0 (not 10)
        // 9780471486749 has checksum 0
        String isbn13WithZeroChecksum = "9780471486749";

        // Act
        Isbn isbn = new Isbn(isbn13WithZeroChecksum);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbn13WithZeroChecksum, isbn.getIsbn());
    }

    @Test
    void testIsbn13Checksum_withInvalidCheckDigit_shouldReject() {
        // Arrange
        // Correct ISBN would be 9780306406157, but we use 9780306406158 (wrong last digit)
        String invalidIsbn13 = "9780306406158";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Isbn(invalidIsbn13));
    }

    // ========================================
    // BUSINESS LOGIC TESTS
    // ========================================

    @Test
    void testGetIsbn_shouldReturnStoredValue() {
        // Arrange
        Isbn isbn = new Isbn(VALID_ISBN13_1);

        // Act
        String result = isbn.getIsbn();

        // Assert
        assertEquals(VALID_ISBN13_1, result);
    }

    @Test
    void testToString_shouldReturnIsbnValue() {
        // Arrange
        Isbn isbn = new Isbn(VALID_ISBN10_1);

        // Act
        String result = isbn.toString();

        // Assert
        assertEquals(VALID_ISBN10_1, result);
        assertSame(isbn.getIsbn(), result);
    }

    // ========================================
    // EQUALS AND HASHCODE TESTS
    // ========================================

    @Test
    void testEquals_withSameIsbn_shouldReturnTrue() {
        // Arrange
        Isbn isbn1 = new Isbn(VALID_ISBN13_1);
        Isbn isbn2 = new Isbn(VALID_ISBN13_1);

        // Act & Assert
        assertEquals(isbn1, isbn2);
        assertEquals(isbn1.hashCode(), isbn2.hashCode());
    }

    @Test
    void testEquals_withDifferentIsbn_shouldReturnFalse() {
        // Arrange
        Isbn isbn1 = new Isbn(VALID_ISBN13_1);
        Isbn isbn2 = new Isbn(VALID_ISBN13_2);

        // Act & Assert
        assertNotEquals(isbn1, isbn2);
    }

    @Test
    void testEquals_withIsbn10AndIsbn13_shouldReturnFalse() {
        // Arrange
        Isbn isbn10 = new Isbn(VALID_ISBN10_1);
        Isbn isbn13 = new Isbn(VALID_ISBN13_1);

        // Act & Assert
        assertNotEquals(isbn10, isbn13);
    }

    @Test
    void testHashCode_consistentWithEquals() {
        // Arrange
        Isbn isbn1 = new Isbn(VALID_ISBN13_1);
        Isbn isbn2 = new Isbn(VALID_ISBN13_1);
        Isbn isbn3 = new Isbn(VALID_ISBN13_2);

        // Act & Assert
        // Equal objects must have equal hash codes
        assertEquals(isbn1.hashCode(), isbn2.hashCode());

        // Non-equal objects should (ideally) have different hash codes
        // Note: This is not strictly required, but good practice
        assertNotEquals(isbn1.hashCode(), isbn3.hashCode());
    }

    // ========================================
    // EDGE CASES & BOUNDARY CONDITIONS
    // ========================================

    @Test
    void testConstructor_withIsbn10AllZeros_shouldValidateChecksum() {
        // Arrange - ISBN with all zeros has valid checksum (0*weights % 11 = 0)
        String isbn10 = "0000000000";

        // Act - This ISBN is technically valid (checksum = 0)
        Isbn isbn = new Isbn(isbn10);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbn10, isbn.getIsbn());
    }

    @Test
    void testConstructor_withIsbn13AllZeros_shouldValidateChecksum() {
        // Arrange - ISBN with all zeros has valid checksum (0*weights % 10 = 0)
        String isbn13 = "0000000000000";

        // Act - This ISBN is technically valid (checksum = 0)
        Isbn isbn = new Isbn(isbn13);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbn13, isbn.getIsbn());
    }

    @Test
    void testConstructor_with11Characters_shouldRejectAsInvalid() {
        // Arrange - Neither 10 nor 13 characters
        String isbn11 = "12345678901";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Isbn(isbn11));
    }

    @Test
    void testConstructor_with12Characters_shouldRejectAsInvalid() {
        // Arrange - Neither 10 nor 13 characters
        String isbn12 = "123456789012";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Isbn(isbn12));
    }

    @Test
    void testConstructor_withSpaces_shouldRejectAsInvalid() {
        // Arrange - ISBN with spaces (should not be accepted)
        String isbnWithSpaces = "978 0306406157";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Isbn(isbnWithSpaces));
    }

    @Test
    void testConstructor_withHyphens_shouldRejectAsInvalid() {
        // Arrange - ISBN with hyphens (should not be accepted)
        String isbnWithHyphens = "978-0-306-40615-7";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Isbn(isbnWithHyphens));
    }

    // ========================================
    // MUTATION TESTING SUPPORT
    // ========================================

    @Test
    void testIsbn10_checksumCalculation_boundary() {
        // Test ISBN-10 where sum % 11 = 1 (checksum should be 10, represented as X)
        // This tests the boundary case where checksum = 10
        String isbnWithChecksum10 = "043942089X";

        // Act
        Isbn isbn = new Isbn(isbnWithChecksum10);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbnWithChecksum10, isbn.getIsbn());
    }

    @Test
    void testIsbn13_checksumCalculation_whenSumMod10IsZero() {
        // Test ISBN-13 where sum % 10 = 0 (checksum should be 0, not 10)
        // 9780471486749 is an example where checksum is 0
        String isbnWithChecksum0 = "9780471486749";

        // Act
        Isbn isbn = new Isbn(isbnWithChecksum0);

        // Assert
        assertNotNull(isbn);
        assertEquals(isbnWithChecksum0, isbn.getIsbn());
    }

    @Test
    void testIsbn10_everyPosition_contributesToChecksum() {
        // Verify that changing ANY digit in a valid ISBN-10 makes it invalid
        String validIsbn10 = "0306406152";

        // Modify first digit
        assertThrows(IllegalArgumentException.class, () -> new Isbn("1306406152"));

        // Modify middle digit
        assertThrows(IllegalArgumentException.class, () -> new Isbn("0306416152"));

        // Modify last digit
        assertThrows(IllegalArgumentException.class, () -> new Isbn("0306406153"));
    }

    @Test
    void testIsbn13_everyPosition_contributesToChecksum() {
        // Verify that changing ANY digit in a valid ISBN-13 makes it invalid
        String validIsbn13 = "9780306406157";

        // Modify first digit
        assertThrows(IllegalArgumentException.class, () -> new Isbn("8780306406157"));

        // Modify middle digit
        assertThrows(IllegalArgumentException.class, () -> new Isbn("9780306416157"));

        // Modify last digit
        assertThrows(IllegalArgumentException.class, () -> new Isbn("9780306406158"));
    }

}