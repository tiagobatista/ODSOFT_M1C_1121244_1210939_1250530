package pt.psoft.g1.psoftg1.readermanagement.model.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
public class PhoneNumberEntity {

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    public PhoneNumberEntity(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return this.phoneNumber;
    }
}