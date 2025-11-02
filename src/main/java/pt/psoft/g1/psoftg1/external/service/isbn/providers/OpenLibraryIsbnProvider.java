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
 * Provider para Open Library Search API
 * Documentação: https://openlibrary.org/dev/docs/api/search
 *
 * NOTA: A API de busca não retorna ISBN diretamente.
 * Precisamos fazer uma segunda chamada à API de edições.
 */
@Component
@RequiredArgsConstructor
public class OpenLibraryIsbnProvider implements ExternalIsbnProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryIsbnProvider.class);
    private static final String SEARCH_API_URL = "https://openlibrary.org/search.json";
    private static final String EDITION_API_URL = "https://openlibrary.org";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.api.open-library.enabled:true}")
    private boolean enabled;

    @Override
    public String getProviderName() {
        return "Open Library";
    }

    @Override
    public List<IsbnSearchResult> searchByTitle(String title) throws Exception {
        if (!isAvailable()) {
            logger.warn("Open Library provider is disabled");
            return List.of();
        }

        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = SEARCH_API_URL + "?title=" + encodedTitle + "&limit=5";

            logger.info("🔍 Searching Open Library for: {}", title);
            logger.info("📡 URL: {}", url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isBlank()) {
                logger.warn("⚠️ Open Library returned empty response");
                return List.of();
            }

            logger.info("📦 Response received, length: {} chars", response.length());

            List<IsbnSearchResult> results = parseSearchResponse(response);

            if (results.isEmpty()) {
                logger.warn("⚠️ No ISBNs found in Open Library response for: {}", title);
            } else {
                logger.info("✅ Found {} results from Open Library", results.size());
            }

            return results;

        } catch (Exception e) {
            logger.error("❌ Error searching Open Library: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    private List<IsbnSearchResult> parseSearchResponse(String response) throws Exception {
        List<IsbnSearchResult> results = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode docs = root.get("docs");

            if (docs == null || !docs.isArray()) {
                logger.warn("⚠️ 'docs' field not found or not an array");
                return results;
            }

            logger.info("📚 Found {} documents in search response", docs.size());

            int processedCount = 0;
            int foundIsbns = 0;

            for (JsonNode doc : docs) {
                processedCount++;

                // Extrair dados básicos
                String bookTitle = doc.has("title") ? doc.get("title").asText() : null;
                List<String> authors = extractAuthors(doc);
                String publishedDate = doc.has("first_publish_year")
                        ? String.valueOf(doc.get("first_publish_year").asInt())
                        : null;

                // Buscar ISBN através da edição
                String editionKey = doc.has("cover_edition_key")
                        ? doc.get("cover_edition_key").asText()
                        : null;

                if (editionKey == null) {
                    logger.debug("⏭️ Document {} has no cover_edition_key, trying edition_key array", processedCount);

                    // Tentar array edition_key
                    if (doc.has("edition_key") && doc.get("edition_key").isArray()) {
                        JsonNode editionKeys = doc.get("edition_key");
                        if (editionKeys.size() > 0) {
                            editionKey = editionKeys.get(0).asText();
                        }
                    }
                }

                if (editionKey == null) {
                    logger.debug("⏭️ Document {} has no edition key, skipping", processedCount);
                    continue;
                }

                // Buscar detalhes da edição para obter ISBN
                String isbn = fetchIsbnFromEdition(editionKey);

                if (isbn == null) {
                    logger.debug("⏭️ Edition {} has no ISBN, skipping", editionKey);
                    continue;
                }

                foundIsbns++;

                IsbnSearchResult result = new IsbnSearchResult(
                        isbn, bookTitle, authors, null, publishedDate, getProviderName()
                );

                results.add(result);
                logger.info("✅ Added result: ISBN={}, Title={}", isbn, bookTitle);

                // Limitar a 5 resultados com ISBN
                if (foundIsbns >= 5) {
                    break;
                }
            }

            logger.info("📊 Parsing complete: {} results found", results.size());

        } catch (Exception e) {
            logger.error("❌ Error parsing Open Library response: {}", e.getMessage(), e);
            throw e;
        }

        return results;
    }

    /**
     * Busca ISBN de uma edição específica
     * API: https://openlibrary.org/books/{edition_key}.json
     */
    private String fetchIsbnFromEdition(String editionKey) {
        try {
            String url = EDITION_API_URL + "/books/" + editionKey + ".json";
            logger.debug("🔍 Fetching edition details: {}", url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isBlank()) {
                return null;
            }

            JsonNode edition = objectMapper.readTree(response);

            // Tentar isbn_13 primeiro
            if (edition.has("isbn_13") && edition.get("isbn_13").isArray()) {
                JsonNode isbn13Array = edition.get("isbn_13");
                if (isbn13Array.size() > 0) {
                    String isbn = isbn13Array.get(0).asText().replaceAll("[^0-9]", "");
                    logger.debug("✅ Found ISBN-13: {}", isbn);
                    return isbn;
                }
            }

            // Tentar isbn_10
            if (edition.has("isbn_10") && edition.get("isbn_10").isArray()) {
                JsonNode isbn10Array = edition.get("isbn_10");
                if (isbn10Array.size() > 0) {
                    String isbn = isbn10Array.get(0).asText().replaceAll("[^0-9X]", "");
                    logger.debug("✅ Found ISBN-10: {}", isbn);
                    return isbn;
                }
            }

            logger.debug("⚠️ No ISBN found in edition {}", editionKey);
            return null;

        } catch (Exception e) {
            logger.warn("⚠️ Failed to fetch edition {}: {}", editionKey, e.getMessage());
            return null;
        }
    }

    private List<String> extractAuthors(JsonNode doc) {
        List<String> authors = new ArrayList<>();

        if (!doc.has("author_name")) {
            return authors;
        }

        JsonNode authorsNode = doc.get("author_name");

        if (authorsNode != null && authorsNode.isArray()) {
            for (JsonNode author : authorsNode) {
                authors.add(author.asText());
            }
        }

        return authors;
    }
}