package pt.psoft.g1.psoftg1.lendingmanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class LendingNumberEntity implements Serializable {

    @Column(name = "IDENTIFIER", nullable = false, unique = true)
    private String lendingNumber;

    // Construtor vazio para JPA
    protected LendingNumberEntity() {}

    // Construtor principal
    public LendingNumberEntity(String lendingNumber) {
        this.lendingNumber = lendingNumber;
    }

    // Construtor com ano e sequência
    public LendingNumberEntity(int year, int sequence) {
        this.lendingNumber = year + "/" + sequence;
    }


    @Override
    public String toString() {
        return lendingNumber;
    }
}