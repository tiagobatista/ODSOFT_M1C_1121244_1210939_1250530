package pt.psoft.g1.psoftg1.external.service.isbn.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pt.psoft.g1.psoftg1.external.service.isbn.IsbnSearchResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Provider para ISBNdb API
 * Documentação: https://isbndb.com/apidocs/v2
 *
 * PRECISA DE API KEY (gratuita com 500 requests/dia)
 * Registar em: https://isbndb.com/
 */
@Component
@RequiredArgsConstructor
public class IsbnDbProvider implements ExternalIsbnProvider {

    private static final Logger logger = LoggerFactory.getLogger(IsbnDbProvider.class);
    private static final String API_URL = "https://api2.isbndb.com/books";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.api.isbndb.api-key:}")
    private String apiKey;

    @Value("${external.api.isbndb.enabled:false}")
    private boolean enabled;

    @Override
    public String getProviderName() {
        return "ISBNdb";
    }

    @Override
    public List<IsbnSearchResult> searchByTitle(String title) throws Exception {
        if (!isAvailable()) {
            logger.warn("ISBNdb provider is disabled or API key not configured");
            return List.of();
        }

        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = API_URL + "/" + encodedTitle + "?page=1&pageSize=5";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info("🔍 Searching ISBNdb for: {}", title);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return parseResponse(response.getBody());

        } catch (Exception e) {
            logger.error("Error searching ISBNdb: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public int getPriority() {
        return 3; // Menor prioridade (porque precisa de API key)
    }

    private List<IsbnSearchResult> parseResponse(String response) throws Exception {
        List<IsbnSearchResult> results = new ArrayList<>();

        JsonNode root = objectMapper.readTree(response);
        JsonNode books = root.get("books");

        if (books == null || !books.isArray()) {
            logger.info("No results found in ISBNdb");
            return results;
        }

        for (JsonNode book : books) {
            // Extrair ISBN
            String isbn = book.has("isbn13") ? book.get("isbn13").asText() :
                    book.has("isbn") ? book.get("isbn").asText() : null;

            if (isbn == null) continue;

            // Extrair outros dados
            String bookTitle = book.has("title") ? book.get("title").asText() : null;
            List<String> authors = extractAuthors(book);
            String publisher = book.has("publisher") ? book.get("publisher").asText() : null;
            String publishedDate = book.has("date_published") ? book.get("date_published").asText() : null;

            IsbnSearchResult result = new IsbnSearchResult(
                    isbn, bookTitle, authors, publisher, publishedDate, getProviderName()
            );

            results.add(result);
            logger.info("✅ Found ISBN {} from ISBNdb", isbn);
        }

        return results;
    }

    private List<String> extractAuthors(JsonNode book) {
        List<String> authors = new ArrayList<>();
        JsonNode authorsNode = book.get("authors");

        if (authorsNode != null && authorsNode.isArray()) {
            for (JsonNode author : authorsNode) {
                authors.add(author.asText());
            }
        }

        return authors;
    }
}