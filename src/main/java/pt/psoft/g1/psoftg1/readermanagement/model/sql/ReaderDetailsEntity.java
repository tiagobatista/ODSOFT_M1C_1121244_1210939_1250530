package pt.psoft.g1.psoftg1.readermanagement.model.sql;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.shared.model.SQL.EntityWithPhotoEntity;
import pt.psoft.g1.psoftg1.shared.model.SQL.PhotoEntity;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.SQL.ReaderEntity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("sql-redis")
@Primary
@Entity
@Table(name = "READER_DETAILS")
@Getter
@Setter
public class ReaderDetailsEntity extends EntityWithPhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @OneToOne
    @JoinColumn(name = "reader_id", nullable = false)
    private ReaderEntity reader;

    @Embedded
    private ReaderNumberEntity readerNumber;

    @Embedded
    private BirthDateEntity birthDate;

    @Embedded
    private PhoneNumberEntity phoneNumber;

    @Column(nullable = false)
    private boolean gdprConsent;

    @Column(nullable = false)
    private boolean marketingConsent;

    @Column(nullable = false)
    private boolean thirdPartySharingConsent;

    @Version
    private Long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "READER_INTERESTS",
            joinColumns = @JoinColumn(name = "reader_details_pk"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<GenreEntity> interestList = new ArrayList<>();

    protected ReaderDetailsEntity() {
        // for ORM only
    }

    public ReaderDetailsEntity(
            ReaderNumberEntity readerNumber,
            ReaderEntity reader,
            BirthDateEntity birthDate,
            PhoneNumberEntity phoneNumber,
            boolean gdprConsent,
            boolean marketingConsent,
            boolean thirdPartySharingConsent,
            PhotoEntity photo,
            List<GenreEntity> interestList) {

        this.reader = reader;
        this.readerNumber = readerNumber;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.gdprConsent = gdprConsent;
        setPhoto(photo);
        this.marketingConsent = marketingConsent;
        this.thirdPartySharingConsent = thirdPartySharingConsent;
        this.interestList = interestList != null ? interestList : new ArrayList<>();
    }
}
