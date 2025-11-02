package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.SQL.GenreEntity;  // ← MUDAR!

import java.util.*;


public interface SpringDataGenreRepository extends CrudRepository<GenreEntity, Long> {  // ← MUDAR!

    @Query("SELECT g FROM GenreEntity g")  // ← MUDAR!
    List<GenreEntity> findAllGenres();  // ← MUDAR!

    @Query("SELECT g FROM GenreEntity g WHERE g.genre = :genreName")  // ← MUDAR!
    Optional<GenreEntity> findByString(@Param("genreName") @NotNull String genre);  // ← MUDAR!

    // TODO: Descomentar quando BookEntity existir

    @Query("SELECT new pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO(g.genre, COUNT(b)) " +
            "FROM GenreEntity g " +
            "JOIN BookEntity b ON b.genre.pk = g.pk " +
            "GROUP BY g " +
            "ORDER BY COUNT(b) DESC")
    Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable);

}


