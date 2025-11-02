package pt.psoft.g1.psoftg1.lendingmanagement.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class LendingNumber implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^\\d{4}/\\d+$");
    private static final String SEPARATOR = "/";
    private static final int MIN_YEAR = 1970;

    private final String formattedNumber;

    // Construtor com ano e sequência
    public LendingNumber(int year, int sequence) {
        validateYear(year);
        validateSequence(sequence);

        this.formattedNumber = formatNumber(year, sequence);
    }

    // Construtor apenas com sequência (usa ano atual)
    public LendingNumber(int sequence) {
        this(LocalDate.now().getYear(), sequence);
    }

    // Construtor a partir de string
    public LendingNumber(String numberString) {
        validateNumberString(numberString);

        String[] parts = parseNumberString(numberString);
        int year = Integer.parseInt(parts[0]);
        int sequence = Integer.parseInt(parts[1]);

        validateYear(year);
        validateSequence(sequence);

        this.formattedNumber = formatNumber(year, sequence);
    }

    // Validações
    private void validateYear(int year) {
        int currentYear = LocalDate.now().getYear();
        if (year < MIN_YEAR || year > currentYear) {
            throw new IllegalArgumentException(
                    String.format("Year must be between %d and %d", MIN_YEAR, currentYear)
            );
        }
    }

    private void validateSequence(int sequence) {
        if (sequence < 0) {
            throw new IllegalArgumentException("Sequence number must be non-negative");
        }
    }

    private void validateNumberString(String numberString) {
        if (numberString == null || numberString.trim().isEmpty()) {
            throw new IllegalArgumentException("Number string cannot be null or empty");
        }

        if (!FORMAT_PATTERN.matcher(numberString).matches()) {
            throw new IllegalArgumentException(
                    "Invalid format. Expected: YYYY/sequence (e.g., 2024/123)"
            );
        }
    }

    // Parsing
    private String[] parseNumberString(String numberString) {
        try {
            return numberString.split(SEPARATOR);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse lending number: " + numberString);
        }
    }

    // Formatação
    private String formatNumber(int year, int sequence) {
        return year + SEPARATOR + sequence;
    }

    // Métodos públicos
    public String getFormattedNumber() {
        return formattedNumber;
    }

    @Override
    public String toString() {
        return formattedNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LendingNumber that = (LendingNumber) obj;
        return formattedNumber.equals(that.formattedNumber);
    }

    @Override
    public int hashCode() {
        return formattedNumber.hashCode();
    }
}