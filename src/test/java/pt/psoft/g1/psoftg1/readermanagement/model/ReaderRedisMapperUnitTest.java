package pt.psoft.g1.psoftg1.readermanagement.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Mapper.ReaderRedisMapper;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = ReaderRedisMapper
 *
 * Testa conversão ReaderDetails ↔ Redis Hash
 *
 * ReaderDetails é mais complexo que Book/Author/Genre porque:
 * - Tem nested object Reader (username, password, name)
 * - Tem múltiplos Value Objects (ReaderNumber, PhoneNumber, BirthDate)
 * - Tem múltiplos consents (GDPR, marketing, third-party)
 * - Tem photo opcional
 */
class ReaderRedisMapperUnitTest {

    private ReaderRedisMapper mapper;
    private ReaderDetails validReaderDetails;
    private Reader validReader;

    @BeforeEach
    void setUp() {
        mapper = new ReaderRedisMapper();

        // Setup valid Reader (nested object)
        validReader = Reader.newReader("john.doe@example.com", "Password123!", "John Doe");
        validReader.setId(1L);

        // Setup valid ReaderDetails
        validReaderDetails = new ReaderDetails(
                2025,
                validReader,
                "1990-01-15",
                "912345678",
                true,  // GDPR
                false, // Marketing
                false, // Third-party
                "photo.jpg",
                null   // Interest list (não usado no Redis)
        );
        validReaderDetails.pk = 1L;
        validReaderDetails.setVersion(1L);
    }

    // ==================== TO REDIS HASH TESTS ====================

    @Test
    void ensureToRedisHashConvertsCompleteReaderSuccessfully() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(validReaderDetails);

        // Assert
        assertNotNull(hash);
        assertEquals("1", hash.get("pk"));
        assertEquals("1", hash.get("version"));
        assertTrue(hash.get("readerNumber").startsWith("2025/"));
        assertEquals("912345678", hash.get("phoneNumber"));
        assertEquals("1990-01-15", hash.get("birthDate"));
        assertEquals("true", hash.get("gdprConsent"));
        assertEquals("false", hash.get("marketingConsent"));
        assertEquals("false", hash.get("thirdPartySharingConsent"));
        assertEquals("photo.jpg", hash.get("photo"));
        assertEquals("john.doe@example.com", hash.get("reader_username"));
        assertEquals("John Doe", hash.get("reader_name"));
        assertEquals("1", hash.get("reader_id"));
        assertNotNull(hash.get("reader_password")); // Password hash deve existir
    }

    @Test
    void ensureToRedisHashReturnsNullWhenReaderDetailsIsNull() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(null);

        // Assert
        assertNull(hash);
    }

    @Test
    void ensureToRedisHashHandlesReaderWithoutPk() {
        // Arrange
        validReaderDetails.pk = null;

        // Act
        Map<String, String> hash = mapper.toRedisHash(validReaderDetails);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("pk"));
        assertTrue(hash.containsKey("readerNumber"));
    }

    @Test
    void ensureToRedisHashHandlesReaderWithoutVersion() {
        // Arrange
        validReaderDetails.setVersion(null);

        // Act
        Map<String, String> hash = mapper.toRedisHash(validReaderDetails);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("version"));
    }

    @Test
    void ensureToRedisHashHandlesReaderWithoutPhoto() {
        // Arrange
        ReaderDetails readerWithoutPhoto = new ReaderDetails(
                2025,
                validReader,
                "1990-01-15",
                "912345678",
                true,
                false,
                false,
                null, // No photo
                null
        );

        // Act
        Map<String, String> hash = mapper.toRedisHash(readerWithoutPhoto);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("photo"));
    }

    @Test
    void ensureToRedisHashHandlesAllConsentsTrue() {
        // Arrange
        ReaderDetails readerWithAllConsents = new ReaderDetails(
                2025,
                validReader,
                "1990-01-15",
                "912345678",
                true, // GDPR
                true, // Marketing
                true, // Third-party
                "photo.jpg",
                null
        );

        // Act
        Map<String, String> hash = mapper.toRedisHash(readerWithAllConsents);

        // Assert
        assertEquals("true", hash.get("gdprConsent"));
        assertEquals("true", hash.get("marketingConsent"));
        assertEquals("true", hash.get("thirdPartySharingConsent"));
    }

    @Test
    void ensureToRedisHashHandlesReaderNumberWithDifferentYear() {
        // Arrange
        ReaderDetails readerDifferentYear = new ReaderDetails(
                new ReaderNumber(2024, 999),
                validReader,
                new BirthDate("1990-01-15"),
                new PhoneNumber("912345678"),
                true,
                false,
                false,
                "photo.jpg",
                null
        );

        // Act
        Map<String, String> hash = mapper.toRedisHash(readerDifferentYear);

        // Assert
        assertEquals("2024/999", hash.get("readerNumber"));
    }

    @Test
    void ensureToRedisHashHandlesLongPhoneNumber() {
        // Arrange
        ReaderDetails readerLongPhone = new ReaderDetails(
                2025,
                validReader,
                "1990-01-15",
                "912345678901234", // Max length
                true,
                false,
                false,
                null,
                null
        );

        // Act
        Map<String, String> hash = mapper.toRedisHash(readerLongPhone);

        // Assert
        assertEquals("912345678901234", hash.get("phoneNumber"));
    }

    // ==================== FROM REDIS HASH TESTS ====================

    @Test
    void ensureFromRedisHashConvertsCompleteHashSuccessfully() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "1");
        hash.put("version", "1");
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("birthDate", "1990-01-15");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("photo", "photo.jpg");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");
        hash.put("reader_name", "John Doe");
        hash.put("reader_id", "1");

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertEquals(1L, reader.getPk());
        assertEquals("2025/1", reader.getReaderNumber());
        assertEquals("912345678", reader.getPhoneNumber());
        assertTrue(reader.isGdprConsent());
        assertFalse(reader.isMarketingConsent());
        assertFalse(reader.isThirdPartySharingConsent());
        assertNotNull(reader.getReader());
        assertEquals("john.doe@example.com", reader.getReader().getUsername());
        assertEquals("John Doe", reader.getReader().getName().toString());
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenHashIsNull() {
        // Act
        ReaderDetails reader = mapper.fromRedisHash(null);

        // Assert
        assertNull(reader);
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenHashIsEmpty() {
        // Act
        ReaderDetails reader = mapper.fromRedisHash(new HashMap<>());

        // Assert
        assertNull(reader);
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenUsernameIsMissing() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("reader_password", "hashedPassword123");
        // Missing reader_username

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNull(reader);
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenPasswordIsMissing() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("reader_username", "john.doe@example.com");
        // Missing reader_password

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNull(reader);
    }

    @Test
    void ensureFromRedisHashHandlesHashWithoutPk() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("birthDate", "1990-01-15");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");
        hash.put("reader_name", "John Doe");
        // No pk

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertNull(reader.getPk());
    }

    @Test
    void ensureFromRedisHashHandlesHashWithoutVersion() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");
        // No version

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertNull(reader.getVersion());
    }

    @Test
    void ensureFromRedisHashHandlesHashWithoutPhoto() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");
        // No photo

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertNull(reader.getPhoto());
    }

    @Test
    void ensureFromRedisHashHandlesHashWithoutBirthDate() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");
        // No birthDate

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertNull(reader.getBirthDate());
    }

    @Test
    void ensureFromRedisHashHandlesHashWithoutReaderName() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");
        hash.put("reader_id", "1");
        // No reader_name

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertNotNull(reader.getReader());
        // Name pode ser null no Reader
    }

    @Test
    void ensureFromRedisHashHandlesAllConsentsFalse() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("gdprConsent", "false");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertFalse(reader.isGdprConsent());
        assertFalse(reader.isMarketingConsent());
        assertFalse(reader.isThirdPartySharingConsent());
    }

    @Test
    void ensureFromRedisHashHandlesInvalidBirthDateFormat() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("birthDate", "invalid-date");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "john.doe@example.com");
        hash.put("reader_password", "hashedPassword123");

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert - deve retornar null porque BirthDate lança exceção
        assertNull(reader);
    }

    // ==================== ROUND-TRIP TESTS ====================

    @Test
    void ensureRoundTripConversionPreservesAllData() {
        // Arrange - original ReaderDetails
        ReaderDetails original = validReaderDetails;

        // Act - convert to hash and back
        Map<String, String> hash = mapper.toRedisHash(original);
        Map<Object, Object> hashAsObject = new HashMap<>(hash);
        ReaderDetails reconstructed = mapper.fromRedisHash(hashAsObject);

        // Assert
        assertNotNull(reconstructed);
        assertEquals(original.getPk(), reconstructed.getPk());
        assertEquals(original.getReaderNumber(), reconstructed.getReaderNumber());
        assertEquals(original.getPhoneNumber(), reconstructed.getPhoneNumber());
        assertEquals(original.getBirthDate().toString(), reconstructed.getBirthDate().toString());
        assertEquals(original.isGdprConsent(), reconstructed.isGdprConsent());
        assertEquals(original.isMarketingConsent(), reconstructed.isMarketingConsent());
        assertEquals(original.isThirdPartySharingConsent(), reconstructed.isThirdPartySharingConsent());
        assertEquals(original.getReader().getUsername(), reconstructed.getReader().getUsername());
        assertEquals(original.getReader().getName().toString(), reconstructed.getReader().getName().toString());
    }

    @Test
    void ensureRoundTripConversionWithMinimalData() {
        // Arrange - minimal ReaderDetails (sem photo, sem birthDate)
        Reader minimalReader = Reader.newReader("minimal@example.com", "Pass123!", "Min User");
        ReaderDetails minimal = new ReaderDetails();
        minimal.setReader(minimalReader);
        minimal.setReaderNumber(new ReaderNumber("2025/999"));
        minimal.setPhoneNumber(new PhoneNumber("999999999"));
        minimal.setGdprConsent(true);
        minimal.setMarketingConsent(false);
        minimal.setThirdPartySharingConsent(false);

        // Act
        Map<String, String> hash = mapper.toRedisHash(minimal);
        Map<Object, Object> hashAsObject = new HashMap<>(hash);
        ReaderDetails reconstructed = mapper.fromRedisHash(hashAsObject);

        // Assert
        assertNotNull(reconstructed);
        assertEquals(minimal.getReaderNumber(), reconstructed.getReaderNumber());
        assertEquals(minimal.getPhoneNumber(), reconstructed.getPhoneNumber());
        assertEquals(minimal.getReader().getUsername(), reconstructed.getReader().getUsername());
    }

    @Test
    void ensureToRedisHashHandlesSpecialCharactersInName() {
        // Arrange
        Reader readerWithSpecialChars = Reader.newReader(
                "user@example.com",
                "Pass123!",
                "José António Silva-Santos"
        );
        ReaderDetails readerDetails = new ReaderDetails(
                2025,
                readerWithSpecialChars,
                "1990-01-15",
                "912345678",
                true,
                false,
                false,
                null,
                null
        );

        // Act
        Map<String, String> hash = mapper.toRedisHash(readerDetails);

        // Assert
        assertNotNull(hash);
        assertEquals("José António Silva-Santos", hash.get("reader_name"));
    }

    @Test
    void ensureFromRedisHashHandlesSpecialCharactersInName() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/1");
        hash.put("phoneNumber", "912345678");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "user@example.com");
        hash.put("reader_password", "hashedPassword123");
        hash.put("reader_name", "José António Silva-Santos");

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertEquals("José António Silva-Santos", reader.getReader().getName().toString());
    }

    @Test
    void ensureToRedisHashHandlesReaderNumberWithLeadingZeros() {
        // Arrange
        ReaderDetails readerWithLeadingZeros = new ReaderDetails(
                new ReaderNumber("2025/001"),
                validReader,
                new BirthDate("1990-01-15"),
                new PhoneNumber("912345678"),
                true,
                false,
                false,
                null,
                null
        );

        // Act
        Map<String, String> hash = mapper.toRedisHash(readerWithLeadingZeros);

        // Assert
        assertEquals("2025/001", hash.get("readerNumber"));
    }

    @Test
    void ensureFromRedisHashHandlesReaderNumberWithLeadingZeros() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("readerNumber", "2025/001");
        hash.put("phoneNumber", "912345678");
        hash.put("gdprConsent", "true");
        hash.put("marketingConsent", "false");
        hash.put("thirdPartySharingConsent", "false");
        hash.put("reader_username", "user@example.com");
        hash.put("reader_password", "hashedPassword123");

        // Act
        ReaderDetails reader = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(reader);
        assertEquals("2025/001", reader.getReaderNumber());
    }
}
