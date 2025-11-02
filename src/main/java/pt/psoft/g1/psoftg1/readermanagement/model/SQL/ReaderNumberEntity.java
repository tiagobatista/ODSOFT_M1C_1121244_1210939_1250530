package pt.psoft.g1.psoftg1.readermanagement.model.SQL;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@NoArgsConstructor
@Getter
public class ReaderNumberEntity {

    @Column(name = "reader_number", nullable = false, unique = true)
    private String readerNumber;

    public ReaderNumberEntity(int year, int number) {
        this.readerNumber = year + "/" + number;
    }

    public ReaderNumberEntity(int number) {
        this.readerNumber = LocalDate.now().getYear() + "/" + number;
    }

    public ReaderNumberEntity(String readerNumber) {
        this.readerNumber = readerNumber;
    }

    @Override
    public String toString() {
        return this.readerNumber;
    }
}