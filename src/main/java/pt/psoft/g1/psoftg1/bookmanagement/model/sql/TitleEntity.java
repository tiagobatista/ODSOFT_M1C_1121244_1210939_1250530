package pt.psoft.g1.psoftg1.bookmanagement.model.sql;



import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.psoft.g1.psoftg1.bookmanagement.model.Title;

/**
 * SQL/JPA Embeddable - TitleEntity
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TitleEntity {

    @Size(min = 1, max = Title.TITLE_MAX_LENGTH)
    @Column(name = "TITLE", length = Title.TITLE_MAX_LENGTH, nullable = false)
    private String title;

    @Override
    public String toString() {
        return title;
    }
}
