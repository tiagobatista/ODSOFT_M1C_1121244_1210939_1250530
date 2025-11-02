package pt.psoft.g1.psoftg1.lendingmanagement.model;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.StaleObjectStateException;

public class Lending {

    // ==================== Atributos ====================
    private Long id;
    private LendingNumber identifier;
    private Book borrowedBook;
    private ReaderDetails borrower;
    private LocalDate startDate;
    private LocalDate limitDate;
    private LocalDate returnedDate;
    private Long versionControl;
    private String feedback;
    private Integer remainingDays;
    private Integer lateDays;
    private int dailyFineAmount;

    // ==================== Construtores ====================

    // Construtor vazio para JPA/MapStruct
    public Lending() {}

    // Construtor para novos empréstimos
    public Lending(Book borrowedBook, ReaderDetails borrower, int sequenceNumber, int maxDays, int dailyFineAmount) {
        validateInputs(borrowedBook, borrower);

        this.borrowedBook = borrowedBook;
        this.borrower = borrower;
        this.identifier = new LendingNumber(sequenceNumber);
        this.startDate = LocalDate.now();
        this.limitDate = this.startDate.plusDays(maxDays);
        this.returnedDate = null;
        this.dailyFineAmount = dailyFineAmount;

        updateTimeMetrics();
    }

    // Construtor completo (para reconstrução)
    public Lending(Book borrowedBook, ReaderDetails borrower, LendingNumber identifier,
                   LocalDate startDate, LocalDate limitDate, LocalDate returnedDate, int dailyFineAmount) {
        validateInputs(borrowedBook, borrower);

        this.borrowedBook = borrowedBook;
        this.borrower = borrower;
        this.identifier = identifier;
        this.startDate = startDate;
        this.limitDate = limitDate;
        this.returnedDate = returnedDate;
        this.dailyFineAmount = dailyFineAmount;

        updateTimeMetrics();
    }

    // ==================== Métodos Factory ====================

    public static Lending newBootstrappingLending(Book book, ReaderDetails readerDetails,
                                                  int year, int seq, LocalDate startDate,
                                                  LocalDate returnedDate, int lendingDuration,
                                                  int fineValuePerDayInCents) {
        return new Lending(book, readerDetails, new LendingNumber(year, seq),
                startDate, startDate.plusDays(lendingDuration),
                returnedDate, fineValuePerDayInCents);
    }

    // ==================== Getters - Domain ====================

    public Long getId() {
        return id;
    }

    public Book getBorrowedBook() {
        return borrowedBook;
    }

    public ReaderDetails getBorrower() {
        return borrower;
    }

    public LocalDate getDateStarted() {
        return startDate;
    }

    public LocalDate getDueDate() {
        return limitDate;
    }

    public Optional<LocalDate> getDateReturned() {
        return Optional.ofNullable(returnedDate);
    }

    public String getIdentifierAsString() {
        return identifier.toString();
    }

    public String getBookTitle() {
        return borrowedBook.getTitle().toString();
    }

    public Optional<String> getFeedback() {
        return Optional.ofNullable(feedback);
    }

    public Long getVersionControl() {
        return versionControl;
    }

    public int getDailyFineAmount() {
        return dailyFineAmount;
    }

    public Optional<Integer> getRemainingDays() {
        updateTimeMetrics();
        return Optional.ofNullable(remainingDays);
    }

    public Optional<Integer> getLateDays() {
        updateTimeMetrics();
        return Optional.ofNullable(lateDays);
    }

    public int getDaysDelayed() {
        return calculateDelayDays();
    }

    public Optional<Integer> getFineValueInCents() {
        return calculateFine();
    }

    // ==================== Getters - MapStruct Aliases ====================

    public Long getPk() {
        return id;
    }

    public Book getBook() {
        return borrowedBook;
    }

    public ReaderDetails getReaderDetails() {
        return borrower;
    }

    public String getLendingNumber() {
        return identifier.toString();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getLimitDate() {
        return limitDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public Long getVersion() {
        return versionControl;
    }

    public String getCommentary() {
        return feedback;
    }

    public Integer getFineValuePerDayInCents() {
        return dailyFineAmount;
    }

    // ==================== Setters - MapStruct ====================

    public void setPk(Long pk) {
        this.id = pk;
    }

    public void setLendingNumber(LendingNumber lendingNumber) {
        this.identifier = lendingNumber;
    }

    public void setBook(Book book) {
        this.borrowedBook = book;
    }

    public void setReaderDetails(ReaderDetails readerDetails) {
        this.borrower = readerDetails;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setLimitDate(LocalDate limitDate) {
        this.limitDate = limitDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public void setVersion(Long version) {
        this.versionControl = version;
    }

    public void setCommentary(String commentary) {
        this.feedback = commentary;
    }

    public void setFineValuePerDayInCents(Integer fineValuePerDayInCents) {
        this.dailyFineAmount = fineValuePerDayInCents != null ? fineValuePerDayInCents : 0;
    }

    // ==================== Métodos de Negócio ====================

    public int calculateDelayDays() {
        LocalDate referenceDate = returnedDate != null ? returnedDate : LocalDate.now();
        long daysDifference = ChronoUnit.DAYS.between(limitDate, referenceDate);
        return Math.max(0, (int) daysDifference);
    }

    public Optional<Integer> calculateFine() {
        int delayDays = calculateDelayDays();
        if (delayDays > 0) {
            return Optional.of(dailyFineAmount * delayDays);
        }
        return Optional.empty();
    }

    public void setReturned(final Long expectedVersion, final String userFeedback) {
        registerReturn(expectedVersion, userFeedback);
    }

    public void registerReturn(final Long expectedVersion, final String userFeedback) {
        if (returnedDate != null) {
            throw new IllegalArgumentException("This lending has already been returned");
        }

        if (!Objects.equals(versionControl, expectedVersion)) {
            throw new StaleObjectStateException("Version conflict detected", this.id);
        }

        this.returnedDate = LocalDate.now();
        if (userFeedback != null && !userFeedback.trim().isEmpty()) {
            this.feedback = userFeedback.trim();
        }

        updateTimeMetrics();
    }

    // ==================== Métodos Privados ====================

    private void validateInputs(Book book, ReaderDetails reader) {
        if (book == null || reader == null) {
            throw new IllegalArgumentException("Book and reader information are required");
        }
    }

    private void updateTimeMetrics() {
        // Calcular dias restantes
        if (returnedDate == null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), limitDate);
            this.remainingDays = daysLeft >= 0 ? (int) daysLeft : null;
        } else {
            this.remainingDays = null;
        }

        // Calcular dias em atraso
        int delay = calculateDelayDays();
        this.lateDays = delay > 0 ? delay : null;
    }
}