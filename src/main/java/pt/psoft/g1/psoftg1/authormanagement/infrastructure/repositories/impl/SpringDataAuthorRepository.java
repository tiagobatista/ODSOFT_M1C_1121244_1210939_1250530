package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;

import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorEntity;

import java.util.List;
import java.util.Optional;

public interface SpringDataAuthorRepository extends CrudRepository<AuthorEntity, Long> {

    @Query("SELECT a FROM AuthorEntity a WHERE a.authorNumber = :authorNumber")
    Optional<AuthorEntity> findByAuthorNumber(Long authorNumber);

    @Query("SELECT new pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView(a.name.name, COUNT(l.pk)) " +
            "FROM BookEntity b " +
            "JOIN b.authors a " +
            "JOIN LendingEntity l ON l.book.pk = b.pk " +
            "GROUP BY a.name " +
            "ORDER BY COUNT(l) DESC")
    Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable);

    @Query("SELECT DISTINCT coAuthor FROM BookEntity b " +
            "JOIN b.authors coAuthor " +
            "WHERE b IN (SELECT b FROM BookEntity b JOIN b.authors a WHERE a.authorNumber = :authorNumber) " +
            "AND coAuthor.authorNumber <> :authorNumber")
    List<AuthorEntity> findCoAuthorsByAuthorNumber(Long authorNumber);

    @Query("SELECT a FROM AuthorEntity a WHERE a.name.name = :name")
    List<AuthorEntity> searchByNameName(String name);

    @Query("SELECT a FROM AuthorEntity a WHERE a.name.name LIKE :name%")
    List<AuthorEntity> searchByNameNameStartsWith(String name);

    @Query("SELECT a FROM AuthorEntity a")
    Iterable<AuthorEntity> findAll();
}

