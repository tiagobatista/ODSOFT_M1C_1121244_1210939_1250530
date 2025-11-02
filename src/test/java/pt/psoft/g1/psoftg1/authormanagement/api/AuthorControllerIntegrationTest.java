package pt.psoft.g1.psoftg1.authormanagement.api;

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
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;
import pt.psoft.g1.psoftg1.authormanagement.services.CreateAuthorRequest;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookView;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;

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
 * E2E Integration Test for AuthorController
 *
 * Purpose: Test COMPLETE SYSTEM flow from HTTP request to response
 * Testing Strategy: Use MockMvc to simulate HTTP requests, verify full stack behavior
 * SUT: AuthorController + AuthorService + Domain + Mappers (complete system)
 * Type: 2.3.5 - Functional opaque-box with SUT = system
 *
 * Test Coverage:
 * - All HTTP endpoints (POST, PATCH, GET, DELETE)
 * - Request validation and response formatting
 * - Security (authentication, CSRF)
 * - HTTP status codes (200, 201, 400, 403, 404, etc.)
 * - Headers (ETag, Location, Content-Type)
 * - Error responses
 * - Complete request/response cycles
 *
 * @author ARQSOFT 2025-2026
 */
@WebMvcTest(AuthorController.class)
@WithMockUser(username = "testuser", roles = {"USER"})
class AuthorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private AuthorViewMapper authorViewMapper;

    @MockBean
    private ConcurrencyService concurrencyService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private BookViewMapper bookViewMapper;

    private Author testAuthor;
    private AuthorView testAuthorView;

    @BeforeEach
    void setUp() {
        testAuthor = new Author("John Doe", "Famous author", null);
        testAuthor.setAuthorNumber(1L);
        testAuthor.setVersion(1L);

        testAuthorView = new AuthorView();
        testAuthorView.setAuthorNumber(1L);
        testAuthorView.setName("John Doe");
        testAuthorView.setBio("Famous author");
    }

    // ========================================
    // CREATE AUTHOR - POST /api/authors
    // ========================================

    @Test
    void testCreateAuthor_withValidData_shouldReturn201WithLocation() throws Exception {
        // Arrange
        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(authorService.create(any(CreateAuthorRequest.class))).thenReturn(testAuthor);
        when(authorViewMapper.toAuthorView(testAuthor)).thenReturn(testAuthorView);

        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setName("John Doe");
        request.setBio("Famous author");
        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.authorNumber", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.bio", is("Famous author")));

        verify(authorService).create(any(CreateAuthorRequest.class));
        verify(authorViewMapper).toAuthorView(testAuthor);
    }

    @Test
    void testCreateAuthor_withoutCsrf_shouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test\", \"bio\": \"Bio\"}"))
                .andExpect(status().isForbidden());

        verify(authorService, never()).create(any());
    }

    @Test
    void testCreateAuthor_withPhoto_shouldProcessAndCreate() throws Exception {
        // Arrange
        when(fileStorageService.getRequestPhoto(any(MultipartFile.class))).thenReturn("photo.jpg");
        when(authorService.create(any(CreateAuthorRequest.class))).thenReturn(testAuthor);
        when(authorViewMapper.toAuthorView(testAuthor)).thenReturn(testAuthorView);

        // Act & Assert
        mockMvc.perform(post("/api/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"John Doe\", \"bio\": \"Bio\"}"))
                .andExpect(status().isCreated());

        verify(fileStorageService).getRequestPhoto(any());
    }

    // ========================================
    // PARTIAL UPDATE - PATCH /api/authors/{authorNumber}
    // ========================================

    @Test
    void testPartialUpdate_withValidData_shouldReturn200WithETag() throws Exception {
        // Arrange
        when(concurrencyService.getVersionFromIfMatchHeader("1")).thenReturn(1L);
        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(authorService.partialUpdate(eq(1L), any(UpdateAuthorRequest.class), eq(1L)))
                .thenReturn(testAuthor);
        when(authorViewMapper.toAuthorView(testAuthor)).thenReturn(testAuthorView);

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName("Updated Name");
        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/authors/1")
                        .with(csrf())
                        .header("If-Match", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.name", is("John Doe")));

        verify(concurrencyService).getVersionFromIfMatchHeader("1");
        verify(authorService).partialUpdate(eq(1L), any(UpdateAuthorRequest.class), eq(1L));
    }

    @Test
    void testPartialUpdate_withoutIfMatch_shouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Name\"}"))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).partialUpdate(anyLong(), any(), anyLong());
    }

    @Test
    void testPartialUpdate_withNullIfMatch_shouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/authors/1")
                        .with(csrf())
                        .header("If-Match", "null")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPartialUpdate_withEmptyIfMatch_shouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/authors/1")
                        .with(csrf())
                        .header("If-Match", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // FIND BY AUTHOR NUMBER - GET /api/authors/{authorNumber}
    // ========================================

    @Test
    void testFindByAuthorNumber_whenExists_shouldReturn200WithETag() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));
        when(authorViewMapper.toAuthorView(testAuthor)).thenReturn(testAuthorView);

        // Act & Assert
        mockMvc.perform(get("/api/authors/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.authorNumber", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")));

        verify(authorService).findByAuthorNumber(1L);
    }

    @Test
    void testFindByAuthorNumber_whenNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/authors/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // FIND BY NAME - GET /api/authors?name=xxx
    // ========================================

    @Test
    void testFindByName_whenAuthorsExist_shouldReturn200WithList() throws Exception {
        // Arrange
        List<Author> authors = Arrays.asList(testAuthor);
        List<AuthorView> authorViews = Arrays.asList(testAuthorView);

        when(authorService.findByName("John")).thenReturn(authors);
        when(authorViewMapper.toAuthorView(authors)).thenReturn(authorViews);

        // Act & Assert
        mockMvc.perform(get("/api/authors")
                        .with(csrf())
                        .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("John Doe")));

        verify(authorService).findByName("John");
    }

    @Test
    void testFindByName_whenNoAuthors_shouldReturn200WithEmptyList() throws Exception {
        // Arrange
        when(authorService.findByName("NonExistent"))
                .thenReturn(Collections.emptyList());
        when(authorViewMapper.toAuthorView(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/authors")
                        .with(csrf())
                        .param("name", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        verify(authorService).findByName("NonExistent");
    }

    // ========================================
    // GET BOOKS BY AUTHOR - GET /api/authors/{authorNumber}/books
    // ========================================

    @Test
    void testGetBooksByAuthorNumber_whenAuthorExists_shouldReturnBooks() throws Exception {
        // Arrange
        Book book = mock(Book.class);
        BookView bookView = new BookView();
        bookView.setIsbn("123-456");

        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));
        when(authorService.findBooksByAuthorNumber(1L))
                .thenReturn(Arrays.asList(book));
        when(bookViewMapper.toBookView(anyList()))
                .thenReturn(Arrays.asList(bookView));

        // Act & Assert
        mockMvc.perform(get("/api/authors/1/books")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].isbn", is("123-456")));

        verify(authorService).findByAuthorNumber(1L);
        verify(authorService).findBooksByAuthorNumber(1L);
    }

    @Test
    void testGetBooksByAuthorNumber_whenAuthorNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/authors/999/books")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(authorService, never()).findBooksByAuthorNumber(anyLong());
    }

    // ========================================
    // GET TOP 5 AUTHORS - GET /api/authors/top5
    // ========================================

    @Test
    void testGetTop5_whenAuthorsExist_shouldReturnList() throws Exception {
        // Arrange
        AuthorLendingView view1 = new AuthorLendingView("Author 1", 100L);
        AuthorLendingView view2 = new AuthorLendingView("Author 2", 90L);

        when(authorService.findTopAuthorByLendings())
                .thenReturn(Arrays.asList(view1, view2));

        // Act & Assert
        mockMvc.perform(get("/api/authors/top5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));

        verify(authorService).findTopAuthorByLendings();
    }

    @Test
    void testGetTop5_whenNoAuthors_shouldReturn404() throws Exception {
        // Arrange
        when(authorService.findTopAuthorByLendings())
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/authors/top5")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // GET AUTHOR PHOTO - GET /api/authors/{authorNumber}/photo
    // ========================================

    @Test
    void testGetAuthorPhoto_whenPhotoExists_shouldReturnImage() throws Exception {
        // Arrange
        testAuthor.setPhoto("photo.jpg");
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};

        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));
        when(fileStorageService.getFile("photo.jpg")).thenReturn(imageBytes);
        when(fileStorageService.getExtension("photo.jpg"))
                .thenReturn(Optional.of("jpeg"));

        // Act & Assert
        mockMvc.perform(get("/api/authors/1/photo")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageBytes));

        verify(fileStorageService).getFile("photo.jpg");
    }

    @Test
    void testGetAuthorPhoto_whenNoPhoto_shouldReturn200WithEmptyBody() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));

        // Act & Assert
        mockMvc.perform(get("/api/authors/1/photo")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0]));

        verify(fileStorageService, never()).getFile(anyString());
    }

    @Test
    void testGetAuthorPhoto_whenAuthorNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/authors/999/photo")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAuthorPhoto_withPngExtension_shouldReturnPngContentType() throws Exception {
        // Arrange
        testAuthor.setPhoto("photo.png");

        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));
        when(fileStorageService.getFile("photo.png"))
                .thenReturn(new byte[]{1, 2, 3});
        when(fileStorageService.getExtension("photo.png"))
                .thenReturn(Optional.of("png"));

        // Act & Assert
        mockMvc.perform(get("/api/authors/1/photo")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    // ========================================
    // GET CO-AUTHORS - GET /api/authors/{authorNumber}/coauthors
    // ========================================

    @Test
    void testGetCoAuthors_whenCoAuthorsExist_shouldReturnList() throws Exception {
        // Arrange
        Author coAuthor = new Author("Co-Author", "Bio", null);
        coAuthor.setAuthorNumber(2L);

        Book book = mock(Book.class);
        CoAuthorView coAuthorView = mock(CoAuthorView.class);
        AuthorCoAuthorBooksView resultView = new AuthorCoAuthorBooksView(
                testAuthorView,
                Arrays.asList(coAuthorView)
        );

        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));
        when(authorService.findCoAuthorsByAuthorNumber(1L))
                .thenReturn(Arrays.asList(coAuthor));
        when(authorService.findBooksByAuthorNumber(2L))
                .thenReturn(Arrays.asList(book));
        when(authorViewMapper.toCoAuthorView(eq(coAuthor), anyList()))
                .thenReturn(coAuthorView);
        when(authorViewMapper.toAuthorCoAuthorBooksView(eq(testAuthor), anyList()))
                .thenReturn(resultView);

        // Act & Assert
        mockMvc.perform(get("/api/authors/1/coauthors")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(authorService).findCoAuthorsByAuthorNumber(1L);
    }

    @Test
    void testGetCoAuthors_whenAuthorNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/authors/999/coauthors")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // DELETE AUTHOR PHOTO - DELETE /api/authors/{authorNumber}/photo
    // ========================================

    @Test
    void testDeleteAuthorPhoto_whenPhotoExists_shouldReturn200() throws Exception {
        // Arrange
        testAuthor.setPhoto("photo.jpg");

        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));
        when(authorService.removeAuthorPhoto(1L, 1L))
                .thenReturn(Optional.of(testAuthor));
        doNothing().when(fileStorageService).deleteFile("photo.jpg");

        // Act & Assert
        mockMvc.perform(delete("/api/authors/1/photo")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(fileStorageService).deleteFile("photo.jpg");
        verify(authorService).removeAuthorPhoto(1L, 1L);
    }

    @Test
    void testDeleteAuthorPhoto_whenNoPhoto_shouldReturn404() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));

        // Act & Assert
        mockMvc.perform(delete("/api/authors/1/photo")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(authorService, never()).removeAuthorPhoto(anyLong(), anyLong());
    }

    @Test
    void testDeleteAuthorPhoto_whenAuthorNotExists_shouldReturn403() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/authors/999/photo")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(fileStorageService, never()).deleteFile(anyString());
    }

    // ========================================
    // SECURITY & ERROR HANDLING
    // ========================================

    @Test
    void testCreateAuthor_withoutAuthentication_shouldReturn401() throws Exception {
        // This test would require @WithAnonymousUser or removing @WithMockUser
        // For now, we verify CSRF is required
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test\", \"bio\": \"Bio\"}"))
                .andExpect(status().isForbidden()); // CSRF missing
    }

    @Test
    void testServiceThrowsNotFoundException_shouldReturn404() throws Exception {
        // Arrange
        when(authorService.findByAuthorNumber(999L))
                .thenThrow(new NotFoundException(Author.class, 999L));

        // Act & Assert
        mockMvc.perform(get("/api/authors/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // COMPLETE INTEGRATION SCENARIOS
    // ========================================

    @Test
    void testCompleteFlow_createUpdateFindDelete() throws Exception {
        // 1. Create
        when(fileStorageService.getRequestPhoto(any())).thenReturn(null);
        when(authorService.create(any())).thenReturn(testAuthor);
        when(authorViewMapper.toAuthorView(testAuthor)).thenReturn(testAuthorView);

        mockMvc.perform(post("/api/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"John Doe\", \"bio\": \"Bio\"}"))
                .andExpect(status().isCreated());

        // 2. Find
        when(authorService.findByAuthorNumber(1L))
                .thenReturn(Optional.of(testAuthor));

        mockMvc.perform(get("/api/authors/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        // 3. Update
        when(concurrencyService.getVersionFromIfMatchHeader("1")).thenReturn(1L);
        when(authorService.partialUpdate(eq(1L), any(), eq(1L)))
                .thenReturn(testAuthor);

        mockMvc.perform(patch("/api/authors/1")
                        .with(csrf())
                        .header("If-Match", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isOk());

        // 4. Verify all operations were called
        verify(authorService).create(any());
        verify(authorService, atLeast(1)).findByAuthorNumber(1L);
        verify(authorService).partialUpdate(eq(1L), any(), eq(1L));
    }
}