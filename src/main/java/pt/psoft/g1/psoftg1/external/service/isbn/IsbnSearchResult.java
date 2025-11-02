package pt.psoft.g1.psoftg1.external.service.isbn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Resultado da busca de ISBN por título
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IsbnSearchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String isbn;
    private String title;
    private List<String> authors;
    private String publisher;
    private String publishedDate;
    private String source; // "Google Books", "Open Library", "ISBNdb"

    // Construtor mínimo
    public IsbnSearchResult(String isbn, String title, String source) {
        this.isbn = isbn;
        this.title = title;
        this.source = source;
    }
}