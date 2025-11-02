package pt.psoft.g1.psoftg1.shared.model.sql;



import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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