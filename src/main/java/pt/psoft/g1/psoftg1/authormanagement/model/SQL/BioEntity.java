package pt.psoft.g1.psoftg1.authormanagement.model.SQL;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SQL/JPA Embeddable - BioEntity
 * Contém apenas anotações de persistência (sem lógica de negócio)
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BioEntity {

    private static final int BIO_MAX_LENGTH = 4096;

    @Column(name = "BIO", nullable = false, length = BIO_MAX_LENGTH)
    @NotNull
    @Size(min = 1, max = BIO_MAX_LENGTH)
    private String bio;
}
