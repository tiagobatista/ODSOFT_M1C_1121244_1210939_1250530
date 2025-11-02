package pt.psoft.g1.psoftg1.external.service.isbn.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pt.psoft.g1.psoftg1.external.service.isbn.IsbnSearchResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Provider para Google Books API
 * Documentação: https://developers.google.com/books/docs/v1/using
 *
 * Não precisa de API key obrigatoriamente, mas tem rate limit sem key
 */
@Component
@RequiredArgsConstructor
public class GoogleBooksIsbnProvider implements ExternalIsbnProvider {

    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksIsbnProvider.class);
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.api.google-books.api-key:}")
    private String apiKey;

    @Value("${external.api.google-books.enabled:true}")
    private boolean enabled;

    @Override
    public String getProviderName() {
        return "Google Books";
    }

    @Override
    public List<IsbnSearchResult> searchByTitle(String title) throws Exception {
        if (!isAvailable()) {
            logger.warn("Google Books provider is disabled");
            return List.of();
        }

        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = buildSearchUrl(encodedTitle);

            logger.info("🔍 Searching Google Books for: {}", title);
            String response = restTemplate.getForObject(url, String.class);

            return parseResponse(response);

        } catch (Exception e) {
            logger.error("Error searching Google Books: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return 1; // Maior prioridade
    }

    private String buildSearchUrl(String encodedTitle) {
        StringBuilder url = new StringBuilder(API_URL);
        url.append("?q=intitle:").append(encodedTitle);
        url.append("&maxResults=5");

        if (apiKey != null && !apiKey.isBlank()) {
            url.append("&key=").append(apiKey);
        }

        return url.toString();
    }

    private List<IsbnSearchResult> parseResponse(String response) throws Exception {
        List<IsbnSearchResult> results = new ArrayList<>();

        JsonNode root = objectMapper.readTree(response);
        JsonNode items = root.get("items");

        if (items == null || !items.isArray()) {
            logger.info("No results found in Google Books");
            return results;
        }

        for (JsonNode item : items) {
            JsonNode volumeInfo = item.get("volumeInfo");
            if (volumeInfo == null) continue;

            // Extrair ISBN
            String isbn = extractIsbn(volumeInfo);
            if (isbn == null) continue;

            // Extrair outros dados
            String bookTitle = volumeInfo.has("title") ? volumeInfo.get("title").asText() : null;
            List<String> authors = extractAuthors(volumeInfo);
            String publisher = volumeInfo.has("publisher") ? volumeInfo.get("publisher").asText() : null;
            String publishedDate = volumeInfo.has("publishedDate") ? volumeInfo.get("publishedDate").asText() : null;

            IsbnSearchResult result = new IsbnSearchResult(
                    isbn, bookTitle, authors, publisher, publishedDate, getProviderName()
            );

            results.add(result);
            logger.info("✅ Found ISBN {} from Google Books", isbn);
        }

        return results;
    }

    private String extractIsbn(JsonNode volumeInfo) {
        JsonNode identifiers = volumeInfo.get("industryIdentifiers");
        if (identifiers == null || !identifiers.isArray()) {
            return null;
        }

        // Preferir ISBN_13, depois ISBN_10
        for (JsonNode identifier : identifiers) {
            String type = identifier.get("type").asText();
            if ("ISBN_13".equals(type)) {
                return identifier.get("identifier").asText();
            }
        }

        for (JsonNode identifier : identifiers) {
            String type = identifier.get("type").asText();
            if ("ISBN_10".equals(type)) {
                return identifier.get("identifier").asText();
            }
        }

        return null;
    }

    private List<String> extractAuthors(JsonNode volumeInfo) {
        List<String> authors = new ArrayList<>();
        JsonNode authorsNode = volumeInfo.get("authors");

        if (authorsNode != null && authorsNode.isArray()) {
            for (JsonNode author : authorsNode) {
                authors.add(author.asText());
            }
        }

        return authors;
    }
}