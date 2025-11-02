package pt.psoft.g1.psoftg1.bookmanagement.model.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.api.*;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.*;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E Integration Test for BookController
 *
 * Purpose: Test COMPLETE SYSTEM flow from HTTP request to response
 * Testing Strategy: Use MockMvc to simulate HTTP requests, verify full stack behavior
 * SUT: BookController + BookService + Domain + Mappers (complete system)
 * Type: 2.3.5 - Functional opaque-box with SUT = system
 *
 * Test Coverage:
 * - All HTTP endpoints (PUT, PATCH, GET, DELETE, POST)
 * - Request validation and response formatting
 * - Security (authentication, CSRF)
 * - HTTP status codes (200, 201, 400, 403, 404, 409, etc.)
 * - Headers (ETag, Location, Content-Type)
 * - Error responses
 * - Complete request/response cycles
 * - Photo management endpoints
 * - Search and filtering endpoints
 *
 * @author ARQSOFT 2025-2026
 */
@WebMvcTest(BookController.class)
@WithMockUser(username = "testuser", roles = {"USER"})
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private LendingService lendingService;

    @MockBean
    private ConcurrencyService concurrencyService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private UserService userService;

    @MockBean
    private ReaderService readerService;

    @MockBean
    private BookViewMapper bookViewMapper;

    @MockBean
    private GenreRepository genreRepository;

    @MockBean
    private AuthorRepository authorRepository;

    private Book testBook;
    private BookView testBookView;
    private Genre testGenre;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        // Setup test data
        testGenre = new Genre(1L, "Fiction");

        testAuthor = new Author("John Doe", "Famous author", null);
        testAuthor.setAuthorNumber(1L);

        testBook = new Book(
                "9780306406157",
                "Test Book Title",
                "Test book description",
                testGenre,
                Arrays.asList(testAuthor),
                null
        );
        testBook.pk = 1L;

        testBookView = new BookView();
        testBookView.setIsbn("9780306406157");
        testBookView.setTitle("Test Book Title");
        testBookView.setDescription("Test book description");
        testBookView.setGenre("Fiction");
        testBookView.setAuthors(Arrays.asList("John Doe"));
    }

    // ========================================
    // CREATE BOOK - PUT /api/books/{isbn}
    // ========================================

    @Test
    void testCreate_withValidData_shouldReturn201WithLocation() throws Exception {
        // Arrange
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        request.setDescription("New Description");
        request.setGenre("Fiction");
        request.setAuthors(Arrays.asList(1L));

        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(bookService.create(any(CreateBookRequest.class), eq("9780306406157")))
                .thenReturn(testBook);
        when(bookViewMapper.toBookView(testBook)).thenReturn(testBookView);

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/books/9780306406157")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("ETag", "\"0\""))
                .andExpect(jsonPath("$.isbn", is("9780306406157")))
                .andExpect(jsonPath("$.title", is("Test Book Title")));

        verify(bookService).create(any(CreateBookRequest.class), eq("9780306406157"));
        verify(bookViewMapper).toBookView(testBook);
    }

    @Test
    void testCreate_withoutCsrf_shouldReturn403() throws Exception {
        // Arrange
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/books/9780306406157")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verify(bookService, never()).create(any(), anyString());
    }

    @Test
    void testCreate_withDuplicateIsbn_shouldReturn400() throws Exception {
        // Arrange
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Duplicate Book");
        request.setGenre("Fiction");
        request.setAuthors(Arrays.asList(1L));

        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(bookService.create(any(), anyString()))
                .thenThrow(new ConflictException("Book already exists"));

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/books/9780306406157")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreate_withPhoto_shouldProcessAndCreate() throws Exception {
        // Arrange
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Book with Photo");
        request.setGenre("Fiction");
        request.setAuthors(Arrays.asList(1L));

        when(fileStorageService.getRequestPhoto(any(MultipartFile.class)))
                .thenReturn("book-photo.jpg");
        when(bookService.create(any(), eq("9780306406157"))).thenReturn(testBook);
        when(bookViewMapper.toBookView(testBook)).thenReturn(testBookView);

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/books/9780306406157")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        verify(fileStorageService).getRequestPhoto(any());
    }

    // ========================================
    // FIND BY ISBN - GET /api/books/{isbn}
    // ========================================

    @Test
    void testFindByIsbn_whenExists_shouldReturn200WithETag() throws Exception {
        // Arrange
        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);
        when(bookViewMapper.toBookView(testBook)).thenReturn(testBookView);

        // Act & Assert
        mockMvc.perform(get("/api/books/9780306406157")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"0\""))
                .andExpect(jsonPath("$.isbn", is("9780306406157")))
                .andExpect(jsonPath("$.title", is("Test Book Title")));

        verify(bookService).findByIsbn("9780306406157");
    }

    @Test
    void testFindByIsbn_whenNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(bookService.findByIsbn("9999999999999"))
                .thenThrow(new NotFoundException(Book.class, "9999999999999"));

        // Act & Assert
        mockMvc.perform(get("/api/books/9999999999999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // UPDATE BOOK - PATCH /api/books/{isbn}
    // ========================================

    @Test
    void testUpdateBook_withValidData_shouldReturn200WithETag() throws Exception {
        // Arrange
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated Title");

        when(concurrencyService.getVersionFromIfMatchHeader("0")).thenReturn(0L);
        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(bookService.update(any(UpdateBookRequest.class), eq("0")))
                .thenReturn(testBook);
        when(bookViewMapper.toBookView(testBook)).thenReturn(testBookView);

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/books/9780306406157")
                        .with(csrf())
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"0\""))
                .andExpect(jsonPath("$.title", is("Test Book Title")));

        verify(concurrencyService).getVersionFromIfMatchHeader("0");
        verify(bookService).update(any(UpdateBookRequest.class), eq("0"));
    }

    @Test
    void testUpdateBook_withoutIfMatch_shouldReturn400() throws Exception {
        // Arrange
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated");

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/books/9780306406157")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).update(any(), anyString());
    }

    @Test
    void testUpdateBook_withNullIfMatch_shouldReturn400() throws Exception {
        // Arrange
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated");

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/books/9780306406157")
                        .with(csrf())
                        .header("If-Match", "null")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBook_withEmptyIfMatch_shouldReturn400() throws Exception {
        // Arrange
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated");

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/books/9780306406157")
                        .with(csrf())
                        .header("If-Match", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBook_withConflict_shouldReturn409() throws Exception {
        // Arrange
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated");

        when(concurrencyService.getVersionFromIfMatchHeader("0")).thenReturn(0L);
        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(bookService.update(any(), anyString()))
                .thenThrow(new ConflictException("Version conflict"));

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/books/9780306406157")
                        .with(csrf())
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    // ========================================
    // FIND BOOKS - GET /api/books
    // ========================================

    @Test
    void testFindBooks_byTitle_shouldReturnList() throws Exception {
        // Arrange
        List<Book> books = Arrays.asList(testBook);
        List<BookView> bookViews = Arrays.asList(testBookView);

        when(bookService.findByTitle("Test")).thenReturn(books);
        when(bookViewMapper.toBookView(books)).thenReturn(bookViews);

        // Act & Assert
        mockMvc.perform(get("/api/books")
                        .with(csrf())
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title", is("Test Book Title")));

        verify(bookService).findByTitle("Test");
    }

    @Test
    void testFindBooks_byGenre_shouldReturnList() throws Exception {
        // Arrange
        List<Book> books = Arrays.asList(testBook);
        List<BookView> bookViews = Arrays.asList(testBookView);

        when(bookService.findByGenre("Fiction")).thenReturn(books);
        when(bookViewMapper.toBookView(books)).thenReturn(bookViews);

        // Act & Assert
        mockMvc.perform(get("/api/books")
                        .with(csrf())
                        .param("genre", "Fiction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].genre", is("Fiction")));

        verify(bookService).findByGenre("Fiction");
    }

    @Test
    void testFindBooks_byAuthorName_shouldReturnList() throws Exception {
        // Arrange
        List<Book> books = Arrays.asList(testBook);
        List<BookView> bookViews = Arrays.asList(testBookView);

        when(bookService.findByAuthorName("John")).thenReturn(books);
        when(bookViewMapper.toBookView(books)).thenReturn(bookViews);

        // Act & Assert
        mockMvc.perform(get("/api/books")
                        .with(csrf())
                        .param("authorName", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(bookService).findByAuthorName("John");
    }

    @Test
    void testFindBooks_multipleParams_shouldCombineResults() throws Exception {
        // Arrange
        Book book2 = new Book("9780471958697", "Another Book", "Description", testGenre, Arrays.asList(testAuthor), null);
        List<Book> booksByTitle = Arrays.asList(testBook);
        List<Book> booksByGenre = Arrays.asList(testBook, book2);

        BookView bookView2 = new BookView();
        bookView2.setIsbn("9780471958697");
        bookView2.setTitle("Another Book");

        when(bookService.findByTitle("Test")).thenReturn(booksByTitle);
        when(bookService.findByGenre("Fiction")).thenReturn(booksByGenre);
        when(bookViewMapper.toBookView(anyList())).thenReturn(Arrays.asList(testBookView, bookView2));

        // Act & Assert
        mockMvc.perform(get("/api/books")
                        .with(csrf())
                        .param("title", "Test")
                        .param("genre", "Fiction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void testFindBooks_noMatches_shouldReturn404() throws Exception {
        // Arrange
        when(bookService.findByTitle("NonExistent")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/books")
                        .with(csrf())
                        .param("title", "NonExistent"))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // TOP 5 BOOKS - GET /api/books/top5
    // ========================================

    @Test
    void testGetTop5BooksLent_shouldReturnList() throws Exception {
        // Arrange
        BookCountView view1 = new BookCountView();
        BookCountView view2 = new BookCountView();

        BookCountDTO dto1 = mock(BookCountDTO.class);
        BookCountDTO dto2 = mock(BookCountDTO.class);

        when(bookService.findTop5BooksLent()).thenReturn(Arrays.asList(dto1, dto2));
        when(bookViewMapper.toBookCountView(anyList())).thenReturn(Arrays.asList(view1, view2));

        // Act & Assert
        mockMvc.perform(get("/api/books/top5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));

        verify(bookService).findTop5BooksLent();
    }

    @Test
    void testGetTop5BooksLent_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(bookService.findTop5BooksLent()).thenReturn(Collections.emptyList());
        when(bookViewMapper.toBookCountView(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/books/top5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    // ========================================
    // BOOK PHOTO - GET /api/books/{isbn}/photo
    // ========================================

    @Test
    void testGetBookPhoto_whenPhotoExists_shouldReturnImage() throws Exception {
        // Arrange
        testBook.setPhoto("book-photo.jpg");
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};

        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);
        when(fileStorageService.getFile("book-photo.jpg")).thenReturn(imageBytes);
        when(fileStorageService.getExtension("book-photo.jpg")).thenReturn(Optional.of("jpeg"));

        // Act & Assert
        mockMvc.perform(get("/api/books/9780306406157/photo")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageBytes));

        verify(fileStorageService).getFile("book-photo.jpg");
    }

    @Test
    void testGetBookPhoto_whenNoPhoto_shouldReturn200WithEmptyBody() throws Exception {
        // Arrange
        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(get("/api/books/9780306406157/photo")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0]));

        verify(fileStorageService, never()).getFile(anyString());
    }

    @Test
    void testGetBookPhoto_whenBookNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(bookService.findByIsbn("9999999999999"))
                .thenThrow(new NotFoundException(Book.class, "9999999999999"));

        // Act & Assert
        mockMvc.perform(get("/api/books/9999999999999/photo")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBookPhoto_withPngExtension_shouldReturnPngContentType() throws Exception {
        // Arrange
        testBook.setPhoto("book-photo.png");

        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);
        when(fileStorageService.getFile("book-photo.png")).thenReturn(new byte[]{1, 2, 3});
        when(fileStorageService.getExtension("book-photo.png")).thenReturn(Optional.of("png"));

        // Act & Assert
        mockMvc.perform(get("/api/books/9780306406157/photo")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    // ========================================
    // DELETE BOOK PHOTO - DELETE /api/books/{isbn}/photo
    // ========================================

    @Test
    void testDeleteBookPhoto_whenPhotoExists_shouldReturn200() throws Exception {
        // Arrange
        testBook.setPhoto("book-photo.jpg");

        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);
        doNothing().when(fileStorageService).deleteFile("book-photo.jpg");
        when(bookService.removeBookPhoto("9780306406157", 0L)).thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(delete("/api/books/9780306406157/photo")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(fileStorageService).deleteFile("book-photo.jpg");
        verify(bookService).removeBookPhoto("9780306406157", 0L);
    }

    @Test
    void testDeleteBookPhoto_whenNoPhoto_shouldReturn404() throws Exception {
        // Arrange
        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(delete("/api/books/9780306406157/photo")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(bookService, never()).removeBookPhoto(anyString(), anyLong());
    }

    // ========================================
    // GET AVERAGE LENDING DURATION
    // ========================================

    @Test
    void testGetAvgLendingDuration_shouldReturnDuration() throws Exception {
        // Arrange
        Double avgDuration = 14.5;
        BookAverageLendingDurationView view = new BookAverageLendingDurationView();

        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);
        when(lendingService.getAvgLendingDurationByIsbn("9780306406157")).thenReturn(avgDuration);
        when(bookViewMapper.toBookAverageLendingDurationView(testBook, avgDuration)).thenReturn(view);

        // Act & Assert
        mockMvc.perform(get("/api/books/9780306406157/avgDuration")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(lendingService).getAvgLendingDurationByIsbn("9780306406157");
    }

    // ========================================
    // BOOK SUGGESTIONS
    // ========================================

    @Test
    void testGetBooksSuggestions_shouldReturnSuggestions() throws Exception {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getUsername()).thenReturn("testuser");

        ReaderDetails mockReader = mock(ReaderDetails.class);
        when(mockReader.getReaderNumber()).thenReturn("R001");

        when(userService.getAuthenticatedUser(any())).thenReturn(mockUser);
        when(readerService.findByUsername("testuser")).thenReturn(Optional.of(mockReader));
        when(bookService.getBooksSuggestionsForReader("R001")).thenReturn(Arrays.asList(testBook));
        when(bookViewMapper.toBookView(anyList())).thenReturn(Arrays.asList(testBookView));

        // Act & Assert
        mockMvc.perform(get("/api/books/suggestions")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(bookService).getBooksSuggestionsForReader("R001");
    }

    // ========================================
    // SEARCH BOOKS - POST /api/books/search
    // ========================================

    @Test
    void testSearchBooks_withQuery_shouldReturnResults() throws Exception {
        // Arrange
        SearchBooksQuery query = new SearchBooksQuery("Test", "Fiction", "John");
        when(bookService.searchBooks(any(), eq(query))).thenReturn(Arrays.asList(testBook));
        when(bookViewMapper.toBookView(anyList())).thenReturn(Arrays.asList(testBookView));

        String requestBody = "{\"query\": {\"title\": \"Test\", \"genre\": \"Fiction\", \"authorName\": \"John\"}}";

        // Act & Assert
        mockMvc.perform(post("/api/books/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    // ========================================
    // COMPLETE INTEGRATION SCENARIOS
    // ========================================

    @Test
    void testCompleteFlow_createFindUpdateDelete() throws Exception {
        // 1. Create
        CreateBookRequest createRequest = new CreateBookRequest();
        createRequest.setTitle("New Book");
        createRequest.setGenre("Fiction");
        createRequest.setAuthors(Arrays.asList(1L));

        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(bookService.create(any(), eq("9780306406157"))).thenReturn(testBook);
        when(bookViewMapper.toBookView(testBook)).thenReturn(testBookView);

        mockMvc.perform(put("/api/books/9780306406157")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // 2. Find
        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);

        mockMvc.perform(get("/api/books/9780306406157")
                        .with(csrf()))
                .andExpect(status().isOk());

        // 3. Update
        UpdateBookRequest updateRequest = new UpdateBookRequest();
        updateRequest.setTitle("Updated Title");

        when(concurrencyService.getVersionFromIfMatchHeader("0")).thenReturn(0L);
        when(bookService.update(any(), eq("0"))).thenReturn(testBook);

        mockMvc.perform(patch("/api/books/9780306406157")
                        .with(csrf())
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Verify all operations
        verify(bookService).create(any(), eq("9780306406157"));
        verify(bookService, atLeast(1)).findByIsbn("9780306406157");
        verify(bookService).update(any(), eq("0"));
    }

    @Test
    void testPhotoManagement_uploadAndDelete() throws Exception {
        // 1. Create book with photo
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Book with Photo");
        request.setGenre("Fiction");
        request.setAuthors(Arrays.asList(1L));

        testBook.setPhoto("photo.jpg");

        when(fileStorageService.getRequestPhoto(any())).thenReturn("photo.jpg");
        when(bookService.create(any(), eq("9780306406157"))).thenReturn(testBook);
        when(bookViewMapper.toBookView(testBook)).thenReturn(testBookView);

        mockMvc.perform(put("/api/books/9780306406157")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 2. Delete photo
        when(bookService.findByIsbn("9780306406157")).thenReturn(testBook);
        doNothing().when(fileStorageService).deleteFile("photo.jpg");

        mockMvc.perform(delete("/api/books/9780306406157/photo")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(fileStorageService).deleteFile("photo.jpg");
    }
}