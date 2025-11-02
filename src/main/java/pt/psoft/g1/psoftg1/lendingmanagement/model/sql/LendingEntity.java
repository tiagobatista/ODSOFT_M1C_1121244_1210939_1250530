package pt.psoft.g1.psoftg1.lendingmanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsEntity;

import java.time.LocalDate;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Entity
@Table(name = "Lending")  // ← SÓ ESTA LINHA MUDA!
@Profile("sql-redis")
@Primary
@Getter
@Setter
public class LendingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Embedded
    private LendingNumberEntity lendingNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_fk")
    private BookEntity book;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "reader_fk")
    private ReaderDetailsEntity readerDetails;

    @NotNull
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate limitDate;

    @Temporal(TemporalType.DATE)
    private LocalDate returnedDate;

    @Version
    private Long version;

    @Size(max = 1024)
    @Column(length = 1024)
    private String commentary;

    @Column(nullable = false)
    private Integer fineValuePerDayInCents;

    // Construtor vazio para JPA
    protected LendingEntity() {}

    // Construtor completo
    public LendingEntity(BookEntity book, ReaderDetailsEntity readerDetails,
                         LendingNumberEntity lendingNumber, LocalDate startDate,
                         LocalDate limitDate, LocalDate returnedDate,
                         Integer fineValuePerDayInCents, String commentary) {
        this.book = book;
        this.readerDetails = readerDetails;
        this.lendingNumber = lendingNumber;
        this.startDate = startDate;
        this.limitDate = limitDate;
        this.returnedDate = returnedDate;
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        this.commentary = commentary;
    }
}
