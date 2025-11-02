package pt.psoft.g1.psoftg1.bookmanagement.model.SQL;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SQL/JPA Embeddable - IsbnEntity
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IsbnEntity {

    @Size(min = 10, max = 13)
    @Column(name = "ISBN", length = 16, unique = true, nullable = false)
    private String isbn;

    @Override
    public String toString() {
        return isbn;
    }
}
