package pt.psoft.g1.psoftg1.readermanagement.model;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
public class ReaderNumber implements Serializable {

    private final String readerNumber;

    public ReaderNumber(int year, int number) {
        this.readerNumber = year + "/" + number;
    }

    public ReaderNumber(int number) {
        this.readerNumber = LocalDate.now().getYear() + "/" + number;
    }

    public ReaderNumber(String readerNumber) {
        if(readerNumber == null || !readerNumber.matches("\\d{4}/\\d+")) {
            throw new IllegalArgumentException("Invalid reader number format: " + readerNumber);
        }
        this.readerNumber = readerNumber;
    }

    @Override
    public String toString() {
        return this.readerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReaderNumber that = (ReaderNumber) o;
        return readerNumber.equals(that.readerNumber);
    }

    @Override
    public int hashCode() {
        return readerNumber.hashCode();
    }
}