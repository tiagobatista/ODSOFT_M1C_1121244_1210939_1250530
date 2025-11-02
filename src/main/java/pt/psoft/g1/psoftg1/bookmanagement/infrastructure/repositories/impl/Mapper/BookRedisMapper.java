package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper para converter Book para/de formato Redis Hash
 *
 * IMPORTANTE: Book tem relacionamentos (Genre e Authors)
 * - Guardamos apenas IDs no Redis para evitar duplicação
 * - Na reconstrução, precisamos buscar Genre e Authors separadamente
 */
@Component
public class BookRedisMapper {

    /**
     * Converte Book para Map (formato Redis Hash)
     */
    public Map<String, String> toRedisHash(Book book) {
        if (book == null) {
            return null;
        }

        Map<String, String> hash = new HashMap<>();

        // PK
        if (book.getPk() != null) {
            hash.put("pk", book.getPk().toString());
        }

        // Version
        if (book.getVersion() != null) {
            hash.put("version", book.getVersion().toString());
        }

        // ISBN
        if (book.getIsbn() != null) {
            hash.put("isbn", book.getIsbn().toString());
        }

        // Title
        if (book.getTitle() != null) {
            hash.put("title", book.getTitle().toString());
        }

        // Description
        if (book.getDescription() != null) {
            hash.put("description", book.getDescription().toString());
        }

        // Genre - guardar apenas o PK
        if (book.getGenre() != null && book.getGenre().getPk() != null) {
            hash.put("genrePk", book.getGenre().getPk().toString());
            hash.put("genreName", book.getGenre().getGenre()); // Para debug
        }

        // Authors - guardar lista de IDs separados por vírgula
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            String authorIds = book.getAuthors().stream()
                    .filter(a -> a.getAuthorNumber() != null)
                    .map(a -> a.getAuthorNumber().toString())
                    .collect(Collectors.joining(","));
            hash.put("authorIds", authorIds);
        }

        // Photo
        if (book.getPhoto() != null && book.getPhoto().getPhotoFile() != null) {
            hash.put("photo", book.getPhoto().getPhotoFile());
        }

        return hash;
    }

    /**
     * Converte Map (Redis Hash) para Book
     *
     * ATENÇÃO: Este método cria um Book com Genre e Authors PARCIAIS
     * - Genre: apenas pk e name
     * - Authors: apenas authorNumber e name
     *
     * Para dados completos, use o SQL.
     */
    public Book fromRedisHash(Map<Object, Object> hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        try {
            // Extrair dados básicos
            String isbn = hash.containsKey("isbn") ? hash.get("isbn").toString() : null;
            String title = hash.containsKey("title") ? hash.get("title").toString() : null;
            String description = hash.containsKey("description") ? hash.get("description").toString() : null;

            // Genre parcial (só pk e name)
            Genre genre = null;
            if (hash.containsKey("genrePk") && hash.containsKey("genreName")) {
                Long genrePk = Long.parseLong(hash.get("genrePk").toString());
                String genreName = hash.get("genreName").toString();
                genre = new Genre(genrePk, genreName);
            }

            // Authors parciais (só authorNumber e name)
            List<Author> authors = new ArrayList<>();
            if (hash.containsKey("authorIds")) {
                String authorIdsStr = hash.get("authorIds").toString();
                String[] authorIdArray = authorIdsStr.split(",");

                for (String authorIdStr : authorIdArray) {
                    if (!authorIdStr.isEmpty()) {
                        Long authorNumber = Long.parseLong(authorIdStr.trim());
                        // Criar Author apenas com ID (dados mínimos)
                        Author author = new Author();
                        author.setAuthorNumber(authorNumber);
                        // Nome será buscado do cache de Authors quando necessário
                        authors.add(author);
                    }
                }
            }

            // Photo
            String photoFile = hash.containsKey("photo") ? hash.get("photo").toString() : null;

            // Criar Book
            if (isbn == null || title == null || genre == null || authors.isEmpty()) {
                return null; // Dados incompletos
            }

            Book book = new Book(isbn, title, description, genre, authors, photoFile);

            // Setar campos adicionais
            if (hash.containsKey("pk")) {
                book.pk = Long.parseLong(hash.get("pk").toString());
            }

            return book;

        } catch (Exception e) {
            System.err.println("Error converting Redis hash to Book: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gera a chave Redis para um Book por ISBN
     */
    public String generateKeyByIsbn(String isbn) {
        return "book:isbn:" + isbn.toLowerCase();
    }

    /**
     * Gera a chave Redis para um Book por Title
     */
    public String generateKeyByTitle(String title) {
        return "book:title:" + title.toLowerCase();
    }
}