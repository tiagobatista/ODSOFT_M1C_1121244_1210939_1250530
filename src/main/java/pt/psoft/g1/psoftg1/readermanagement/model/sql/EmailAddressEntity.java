package pt.psoft.g1.psoftg1.readermanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
public class EmailAddressEntity {

    @Email
    @Column(name = "email_address", nullable = false, unique = true)
    private String address;

    public EmailAddressEntity(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return this.address;
    }
}