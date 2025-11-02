package pt.psoft.g1.psoftg1.readermanagement.model;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Redis.ReaderCacheRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 2.3.4 - Functional Integration Tests
 * SUT = ReaderCacheRepository (Cache-Aside Pattern)
 *
 * Testa o padrão Cache-Aside:
 * - Cache Hit: retorna direto do Redis
 * - Cache Miss: busca SQL → atualiza Redis
 * - Write-Through: escreve SQL → atualiza Redis
 * - Cache Invalidation: remove SQL → remove Redis
 * - Graceful Degradation: funciona mesmo se cache falhar
 *
 * Coordenação entre:
 * - cacheRepository (Redis)
 * - sourceRepository (SQL)
 */
@ExtendWith(MockitoExtension.class)
class ReaderCacheRepositoryIntegrationTest {

    @Mock
    private ReaderRepository cacheRepository;  // Redis

    @Mock
    private ReaderRepository sourceRepository; // SQL

    private ReaderCacheRepository readerCacheRepository;

    private ReaderDetails validReaderDetails;
    private Reader validReader;

    @BeforeEach
    void setUp() {
        readerCacheRepository = new ReaderCacheRepository(cacheRepository, sourceRepository);

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
    }

    // ==================== FIND BY READER NUMBER - CACHE HIT ====================

    @Test
    void ensureFindByReaderNumberReturnsCachedReaderWhenCacheHit() {
        // Arrange
        String readerNumber = "2025/1";
        when(cacheRepository.findByReaderNumber(readerNumber))
                .thenReturn(Optional.of(validReaderDetails));

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByReaderNumber(readerNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validReaderDetails, result.get());

        // Verify: apenas cache foi consultado
        verify(cacheRepository).findByReaderNumber(readerNumber);
        verify(sourceRepository, never()).findByReaderNumber(any());
        verify(cacheRepository, never()).save(any());
    }

    // ==================== FIND BY READER NUMBER - CACHE MISS ====================

    @Test
    void ensureFindByReaderNumberFetchesFromSQLWhenCacheMiss() {
        // Arrange
        String readerNumber = "2025/1";
        when(cacheRepository.findByReaderNumber(readerNumber))
                .thenReturn(Optional.empty()); // Cache miss
        when(sourceRepository.findByReaderNumber(readerNumber))
                .thenReturn(Optional.of(validReaderDetails));

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByReaderNumber(readerNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validReaderDetails, result.get());

        // Verify: cache miss → SQL → atualiza cache
        InOrder inOrder = inOrder(cacheRepository, sourceRepository);
        inOrder.verify(cacheRepository).findByReaderNumber(readerNumber);
        inOrder.verify(sourceRepository).findByReaderNumber(readerNumber);
        inOrder.verify(cacheRepository).save(validReaderDetails);
    }

    @Test
    void ensureFindByReaderNumberReturnsEmptyWhenNotFoundInSQL() {
        // Arrange
        String readerNumber = "2025/999";
        when(cacheRepository.findByReaderNumber(readerNumber))
                .thenReturn(Optional.empty());
        when(sourceRepository.findByReaderNumber(readerNumber))
                .thenReturn(Optional.empty());

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByReaderNumber(readerNumber);

        // Assert
        assertFalse(result.isPresent());

        // Verify: não tenta cachear quando não encontrou
        verify(cacheRepository, never()).save(any());
    }

    @Test
    void ensureFindByReaderNumberHandlesCacheFailureGracefully() {
        // Arrange
        String readerNumber = "2025/1";
        when(cacheRepository.findByReaderNumber(readerNumber))
                .thenReturn(Optional.empty());
        when(sourceRepository.findByReaderNumber(readerNumber))
                .thenReturn(Optional.of(validReaderDetails));
        doThrow(new RuntimeException("Redis unavailable"))
                .when(cacheRepository).save(any());

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByReaderNumber(readerNumber);

        // Assert - sistema continua a funcionar mesmo com cache down
        assertTrue(result.isPresent());
        assertEquals(validReaderDetails, result.get());
    }

    // ==================== FIND BY USERNAME TESTS ====================

    @Test
    void ensureFindByUsernameReturnsCachedReaderWhenCacheHit() {
        // Arrange
        String username = "john.doe@example.com";
        when(cacheRepository.findByUsername(username))
                .thenReturn(Optional.of(validReaderDetails));

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        verify(cacheRepository).findByUsername(username);
        verify(sourceRepository, never()).findByUsername(any());
    }

    @Test
    void ensureFindByUsernameFetchesFromSQLWhenCacheMiss() {
        // Arrange
        String username = "john.doe@example.com";
        when(cacheRepository.findByUsername(username))
                .thenReturn(Optional.empty());
        when(sourceRepository.findByUsername(username))
                .thenReturn(Optional.of(validReaderDetails));

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());

        InOrder inOrder = inOrder(cacheRepository, sourceRepository);
        inOrder.verify(cacheRepository).findByUsername(username);
        inOrder.verify(sourceRepository).findByUsername(username);
        inOrder.verify(cacheRepository).save(validReaderDetails);
    }

    // ==================== FIND BY USER ID TESTS ====================

    @Test
    void ensureFindByUserIdReturnsCachedReaderWhenCacheHit() {
        // Arrange
        Long userId = 1L;
        when(cacheRepository.findByUserId(userId))
                .thenReturn(Optional.of(validReaderDetails));

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByUserId(userId);

        // Assert
        assertTrue(result.isPresent());
        verify(cacheRepository).findByUserId(userId);
        verify(sourceRepository, never()).findByUserId(any());
    }

    @Test
    void ensureFindByUserIdFetchesFromSQLWhenCacheMiss() {
        // Arrange
        Long userId = 1L;
        when(cacheRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
        when(sourceRepository.findByUserId(userId))
                .thenReturn(Optional.of(validReaderDetails));

        // Act
        Optional<ReaderDetails> result = readerCacheRepository.findByUserId(userId);

        // Assert
        assertTrue(result.isPresent());

        InOrder inOrder = inOrder(cacheRepository, sourceRepository);
        inOrder.verify(cacheRepository).findByUserId(userId);
        inOrder.verify(sourceRepository).findByUserId(userId);
        inOrder.verify(cacheRepository).save(validReaderDetails);
    }

    // ==================== FIND BY PHONE NUMBER TESTS ====================

    @Test
    void ensureFindByPhoneNumberReturnsCachedReadersWhenCacheHit() {
        // Arrange
        String phoneNumber = "912345678";
        List<ReaderDetails> cachedReaders = List.of(validReaderDetails);
        when(cacheRepository.findByPhoneNumber(phoneNumber))
                .thenReturn(cachedReaders);

        // Act
        List<ReaderDetails> result = readerCacheRepository.findByPhoneNumber(phoneNumber);

        // Assert
        assertEquals(1, result.size());
        verify(cacheRepository).findByPhoneNumber(phoneNumber);
        verify(sourceRepository, never()).findByPhoneNumber(any());
    }

    @Test
    void ensureFindByPhoneNumberFetchesFromSQLWhenCacheMiss() {
        // Arrange
        String phoneNumber = "912345678";
        List<ReaderDetails> sqlReaders = List.of(validReaderDetails);
        when(cacheRepository.findByPhoneNumber(phoneNumber))
                .thenReturn(Collections.emptyList());
        when(sourceRepository.findByPhoneNumber(phoneNumber))
                .thenReturn(sqlReaders);

        // Act
        List<ReaderDetails> result = readerCacheRepository.findByPhoneNumber(phoneNumber);

        // Assert
        assertEquals(1, result.size());

        InOrder inOrder = inOrder(cacheRepository, sourceRepository);
        inOrder.verify(cacheRepository).findByPhoneNumber(phoneNumber);
        inOrder.verify(sourceRepository).findByPhoneNumber(phoneNumber);
        verify(cacheRepository).save(validReaderDetails);
    }

    // ==================== SAVE - WRITE-THROUGH PATTERN ====================

    @Test
    void ensureSaveWritesToSQLFirstThenUpdatesCache() {
        // Arrange
        when(sourceRepository.save(validReaderDetails))
                .thenReturn(validReaderDetails);

        // Act
        ReaderDetails result = readerCacheRepository.save(validReaderDetails);

        // Assert
        assertEquals(validReaderDetails, result);

        // Verify: SQL primeiro, depois cache
        InOrder inOrder = inOrder(sourceRepository, cacheRepository);
        inOrder.verify(sourceRepository).save(validReaderDetails);
        inOrder.verify(cacheRepository).save(validReaderDetails);
    }

    @Test
    void ensureSaveWorksEvenIfCacheUpdateFails() {
        // Arrange
        when(sourceRepository.save(validReaderDetails))
                .thenReturn(validReaderDetails);
        doThrow(new RuntimeException("Cache update failed"))
                .when(cacheRepository).save(any());

        // Act
        ReaderDetails result = readerCacheRepository.save(validReaderDetails);

        // Assert - sistema continua a funcionar
        assertEquals(validReaderDetails, result);
        verify(sourceRepository).save(validReaderDetails);
    }

    // ==================== DELETE - CACHE INVALIDATION ====================

    @Test
    void ensureDeleteRemovesFromSQLFirstThenInvalidatesCache() {
        // Act
        readerCacheRepository.delete(validReaderDetails);

        // Assert
        // Verify: SQL primeiro, depois cache
        InOrder inOrder = inOrder(sourceRepository, cacheRepository);
        inOrder.verify(sourceRepository).delete(validReaderDetails);
        inOrder.verify(cacheRepository).delete(validReaderDetails);
    }

    @Test
    void ensureDeleteWorksEvenIfCacheInvalidationFails() {
        // Arrange
        doThrow(new RuntimeException("Cache invalidation failed"))
                .when(cacheRepository).delete(any());

        // Act - não deve lançar exceção
        assertDoesNotThrow(() -> {
            readerCacheRepository.delete(validReaderDetails);
        });

        // Assert
        verify(sourceRepository).delete(validReaderDetails);
    }

    // ==================== QUERIES COMPLEXAS - SEMPRE SQL ====================

    @Test
    void ensureGetCountFromCurrentYearAlwaysUsesSQL() {
        // Arrange
        when(sourceRepository.getCountFromCurrentYear()).thenReturn(42);

        // Act
        int result = readerCacheRepository.getCountFromCurrentYear();

        // Assert
        assertEquals(42, result);
        verify(sourceRepository).getCountFromCurrentYear();
        verifyNoInteractions(cacheRepository);
    }

    @Test
    void ensureFindAllAlwaysUsesSQL() {
        // Arrange
        List<ReaderDetails> readers = List.of(validReaderDetails);
        when(sourceRepository.findAll()).thenReturn(readers);

        // Act
        Iterable<ReaderDetails> result = readerCacheRepository.findAll();

        // Assert
        assertNotNull(result);
        verify(sourceRepository).findAll();
        verifyNoInteractions(cacheRepository);
    }

    @Test
    void ensureFindTopReadersAlwaysUsesSQL() {
        // Act
        readerCacheRepository.findTopReaders(null);

        // Assert
        verify(sourceRepository).findTopReaders(null);
        verifyNoInteractions(cacheRepository);
    }

    @Test
    void ensureFindTopByGenreAlwaysUsesSQL() {
        // Act
        readerCacheRepository.findTopByGenre(null, "Fiction", null, null);

        // Assert
        verify(sourceRepository).findTopByGenre(null, "Fiction", null, null);
        verifyNoInteractions(cacheRepository);
    }

    @Test
    void ensureSearchReaderDetailsAlwaysUsesSQL() {
        // Act
        readerCacheRepository.searchReaderDetails(null, null);

        // Assert
        verify(sourceRepository).searchReaderDetails(null, null);
        verifyNoInteractions(cacheRepository);
    }
}