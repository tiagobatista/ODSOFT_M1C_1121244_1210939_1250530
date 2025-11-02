package pt.psoft.g1.psoftg1.bookmanagement.model.sql;



import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.psoft.g1.psoftg1.bookmanagement.model.Description;

/**
 * SQL/JPA Embeddable - DescriptionEntity
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DescriptionEntity {

    @Size(max = Description.DESC_MAX_LENGTH)
    @Column(name = "DESCRIPTION", length = Description.DESC_MAX_LENGTH)
    private String description;

    @Override
    public String toString() {
        return description;
    }
}