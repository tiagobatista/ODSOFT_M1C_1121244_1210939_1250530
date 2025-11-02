package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper.BookRedisMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 2.3.1 - Functional Opaque-Box Tests (Unit Tests)
 * SUT = BookRedisMapper
 *
 * Testa conversão Book ↔ Redis Hash
 */
class BookRedisMapperUnitTest {

    private BookRedisMapper mapper;
    private Book validBook;
    private Genre validGenre;
    private Author validAuthor1;
    private Author validAuthor2;
    private List<Author> authors;

    @BeforeEach
    void setUp() {
        mapper = new BookRedisMapper();

        // Setup Genre
        validGenre = new Genre("Fantasia");
        validGenre.setPk(1L);

        // Setup Authors
        validAuthor1 = new Author("João Alberto", "Bio do João", null);
        validAuthor1.setAuthorNumber(1L);

        validAuthor2 = new Author("Maria Silva", "Bio da Maria", null);
        validAuthor2.setAuthorNumber(2L);

        authors = new ArrayList<>();
        authors.add(validAuthor1);
        authors.add(validAuthor2);

        // Setup Book
        validBook = new Book("9782826012092", "Encantos de contar", "Uma descrição interessante",
                validGenre, authors, null);
        validBook.pk = 10L;
    }

    // ==================== TO REDIS HASH TESTS ====================

    @Test
    void ensureToRedisHashConvertsCompleteBookSuccessfully() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(validBook);

        // Assert
        assertNotNull(hash);
        assertEquals("10", hash.get("pk"));
        assertEquals("9782826012092", hash.get("isbn"));
        assertEquals("Encantos de contar", hash.get("title"));
        assertEquals("Uma descrição interessante", hash.get("description"));
        assertEquals("1", hash.get("genrePk"));
        assertEquals("Fantasia", hash.get("genreName"));
        assertEquals("1,2", hash.get("authorIds"));
    }

    @Test
    void ensureToRedisHashReturnsNullWhenBookIsNull() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(null);

        // Assert
        assertNull(hash);
    }

    @Test
    void ensureToRedisHashHandlesBookWithoutPk() {
        // Arrange
        Book bookWithoutPk = new Book("9782826012092", "Title", "Desc", validGenre, authors, null);
        bookWithoutPk.pk = null;

        // Act
        Map<String, String> hash = mapper.toRedisHash(bookWithoutPk);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("pk"));
        assertEquals("9782826012092", hash.get("isbn"));
        assertEquals("Title", hash.get("title"));
    }

    @Test
    void ensureToRedisHashHandlesBookWithNullDescription() {
        // Arrange
        Book bookWithoutDesc = new Book("9782826012092", "Title", null, validGenre, authors, null);
        bookWithoutDesc.pk = 10L;

        // Act
        Map<String, String> hash = mapper.toRedisHash(bookWithoutDesc);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("description"));
        assertEquals("Title", hash.get("title"));
    }

    @Test
    void ensureToRedisHashHandlesBookWithPhoto() {
        // Arrange
        Book bookWithPhoto = new Book("9782826012092", "Title", "Desc", validGenre, authors, "book-cover.jpg");
        bookWithPhoto.pk = 10L;

        // Act
        Map<String, String> hash = mapper.toRedisHash(bookWithPhoto);

        // Assert
        assertNotNull(hash);
        assertEquals("book-cover.jpg", hash.get("photo"));
    }

    @Test
    void ensureToRedisHashHandlesBookWithoutPhoto() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(validBook);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("photo"));
    }

    @Test
    void ensureToRedisHashHandlesSingleAuthor() {
        // Arrange
        List<Author> singleAuthor = List.of(validAuthor1);
        Book bookWithOneAuthor = new Book("9782826012092", "Title", "Desc", validGenre, singleAuthor, null);
        bookWithOneAuthor.pk = 10L;

        // Act
        Map<String, String> hash = mapper.toRedisHash(bookWithOneAuthor);

        // Assert
        assertNotNull(hash);
        assertEquals("1", hash.get("authorIds"));
    }

    @Test
    void ensureToRedisHashHandlesMultipleAuthors() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(validBook);

        // Assert
        assertNotNull(hash);
        assertEquals("1,2", hash.get("authorIds"));
        String authorIds = hash.get("authorIds");
        assertTrue(authorIds.contains("1"));
        assertTrue(authorIds.contains("2"));
        assertTrue(authorIds.contains(","));
    }

    @Test
    void ensureToRedisHashFiltersAuthorsWithoutAuthorNumber() {
        // Arrange
        Author authorWithoutNumber = new Author("Unknown", "Bio", null);
        // authorNumber permanece null

        List<Author> mixedAuthors = new ArrayList<>();
        mixedAuthors.add(validAuthor1);
        mixedAuthors.add(authorWithoutNumber); // Este deve ser filtrado
        mixedAuthors.add(validAuthor2);

        Book bookWithMixedAuthors = new Book("9782826012092", "Title", "Desc", validGenre, mixedAuthors, null);
        bookWithMixedAuthors.pk = 10L;

        // Act
        Map<String, String> hash = mapper.toRedisHash(bookWithMixedAuthors);

        // Assert
        assertNotNull(hash);
        assertEquals("1,2", hash.get("authorIds")); // Author sem número foi filtrado
    }

    @Test
    void ensureToRedisHashHandlesGenreWithoutPk() {
        // Arrange
        Genre genreWithoutPk = new Genre("Terror");
        genreWithoutPk.setPk(null);

        Book bookWithInvalidGenre = new Book("9782826012092", "Title", "Desc", genreWithoutPk, authors, null);
        bookWithInvalidGenre.pk = 10L;

        // Act
        Map<String, String> hash = mapper.toRedisHash(bookWithInvalidGenre);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.containsKey("genrePk"));
    }

    // ==================== FROM REDIS HASH TESTS ====================

    @Test
    void ensureFromRedisHashConvertsCompleteHashSuccessfully() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "10");
        hash.put("isbn", "9782826012092");
        hash.put("title", "Encantos de contar");
        hash.put("description", "Uma descrição");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "1,2");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(book);
        assertEquals(10L, book.getPk());
        assertEquals("9782826012092", book.getIsbn().toString());
        assertEquals("Encantos de contar", book.getTitle().toString());
        assertEquals("Uma descrição", book.getDescription().toString());

        assertNotNull(book.getGenre());
        assertEquals(1L, book.getGenre().getPk());
        assertEquals("Fantasia", book.getGenre().getGenre());

        assertNotNull(book.getAuthors());
        assertEquals(2, book.getAuthors().size());
        assertEquals(1L, book.getAuthors().get(0).getAuthorNumber());
        assertEquals(2L, book.getAuthors().get(1).getAuthorNumber());
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenHashIsNull() {
        // Act
        Book book = mapper.fromRedisHash(null);

        // Assert
        assertNull(book);
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenHashIsEmpty() {
        // Arrange
        Map<Object, Object> emptyHash = new HashMap<>();

        // Act
        Book book = mapper.fromRedisHash(emptyHash);

        // Assert
        assertNull(book);
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenIsbnIsMissing() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        // isbn missing
        hash.put("title", "Title");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "1");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNull(book); // Dados incompletos
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenTitleIsMissing() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        // title missing
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "1");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNull(book); // Dados incompletos
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenGenreIsMissing() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        // genre missing
        hash.put("authorIds", "1");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNull(book); // Dados incompletos
    }

    @Test
    void ensureFromRedisHashReturnsNullWhenAuthorsAreMissing() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        // authorIds missing

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNull(book); // Dados incompletos
    }

    @Test
    void ensureFromRedisHashHandlesHashWithoutDescription() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("pk", "10");
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        // description missing (opcional)
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "1");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(book);
        assertNull(book.getDescription()); // Description é opcional
    }

    @Test
    void ensureFromRedisHashHandlesSingleAuthor() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "5"); // Um único autor

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(book);
        assertEquals(1, book.getAuthors().size());
        assertEquals(5L, book.getAuthors().get(0).getAuthorNumber());
    }

    @Test
    void ensureFromRedisHashHandlesMultipleAuthors() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "1,2,3");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(book);
        assertEquals(3, book.getAuthors().size());
        assertEquals(1L, book.getAuthors().get(0).getAuthorNumber());
        assertEquals(2L, book.getAuthors().get(1).getAuthorNumber());
        assertEquals(3L, book.getAuthors().get(2).getAuthorNumber());
    }

    @Test
    void ensureFromRedisHashHandlesAuthorIdsWithSpaces() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", " 1 , 2 , 3 "); // Com espaços

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(book);
        assertEquals(3, book.getAuthors().size());
    }

    @Test
    void ensureFromRedisHashHandlesInvalidAuthorId() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "invalid"); // ID inválido

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNull(book); // Erro no parsing
    }

    @Test
    void ensureFromRedisHashHandlesInvalidGenrePk() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        hash.put("genrePk", "invalid"); // PK inválido
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "1");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNull(book); // Erro no parsing
    }

    @Test
    void ensureFromRedisHashHandlesPhoto() {
        // Arrange
        Map<Object, Object> hash = new HashMap<>();
        hash.put("isbn", "9782826012092");
        hash.put("title", "Title");
        hash.put("genrePk", "1");
        hash.put("genreName", "Fantasia");
        hash.put("authorIds", "1");
        hash.put("photo", "book-cover.jpg");

        // Act
        Book book = mapper.fromRedisHash(hash);

        // Assert
        assertNotNull(book);
        assertNotNull(book.getPhoto());
        assertEquals("book-cover.jpg", book.getPhoto().getPhotoFile());
    }

    // ==================== ROUND TRIP TESTS ====================

    @Test
    void ensureRoundTripConversionPreservesData() {
        // Act
        Map<String, String> hash = mapper.toRedisHash(validBook);

        // Convert to Object map for fromRedisHash
        Map<Object, Object> objectHash = new HashMap<>(hash);
        Book reconstructedBook = mapper.fromRedisHash(objectHash);

        // Assert
        assertNotNull(reconstructedBook);
        assertEquals(validBook.getPk(), reconstructedBook.getPk());
        assertEquals(validBook.getIsbn().toString(), reconstructedBook.getIsbn().toString());
        assertEquals(validBook.getTitle().toString(), reconstructedBook.getTitle().toString());
        assertEquals(validBook.getDescription().toString(), reconstructedBook.getDescription().toString());
        assertEquals(validBook.getGenre().getPk(), reconstructedBook.getGenre().getPk());
        assertEquals(validBook.getAuthors().size(), reconstructedBook.getAuthors().size());
    }

    // ==================== KEY GENERATION TESTS ====================

    @Test
    void ensureGenerateKeyByIsbnCreatesCorrectKey() {
        // Act
        String key = mapper.generateKeyByIsbn("9782826012092");

        // Assert
        assertEquals("book:isbn:9782826012092", key);
    }

    @Test
    void ensureGenerateKeyByIsbnConvertsToLowercase() {
        // Act
        String key = mapper.generateKeyByIsbn("ISBN123ABC");

        // Assert
        assertEquals("book:isbn:isbn123abc", key);
    }

    @Test
    void ensureGenerateKeyByTitleCreatesCorrectKey() {
        // Act
        String key = mapper.generateKeyByTitle("Encantos de contar");

        // Assert
        assertEquals("book:title:encantos de contar", key);
    }

    @Test
    void ensureGenerateKeyByTitleConvertsToLowercase() {
        // Act
        String key = mapper.generateKeyByTitle("TITLE IN CAPS");

        // Assert
        assertEquals("book:title:title in caps", key);
    }

    @Test
    void ensureGenerateKeyByTitleHandlesSpecialCharacters() {
        // Act
        String key = mapper.generateKeyByTitle("Title: With! Special? Chars.");

        // Assert
        assertEquals("book:title:title: with! special? chars.", key);
    }
}