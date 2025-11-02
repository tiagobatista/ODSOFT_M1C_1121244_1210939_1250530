package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Redis.BookCacheRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 2.3.4 - Functional Opaque-Box Integration Tests
 * SUT = BookCacheRepository (Cache-Aside Pattern)
 *
 * Nomenclatura: *IntegrationTest para testes de integração
 */
@ExtendWith(MockitoExtension.class)
class BookCacheRepositoryIntegrationTest {

    @Mock
    private BookRepository cacheRepository; // Redis

    @Mock
    private BookRepository sourceRepository; // SQL

    private BookCacheRepository bookCacheRepository;

    private Book validBook;
    private Genre validGenre;
    private Author validAuthor;
    private List<Author> authors;

    @BeforeEach
    void setUp() {
        bookCacheRepository = new BookCacheRepository(cacheRepository, sourceRepository);

        validGenre = new Genre("Fantasia");
        validGenre.setPk(1L);

        validAuthor = new Author("João Alberto", "Bio", null);
        validAuthor.setAuthorNumber(1L);

        authors = new ArrayList<>();
        authors.add(validAuthor);

        validBook = new Book("9782826012092", "Encantos de contar", "Descrição", validGenre, authors, null);
        validBook.pk = 1L;
    }

    // ==================== FIND BY ISBN - CACHE HIT TESTS ====================

    @Test
    void ensureFindByIsbnReturnsCachedBookWhenCacheHit() {
        // Arrange
        String isbn = "9782826012092";
        when(cacheRepository.findByIsbn(isbn)).thenReturn(Optional.of(validBook));

        // Act
        Optional<Book> result = bookCacheRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validBook, result.get());

        verify(cacheRepository, times(1)).findByIsbn(isbn);
        verify(sourceRepository, never()).findByIsbn(anyString());
        verify(cacheRepository, never()).save(any());
    }

    // ==================== FIND BY ISBN - CACHE MISS TESTS ====================

    @Test
    void ensureFindByIsbnFetchesFromSqlWhenCacheMiss() {
        // Arrange
        String isbn = "9782826012092";
        when(cacheRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        when(sourceRepository.findByIsbn(isbn)).thenReturn(Optional.of(validBook));

        // Act
        Optional<Book> result = bookCacheRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validBook, result.get());

        verify(cacheRepository, times(1)).findByIsbn(isbn);
        verify(sourceRepository, times(1)).findByIsbn(isbn);
        verify(cacheRepository, times(1)).save(validBook);
    }

    @Test
    void ensureFindByIsbnReturnsEmptyWhenNotFoundInSql() {
        // Arrange
        String isbn = "9999999999999";
        when(cacheRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        when(sourceRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        // Act
        Optional<Book> result = bookCacheRepository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());

        verify(cacheRepository, times(1)).findByIsbn(isbn);
        verify(sourceRepository, times(1)).findByIsbn(isbn);
        verify(cacheRepository, never()).save(any());
    }

    @Test
    void ensureFindByIsbnHandlesCacheExceptionGracefully() {
        // Arrange
        String isbn = "9782826012092";
        when(cacheRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        when(sourceRepository.findByIsbn(isbn)).thenReturn(Optional.of(validBook));
        doThrow(new RuntimeException("Redis connection failed")).when(cacheRepository).save(any());

        // Act
        Optional<Book> result = bookCacheRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validBook, result.get());

        verify(sourceRepository, times(1)).findByIsbn(isbn);
        verify(cacheRepository, times(1)).save(validBook);
    }

    // ==================== SAVE - WRITE-THROUGH TESTS ====================

    @Test
    void ensureSaveWritesToSqlFirst() {
        // Arrange
        when(sourceRepository.save(validBook)).thenReturn(validBook);

        // Act
        Book result = bookCacheRepository.save(validBook);

        // Assert
        assertNotNull(result);
        assertEquals(validBook, result);

        verify(sourceRepository, times(1)).save(validBook);
        verify(cacheRepository, times(1)).save(validBook);
    }

    @Test
    void ensureSaveUpdatesCacheAfterSql() {
        // Arrange
        when(sourceRepository.save(validBook)).thenReturn(validBook);

        // Act
        bookCacheRepository.save(validBook);

        // Assert
        var inOrder = inOrder(sourceRepository, cacheRepository);
        inOrder.verify(sourceRepository).save(validBook);
        inOrder.verify(cacheRepository).save(validBook);
    }

    @Test
    void ensureSaveHandlesCacheUpdateFailureGracefully() {
        // Arrange
        when(sourceRepository.save(validBook)).thenReturn(validBook);
        doThrow(new RuntimeException("Cache update failed")).when(cacheRepository).save(validBook);

        // Act
        Book result = bookCacheRepository.save(validBook);

        // Assert
        assertNotNull(result);
        verify(sourceRepository, times(1)).save(validBook);
        verify(cacheRepository, times(1)).save(validBook);
    }

    // ==================== DELETE - CACHE INVALIDATION TESTS ====================

    @Test
    void ensureDeleteRemovesFromSqlFirst() {
        // Act
        bookCacheRepository.delete(validBook);

        // Assert
        verify(sourceRepository, times(1)).delete(validBook);
        verify(cacheRepository, times(1)).delete(validBook);
    }

    @Test
    void ensureDeleteInvalidatesCacheAfterSql() {
        // Act
        bookCacheRepository.delete(validBook);

        // Assert
        var inOrder = inOrder(sourceRepository, cacheRepository);
        inOrder.verify(sourceRepository).delete(validBook);
        inOrder.verify(cacheRepository).delete(validBook);
    }

    // ==================== SEARCH BOOKS - ALWAYS SQL TESTS ====================

    @Test
    void ensureSearchBooksAlwaysGoesToSql() {
        // Arrange
        when(sourceRepository.searchBooks(any(), any())).thenReturn(List.of(validBook));

        // Act
        List<Book> results = bookCacheRepository.searchBooks(null, null);

        // Assert
        assertFalse(results.isEmpty());
        verify(sourceRepository, times(1)).searchBooks(any(), any());
        verify(cacheRepository, never()).searchBooks(any(), any());
    }
}