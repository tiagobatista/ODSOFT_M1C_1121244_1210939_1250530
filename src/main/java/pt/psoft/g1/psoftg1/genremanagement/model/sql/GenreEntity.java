package pt.psoft.g1.psoftg1.genremanagement.model.sql;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

/**
 * SQL/JPA Entity - GenreEntity
 * Versão JPA do Genre (Domain)
 */
@Profile("sql-redis")
@Primary
@Entity
@Table(name = "Genre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Size(min = 1, max = Genre.GENRE_MAX_LENGTH, message = "Genre name must be between 1 and 100 characters")
    @Column(unique = true, nullable = false, length = Genre.GENRE_MAX_LENGTH)
    private String genre;

    public GenreEntity(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return genre;
    }
}
