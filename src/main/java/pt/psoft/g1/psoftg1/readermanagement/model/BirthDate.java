package pt.psoft.g1.psoftg1.readermanagement.model;

import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;

@Getter
public class BirthDate {

    private final LocalDate birthDate;
    private static final String DATE_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2}";
    private static final int MINIMUM_AGE = 12;

    public BirthDate(int year, int month, int day) {
        this.birthDate = validateAndCreate(year, month, day);
    }

    public BirthDate(String birthDate) {
        if(birthDate == null || !birthDate.matches(DATE_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Provided birth date is not in a valid format. Use yyyy-MM-dd");
        }

        String[] dateParts = birthDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        this.birthDate = validateAndCreate(year, month, day);
    }

    private LocalDate validateAndCreate(int year, int month, int day) {
        LocalDate minimumAgeDate = LocalDate.now().minusYears(MINIMUM_AGE);
        LocalDate userDate = LocalDate.of(year, month, day);

        if(userDate.isAfter(minimumAgeDate)) {
            throw new AccessDeniedException("User must be at least " + MINIMUM_AGE + " years old");
        }

        return userDate;
    }

    @Override
    public String toString() {
        if(birthDate == null) return null;
        return String.format("%d-%02d-%02d",
                this.birthDate.getYear(),
                this.birthDate.getMonthValue(),
                this.birthDate.getDayOfMonth());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BirthDate birthDate1 = (BirthDate) o;
        return birthDate.equals(birthDate1.birthDate);
    }

    @Override
    public int hashCode() {
        return birthDate.hashCode();
    }
}