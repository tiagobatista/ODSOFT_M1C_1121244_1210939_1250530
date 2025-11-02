package pt.psoft.g1.psoftg1.readermanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Mapper.ReaderRedisMapper;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Redis.RedisReaderRepositoryImpl;
import pt.psoft.g1.psoftg1.readermanagement.model.*;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Opaque-Box Unit Tests for RedisReaderRepositoryImpl
 *
 * Type: 2.3.1 - Functional opaque-box with SUT = classes
 * @author ARQSOFT 2025-2026
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

    private RedisReaderRepositoryImpl repository;
    private ReaderDetails testReader;
    private Reader testUser;
    private Map<String, String> testHash;
    private Map<Object, Object> testHashObject;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);

        repository = new RedisReaderRepositoryImpl(redisTemplate, mapper);

        testUser = Reader.newReader("john.doe", "Password123", "John Doe");
        testUser.setId(1L);

        testReader = new ReaderDetails(
                new ReaderNumber("2024/001"),
                testUser,
                new BirthDate("1990-01-15"),
                new PhoneNumber("912345678"),
                true, false, false, null, Collections.emptyList()
        );
        testReader.pk = 1L;

        testHash = new HashMap<>();
        testHash.put("pk", "1");
        testHash.put("readerNumber", "2024/001");
        testHash.put("phoneNumber", "912345678");
        testHash.put("reader_username", "john.doe");
        testHash.put("reader_id", "1");

        testHashObject = new HashMap<>(testHash);
    }

    // DELETE TESTS
    @Test
    void testDelete_withValidReader_shouldDeleteAllIndexes() {
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(setOperations.remove(anyString(), any())).thenReturn(1L);

        repository.delete(testReader);

        verify(redisTemplate).delete("reader:number:2024/001");
        verify(redisTemplate).delete("reader:username:john.doe");
        verify(redisTemplate).delete("reader:userid:1");
        verify(setOperations).remove("reader:phone:912345678", "2024/001");
        verify(setOperations).remove("reader:all", "2024/001");
        verify(redisTemplate).delete("reader:2024/001");
    }

    @Test
    void testDelete_withNull_shouldNotThrowException() {
        repository.delete(null);
        verify(redisTemplate, never()).delete(anyString());
    }

    // NOT SUPPORTED OPERATIONS
    @Test
    void testGetCountFromCurrentYear_shouldReturnZero() {
        int result = repository.getCountFromCurrentYear();
        assertEquals(0, result);
    }

    @Test
    void testFindTopReaders_shouldReturnEmptyPage() {
        Page<ReaderDetails> result = repository.findTopReaders(PageRequest.of(0, 10));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindTopByGenre_shouldReturnEmptyPage() {
        Page result = repository.findTopByGenre(
                PageRequest.of(0, 10),
                "Fiction",
                LocalDate.now().minusMonths(1),
                LocalDate.now()
        );
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchReaderDetails_shouldReturnEmptyList() {
        var page = new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);
        var query = new SearchReadersQuery();

        List<ReaderDetails> result = repository.searchReaderDetails(page, query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}