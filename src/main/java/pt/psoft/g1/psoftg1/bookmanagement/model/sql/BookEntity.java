package pt.psoft.g1.psoftg1.bookmanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorEntity;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreEntity;
import pt.psoft.g1.psoftg1.shared.model.SQL.EntityWithPhotoEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL/JPA Entity - BookEntity
 * Versão JPA do Book (Domain)
 */
@Profile("sql-redis")
@Primary
@Entity
@Table(name = "Book", uniqueConstraints = {
        @UniqueConstraint(name = "uc_book_isbn", columnNames = {"ISBN"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookEntity extends EntityWithPhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Version
    private Long version;

    @Embedded
    private IsbnEntity isbn;

    @Embedded
    @NotNull
    private TitleEntity title;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    @NotNull
    private GenreEntity genre;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_pk"),
            inverseJoinColumns = @JoinColumn(name = "author_number")
    )
    private List<AuthorEntity> authors = new ArrayList<>();

    @Embedded
    private DescriptionEntity description;
}
