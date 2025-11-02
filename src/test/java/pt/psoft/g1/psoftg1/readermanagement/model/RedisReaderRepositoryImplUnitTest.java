package pt.psoft.g1.psoftg1.readermanagement.model;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Mapper.ReaderRedisMapper;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Redis.RedisReaderRepositoryImpl;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = RedisReaderRepositoryImpl
 *
 * Testa operações Redis do ReaderRepository:
 * - findByReaderNumber
 * - findByPhoneNumber (pode ter múltiplos readers)
 * - findByUsername
 * - findByUserId
 * - save (com múltiplos índices)
 * - delete (remove índices)
 * - findAll
 */
@ExtendWith(MockitoExtension.class)
class RedisReaderRepositoryImplUnitTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReaderRedisMapper mapper;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private RedisReaderRepositoryImpl repository;

    private ReaderDetails validReaderDetails;
    private Reader validReader;
    private Map<String, String> validHash;

    @BeforeEach
    void setUp() {
        // Setup RedisTemplate operations
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // Setup valid Reader
        validReader = Reader.newReader("john.doe@example.com", "Password123!", "John Doe");
        validReader.setId(1L);

        // Setup valid ReaderDetails
        validReaderDetails = new ReaderDetails(
                new ReaderNumber("2025/1"),
                validReader,
                new BirthDate("1990-01-15"),
                new PhoneNumber("912345678"),
                true,
                false,
                false,
                "photo.jpg",
                null
        );
        validReaderDetails.pk = 1L;

        // Setup valid hash
        validHash = new HashMap<>();
        validHash.put("pk", "1");
        validHash.put("readerNumber", "2025/1");
        validHash.put("phoneNumber", "912345678");
        validHash.put("reader_username", "john.doe@example.com");
    }

    // ==================== FIND BY READER NUMBER TESTS ====================

    @Test
    void ensureFindByReaderNumberReturnsReaderWhenExists() {
        // Arrange
        String readerNumber = "2025/1";
        String key = "reader:" + readerNumber;
        Map<Object, Object> hash = new HashMap<>(validHash);

        when(hashOperations.entries(key)).thenReturn(hash);
        when(mapper.fromRedisHash(hash)).thenReturn(validReaderDetails);

        // Act
        Optional<ReaderDetails> result = repository.findByReaderNumber(readerNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validReaderDetails, result.get());
        verify(hashOperations).entries(key);
        verify(mapper).fromRedisHash(hash);
    }

    @Test
    void ensureFindByReaderNumberReturnsEmptyWhenNotFound() {
        // Arrange
        String readerNumber = "2025/999";
        String key = "reader:" + readerNumber;

        when(hashOperations.entries(key)).thenReturn(Collections.emptyMap());

        // Act
        Optional<ReaderDetails> result = repository.findByReaderNumber(readerNumber);

        // Assert
        assertFalse(result.isPresent());
        verify(hashOperations).entries(key);
        verify(mapper, never()).fromRedisHash(any());
    }

    @Test
    void ensureFindByReaderNumberReturnsEmptyWhenReaderNumberIsNull() {
        // Act
        Optional<ReaderDetails> result = repository.findByReaderNumber(null);

        // Assert
        assertFalse(result.isPresent());
        verify(hashOperations, never()).entries(anyString());
    }

    @Test
    void ensureFindByReaderNumberReturnsEmptyWhenReaderNumberIsBlank() {
        // Act
        Optional<ReaderDetails> result = repository.findByReaderNumber("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(hashOperations, never()).entries(anyString());
    }

    // ==================== FIND BY PHONE NUMBER TESTS ====================

    @Test
    void ensureFindByPhoneNumberReturnsReadersWhenExists() {
        // Arrange
        String phoneNumber = "912345678";
        String indexKey = "reader:phone:" + phoneNumber;
        Set<Object> readerNumbers = Set.of("2025/1", "2025/2");
        Map<Object, Object> hash = new HashMap<>(validHash);

        when(setOperations.members(indexKey)).thenReturn(readerNumbers);
        when(hashOperations.entries(anyString())).thenReturn(hash);
        when(mapper.fromRedisHash(hash)).thenReturn(validReaderDetails);

        // Act
        List<ReaderDetails> result = repository.findByPhoneNumber(phoneNumber);

        // Assert
        assertEquals(2, result.size());
        verify(setOperations).members(indexKey);
        verify(hashOperations, times(2)).entries(anyString());
    }

    @Test
    void ensureFindByPhoneNumberReturnsEmptyWhenNoReadersFound() {
        // Arrange
        String phoneNumber = "999999999";
        String indexKey = "reader:phone:" + phoneNumber;

        when(setOperations.members(indexKey)).thenReturn(Collections.emptySet());

        // Act
        List<ReaderDetails> result = repository.findByPhoneNumber(phoneNumber);

        // Assert
        assertTrue(result.isEmpty());
        verify(setOperations).members(indexKey);
    }

    @Test
    void ensureFindByPhoneNumberReturnsEmptyWhenPhoneNumberIsNull() {
        // Act
        List<ReaderDetails> result = repository.findByPhoneNumber(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(setOperations, never()).members(anyString());
    }

    @Test
    void ensureFindByPhoneNumberReturnsEmptyWhenPhoneNumberIsBlank() {
        // Act
        List<ReaderDetails> result = repository.findByPhoneNumber("   ");

        // Assert
        assertTrue(result.isEmpty());
        verify(setOperations, never()).members(anyString());
    }

    // ==================== FIND BY USERNAME TESTS ====================

    @Test
    void ensureFindByUsernameReturnsReaderWhenExists() {
        // Arrange
        String username = "john.doe@example.com";
        String indexKey = "reader:username:" + username.toLowerCase();
        String readerNumber = "2025/1";
        Map<Object, Object> hash = new HashMap<>(validHash);

        when(valueOperations.get(indexKey)).thenReturn(readerNumber);
        when(hashOperations.entries("reader:" + readerNumber)).thenReturn(hash);
        when(mapper.fromRedisHash(hash)).thenReturn(validReaderDetails);

        // Act
        Optional<ReaderDetails> result = repository.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validReaderDetails, result.get());
        verify(valueOperations).get(indexKey);
    }

    @Test
    void ensureFindByUsernameConvertsToLowerCase() {
        // Arrange
        String username = "JOHN.DOE@EXAMPLE.COM";
        String indexKey = "reader:username:john.doe@example.com";

        when(valueOperations.get(indexKey)).thenReturn(null);

        // Act
        repository.findByUsername(username);

        // Assert
        verify(valueOperations).get(indexKey);
    }

    @Test
    void ensureFindByUsernameReturnsEmptyWhenNotFound() {
        // Arrange
        String username = "notfound@example.com";
        String indexKey = "reader:username:" + username.toLowerCase();

        when(valueOperations.get(indexKey)).thenReturn(null);

        // Act
        Optional<ReaderDetails> result = repository.findByUsername(username);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations).get(indexKey);
    }

    @Test
    void ensureFindByUsernameReturnsEmptyWhenUsernameIsNull() {
        // Act
        Optional<ReaderDetails> result = repository.findByUsername(null);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    // ==================== FIND BY USER ID TESTS ====================

    @Test
    void ensureFindByUserIdReturnsReaderWhenExists() {
        // Arrange
        Long userId = 1L;
        String indexKey = "reader:userid:" + userId;
        String readerNumber = "2025/1";
        Map<Object, Object> hash = new HashMap<>(validHash);

        when(valueOperations.get(indexKey)).thenReturn(readerNumber);
        when(hashOperations.entries("reader:" + readerNumber)).thenReturn(hash);
        when(mapper.fromRedisHash(hash)).thenReturn(validReaderDetails);

        // Act
        Optional<ReaderDetails> result = repository.findByUserId(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validReaderDetails, result.get());
        verify(valueOperations).get(indexKey);
    }

    @Test
    void ensureFindByUserIdReturnsEmptyWhenNotFound() {
        // Arrange
        Long userId = 999L;
        String indexKey = "reader:userid:" + userId;

        when(valueOperations.get(indexKey)).thenReturn(null);

        // Act
        Optional<ReaderDetails> result = repository.findByUserId(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations).get(indexKey);
    }

    @Test
    void ensureFindByUserIdReturnsEmptyWhenUserIdIsNull() {
        // Act
        Optional<ReaderDetails> result = repository.findByUserId(null);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, never()).get(anyString());
    }

    // ==================== SAVE TESTS ====================

    @Test
    void ensureSaveCreatesAllIndices() {
        // Arrange
        when(mapper.toRedisHash(validReaderDetails)).thenReturn(validHash);

        // Act
        ReaderDetails result = repository.save(validReaderDetails);

        // Assert
        assertEquals(validReaderDetails, result);

        // Verify main hash saved
        verify(hashOperations).putAll(eq("reader:2025/1"), eq(validHash));
        verify(redisTemplate).expire(eq("reader:2025/1"), anyLong(), any());

        // Verify indices created
        verify(valueOperations, atLeast(2)).set(anyString(), any(), any());
        verify(setOperations, atLeast(2)).add(anyString(), any());
    }

    @Test
    void ensureSaveThrowsExceptionWhenReaderDetailsIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(null);
        });

        verify(mapper, never()).toRedisHash(any());
    }

    @Test
    void ensureSaveThrowsExceptionWhenReaderNumberIsNull() {
        // Arrange
        ReaderDetails readerWithoutNumber = new ReaderDetails();
        readerWithoutNumber.setReader(validReader);
        readerWithoutNumber.setPhoneNumber(new PhoneNumber("912345678"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(readerWithoutNumber);
        });

        verify(mapper, never()).toRedisHash(any());
    }

    // ==================== DELETE TESTS ====================

    @Test
    void ensureDeleteRemovesAllIndices() {
        // Act
        repository.delete(validReaderDetails);

        // Assert
        // Verify main hash deleted
        verify(redisTemplate).delete("reader:2025/1");

        // Verify readerNumber index deleted
        verify(redisTemplate).delete("reader:number:2025/1");

        // Verify username index deleted
        verify(redisTemplate).delete("reader:username:john.doe@example.com");

        // Verify userId index deleted
        verify(redisTemplate).delete("reader:userid:1");

        // Verify phone index updated
        verify(setOperations).remove("reader:phone:912345678", "2025/1");

        // Verify removed from all readers set
        verify(setOperations).remove("reader:all", "2025/1");
    }

    @Test
    void ensureDeleteHandlesNullReaderDetails() {
        // Act
        repository.delete(null);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void ensureDeleteHandlesReaderDetailsWithoutReaderNumber() {
        // Arrange
        ReaderDetails readerWithoutNumber = new ReaderDetails();

        // Act
        repository.delete(readerWithoutNumber);

        // Assert
        verify(redisTemplate, never()).delete(anyString());
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void ensureFindAllReturnsAllReaders() {
        // Arrange
        Set<Object> readerNumbers = Set.of("2025/1", "2025/2");
        Map<Object, Object> hash = new HashMap<>(validHash);

        when(setOperations.members("reader:all")).thenReturn(readerNumbers);
        when(hashOperations.entries(anyString())).thenReturn(hash);
        when(mapper.fromRedisHash(hash)).thenReturn(validReaderDetails);

        // Act
        Iterable<ReaderDetails> result = repository.findAll();

        // Assert
        List<ReaderDetails> resultList = new ArrayList<>();
        result.forEach(resultList::add);
        assertEquals(2, resultList.size());
        verify(setOperations).members("reader:all");
    }

    @Test
    void ensureFindAllReturnsEmptyWhenNoReaders() {
        // Arrange
        when(setOperations.members("reader:all")).thenReturn(Collections.emptySet());

        // Act
        Iterable<ReaderDetails> result = repository.findAll();

        // Assert
        assertFalse(result.iterator().hasNext());
    }

    // ==================== UNSUPPORTED OPERATIONS TESTS ====================

    @Test
    void ensureGetCountFromCurrentYearReturnsZero() {
        // Act
        int count = repository.getCountFromCurrentYear();

        // Assert
        assertEquals(0, count);
    }

    @Test
    void ensureFindTopReadersReturnsEmptyPage() {
        // Act
        var result = repository.findTopReaders(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureFindTopByGenreReturnsEmptyPage() {
        // Act
        var result = repository.findTopByGenre(null, "Fiction", null, null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureSearchReaderDetailsReturnsEmptyList() {
        // Act
        List<ReaderDetails> result = repository.searchReaderDetails(null, null);

        // Assert
        assertTrue(result.isEmpty());
    }
}
