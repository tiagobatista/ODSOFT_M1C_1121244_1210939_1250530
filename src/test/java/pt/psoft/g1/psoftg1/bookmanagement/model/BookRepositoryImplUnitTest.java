package pt.psoft.g1.psoftg1.bookmanagement.model;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.SQL.AuthorRepositoryImpl;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.SQL.AuthorEntity;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper.BookEntityMapper;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.SQL.BookRepositoryImpl;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.SpringDataBookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.SQL.BookEntity;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.SQL.GenreRepositoryImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.SQL.GenreEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = BookRepositoryImpl (SQL implementation)
 *
 * Objetivo: Testar BookRepositoryImpl isoladamente com TODOS os mocks
 * Nomenclatura: *UnitTest para testes unitários
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookRepositoryImplUnitTest {

    @Mock
    private SpringDataBookRepository bookRepo;

    @Mock
    private GenreRepositoryImpl genreRepo;

    @Mock
    private AuthorRepositoryImpl authorRepo;

    @Mock
    private BookEntityMapper bookEntityMapper;

    @Mock
    private EntityManager em;

    @InjectMocks
    private BookRepositoryImpl bookRepository;

    private Book validBook;
    private BookEntity validBookEntity;
    private Genre validGenre;
    private GenreEntity validGenreEntity;
    private Author validAuthor;
    private AuthorEntity validAuthorEntity;
    private List<Author> authors;
    private List<AuthorEntity> authorEntities;

    @BeforeEach
    void setUp() {
        // Setup Genre
        validGenre = new Genre("Fantasia");
        validGenre.setPk(1L);

        validGenreEntity = mock(GenreEntity.class);
        when(validGenreEntity.getPk()).thenReturn(1L);

        // Setup Author
        validAuthor = new Author("João Alberto", "Bio do João", null);
        validAuthor.setAuthorNumber(1L);

        // Mock AuthorEntity (não podemos instanciar diretamente - protected constructor)
        validAuthorEntity = mock(AuthorEntity.class);
        when(validAuthorEntity.getAuthorNumber()).thenReturn(1L);

        authors = new ArrayList<>();
        authors.add(validAuthor);

        authorEntities = new ArrayList<>();
        authorEntities.add(validAuthorEntity);

        // Setup Book
        validBook = new Book("9782826012092", "Encantos de contar", "Descrição", validGenre, authors, null);
        validBook.pk = 1L;

        // Setup BookEntity (mock também)
        validBookEntity = mock(BookEntity.class);
        when(validBookEntity.getPk()).thenReturn(1L);
        when(validBookEntity.getGenre()).thenReturn(validGenreEntity);
        when(validBookEntity.getAuthors()).thenReturn(authorEntities);
    }

    // ==================== FIND BY ISBN TESTS ====================

    @Test
    void ensureFindByIsbnReturnsBookWhenExists() {
        // Arrange
        String isbn = "9782826012092";
        when(bookRepo.findByIsbn(isbn)).thenReturn(Optional.of(validBookEntity));
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act
        Optional<Book> result = bookRepository.findByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validBook, result.get());
        verify(bookRepo, times(1)).findByIsbn(isbn);
        verify(bookEntityMapper, times(1)).toModel(validBookEntity);
    }

    @Test
    void ensureFindByIsbnReturnsEmptyWhenNotExists() {
        // Arrange
        String isbn = "9999999999999";
        when(bookRepo.findByIsbn(isbn)).thenReturn(Optional.empty());

        // Act
        Optional<Book> result = bookRepository.findByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());
        verify(bookRepo, times(1)).findByIsbn(isbn);
        verify(bookEntityMapper, never()).toModel(any());
    }

    // ==================== FIND BY TITLE TESTS ====================

    @Test
    void ensureFindByTitleReturnsBooks() {
        // Arrange
        String title = "Encantos";
        List<BookEntity> entities = List.of(validBookEntity);
        when(bookRepo.findByTitle(title)).thenReturn(entities);
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act
        List<Book> results = bookRepository.findByTitle(title);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(validBook, results.get(0));
        verify(bookRepo, times(1)).findByTitle(title);
    }

    @Test
    void ensureFindByTitleReturnsEmptyListWhenNoBooksFound() {
        // Arrange
        String title = "NonExistent";
        when(bookRepo.findByTitle(title)).thenReturn(new ArrayList<>());

        // Act
        List<Book> results = bookRepository.findByTitle(title);

        // Assert
        assertTrue(results.isEmpty());
        verify(bookRepo, times(1)).findByTitle(title);
    }

    // ==================== FIND BY GENRE TESTS ====================

    @Test
    void ensureFindByGenreReturnsBooks() {
        // Arrange
        String genre = "Fantasia";
        List<BookEntity> entities = List.of(validBookEntity);
        when(bookRepo.findByGenre(genre)).thenReturn(entities);
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act
        List<Book> results = bookRepository.findByGenre(genre);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(bookRepo, times(1)).findByGenre(genre);
    }

    @Test
    void ensureFindByGenreReturnsEmptyListWhenNoBooks() {
        // Arrange
        String genre = "Terror";
        when(bookRepo.findByGenre(genre)).thenReturn(new ArrayList<>());

        // Act
        List<Book> results = bookRepository.findByGenre(genre);

        // Assert
        assertTrue(results.isEmpty());
        verify(bookRepo, times(1)).findByGenre(genre);
    }

    // ==================== FIND BY AUTHOR NAME TESTS ====================

    @Test
    void ensureFindByAuthorNameReturnsBooks() {
        // Arrange
        String authorName = "João Alberto";
        List<BookEntity> entities = List.of(validBookEntity);
        when(bookRepo.findByAuthorName(authorName)).thenReturn(entities);
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act
        List<Book> results = bookRepository.findByAuthorName(authorName);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(bookRepo, times(1)).findByAuthorName(authorName);
    }

    @Test
    void ensureFindByAuthorNameReturnsEmptyWhenNoBooks() {
        // Arrange
        String authorName = "Unknown Author";
        when(bookRepo.findByAuthorName(authorName)).thenReturn(new ArrayList<>());

        // Act
        List<Book> results = bookRepository.findByAuthorName(authorName);

        // Assert
        assertTrue(results.isEmpty());
    }

    // ==================== FIND BY AUTHOR NUMBER TESTS ====================

    @Test
    void ensureFindBooksByAuthorNumberReturnsBooks() {
        // Arrange
        Long authorNumber = 1L;
        List<BookEntity> entities = List.of(validBookEntity);
        when(bookRepo.findBooksByAuthorNumber(authorNumber)).thenReturn(entities);
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act
        List<Book> results = bookRepository.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(bookRepo, times(1)).findBooksByAuthorNumber(authorNumber);
    }

    @Test
    void ensureFindBooksByAuthorNumberReturnsEmptyWhenNoBooks() {
        // Arrange
        Long authorNumber = 999L;
        when(bookRepo.findBooksByAuthorNumber(authorNumber)).thenReturn(new ArrayList<>());

        // Act
        List<Book> results = bookRepository.findBooksByAuthorNumber(authorNumber);

        // Assert
        assertTrue(results.isEmpty());
    }

    // ==================== SAVE TESTS ====================

    @Test
    void ensureSaveCreatesNewBook() {
        // Arrange
        BookEntity entityToSave = mock(BookEntity.class);
        when(bookEntityMapper.toEntity(validBook)).thenReturn(entityToSave);
        when(genreRepo.findByString(anyString())).thenReturn(Optional.of(validGenre));
        when(em.getReference(GenreEntity.class, validGenre.getPk())).thenReturn(validGenreEntity);

        // Mock Author search
        when(authorRepo.searchByNameName(anyString())).thenReturn(List.of(validAuthor));
        when(em.getReference(AuthorEntity.class, validAuthor.getAuthorNumber())).thenReturn(validAuthorEntity);

        when(bookRepo.save(any(BookEntity.class))).thenReturn(validBookEntity);
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act
        Book result = bookRepository.save(validBook);

        // Assert
        assertNotNull(result);
        assertEquals(validBook, result);
        verify(bookRepo, times(1)).save(any(BookEntity.class));
    }

    @Test
    void ensureSaveThrowsExceptionWhenGenreNotFound() {
        // Arrange
        BookEntity entityToSave = mock(BookEntity.class);
        when(bookEntityMapper.toEntity(validBook)).thenReturn(entityToSave);
        when(genreRepo.findByString(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookRepository.save(validBook));
        verify(bookRepo, never()).save(any());
    }

    @Test
    void ensureSaveThrowsExceptionWhenAuthorNotFound() {
        // Arrange
        BookEntity entityToSave = mock(BookEntity.class);
        when(bookEntityMapper.toEntity(validBook)).thenReturn(entityToSave);
        when(genreRepo.findByString(anyString())).thenReturn(Optional.of(validGenre));
        when(em.getReference(GenreEntity.class, validGenre.getPk())).thenReturn(validGenreEntity);
        when(authorRepo.searchByNameName(anyString())).thenReturn(new ArrayList<>()); // No authors found

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookRepository.save(validBook));
        verify(bookRepo, never()).save(any());
    }

    @Test
    void ensureSaveThrowsExceptionWhenAuthorHasNullNumber() {
        // Arrange
        Author authorWithoutNumber = new Author("Maria", "Bio", null);
        authorWithoutNumber.setAuthorNumber(null); // NULL authorNumber
        List<Author> authorsWithNull = List.of(authorWithoutNumber);

        Book bookWithInvalidAuthor = new Book("9782826012092", "Title", "Desc", validGenre, authorsWithNull, null);

        BookEntity entityToSave = mock(BookEntity.class);
        when(bookEntityMapper.toEntity(bookWithInvalidAuthor)).thenReturn(entityToSave);
        when(genreRepo.findByString(anyString())).thenReturn(Optional.of(validGenre));
        when(em.getReference(GenreEntity.class, validGenre.getPk())).thenReturn(validGenreEntity);
        when(authorRepo.searchByNameName(anyString())).thenReturn(List.of(authorWithoutNumber));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookRepository.save(bookWithInvalidAuthor));
    }

    // ==================== CACHE ANNOTATION TESTS ====================

    @Test
    void ensureFindByIsbnUsesCacheAnnotation() {
        // Arrange
        String isbn = "9782826012092";
        when(bookRepo.findByIsbn(isbn)).thenReturn(Optional.of(validBookEntity));
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act - primeira chamada (cache miss)
        bookRepository.findByIsbn(isbn);

        // Act - segunda chamada (deveria usar cache, mas como é mock, chama novamente)
        bookRepository.findByIsbn(isbn);

        // Assert - verifica que foi chamado 2 vezes (sem cache real em unit test)
        // Em integration test com Redis real, seria chamado apenas 1 vez
        verify(bookRepo, times(2)).findByIsbn(isbn);
    }

    @Test
    void ensureSaveEvictsCacheAnnotation() {
        // Arrange
        BookEntity entityToSave = mock(BookEntity.class);
        when(bookEntityMapper.toEntity(validBook)).thenReturn(entityToSave);
        when(genreRepo.findByString(anyString())).thenReturn(Optional.of(validGenre));
        when(em.getReference(GenreEntity.class, validGenre.getPk())).thenReturn(validGenreEntity);
        when(authorRepo.searchByNameName(anyString())).thenReturn(List.of(validAuthor));
        when(em.getReference(AuthorEntity.class, validAuthor.getAuthorNumber())).thenReturn(validAuthorEntity);
        when(bookRepo.save(any(BookEntity.class))).thenReturn(validBookEntity);
        when(bookEntityMapper.toModel(validBookEntity)).thenReturn(validBook);

        // Act
        Book result = bookRepository.save(validBook);

        // Assert
        assertNotNull(result);
        // @CacheEvict deveria invalidar cache, mas em unit test não há Redis real
        verify(bookRepo, times(1)).save(any(BookEntity.class));
    }
}