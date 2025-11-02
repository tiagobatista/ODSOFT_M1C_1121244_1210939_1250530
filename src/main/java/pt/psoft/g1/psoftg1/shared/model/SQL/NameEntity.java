package pt.psoft.g1.psoftg1.shared.model.SQL;



import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.PropertySource;

/**
 * SQL/JPA Embeddable - NameEntity
 * Contém apenas anotações de persistência (sem lógica de negócio)
 */
@Embeddable
@Getter
@Setter


public class NameEntity {

    private static final int NAME_MAX_LENGTH = 255;

    @Column(name = "NAME", nullable = false, length = NAME_MAX_LENGTH)
    @NotNull
    @Size(min = 1, max = NAME_MAX_LENGTH)
    private String name;

    @Override
    public String toString() {
        return name;
    }
}