package pt.psoft.g1.psoftg1.bookmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.g1.psoftg1.external.service.isbn.IsbnLookupService;
import pt.psoft.g1.psoftg1.external.service.isbn.IsbnSearchResult;

import java.util.List;
import java.util.Map;

/**
 * REST API para buscar ISBN de livros usando APIs externas
 */
@Tag(name = "ISBN Lookup", description = "Search book ISBN from external APIs")
@RestController
@RequestMapping("/api/isbn")
@RequiredArgsConstructor
public class BookIsbnController {

    private final IsbnLookupService isbnLookupService;

    /**
     * Busca ISBN tentando TODAS as APIs disponíveis (com fallback automático)
     */
    @Operation(summary = "Search ISBN by title (all providers with fallback)")
    @GetMapping("/search")
    public ResponseEntity<List<IsbnSearchResult>> searchIsbn(
            @Parameter(description = "Book title to search")
            @RequestParam String title) {

        List<IsbnSearchResult> results = isbnLookupService.searchIsbnByTitle(title);
        return ResponseEntity.ok(results);
    }

    /**
     * Busca ISBN usando apenas Google Books API
     */
    @Operation(summary = "Search ISBN using Google Books API only")
    @GetMapping("/google")
    public ResponseEntity<List<IsbnSearchResult>> searchIsbnGoogle(
            @Parameter(description = "Book title to search")
            @RequestParam String title) {

        List<IsbnSearchResult> results = isbnLookupService.searchIsbnByTitleWithProvider(title, "Google Books");
        return ResponseEntity.ok(results);
    }

    /**
     * Busca ISBN usando apenas Open Library API
     */
    @Operation(summary = "Search ISBN using Open Library API only")
    @GetMapping("/openlibrary")
    public ResponseEntity<List<IsbnSearchResult>> searchIsbnOpenLibrary(
            @Parameter(description = "Book title to search")
            @RequestParam String title) {

        List<IsbnSearchResult> results = isbnLookupService.searchIsbnByTitleWithProvider(title, "Open Library");
        return ResponseEntity.ok(results);
    }

    /**
     * Busca ISBN usando apenas ISBNdb API
     */
    @Operation(summary = "Search ISBN using ISBNdb API only (requires API key)")
    @GetMapping("/isbndb")
    public ResponseEntity<List<IsbnSearchResult>> searchIsbnDb(
            @Parameter(description = "Book title to search")
            @RequestParam String title) {

        List<IsbnSearchResult> results = isbnLookupService.searchIsbnByTitleWithProvider(title, "ISBNdb");
        return ResponseEntity.ok(results);
    }

    /**
     * Lista providers disponíveis
     */
    @Operation(summary = "List available ISBN providers")
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getAvailableProviders() {
        List<String> providers = isbnLookupService.getAvailableProviders();
        return ResponseEntity.ok(Map.of(
                "available_providers", providers,
                "total", providers.size()
        ));
    }
}