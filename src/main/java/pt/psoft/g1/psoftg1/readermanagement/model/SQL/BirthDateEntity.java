package pt.psoft.g1.psoftg1.readermanagement.model.SQL;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@NoArgsConstructor
@Getter
public class BirthDateEntity {

    @Column(name = "birth_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate birthDate;

    public BirthDateEntity(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public BirthDateEntity(int year, int month, int day) {
        this.birthDate = LocalDate.of(year, month, day);
    }

    public BirthDateEntity(String birthDateStr) {
        this.birthDate = LocalDate.parse(birthDateStr);
    }

    @Override
    public String toString() {
        if (birthDate == null) return null;
        return String.format("%d-%02d-%02d",
                this.birthDate.getYear(),
                this.birthDate.getMonthValue(),
                this.birthDate.getDayOfMonth());
    }
}