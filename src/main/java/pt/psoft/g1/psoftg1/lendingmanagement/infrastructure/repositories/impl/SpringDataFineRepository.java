package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.lendingmanagement.model.SQL.FineEntity;

import java.util.Optional;

@Profile("sql-redis")
public interface SpringDataFineRepository extends CrudRepository<FineEntity, Long> {

    @Query("SELECT f FROM FineEntity f " +
            "JOIN f.relatedLending l " +
            "WHERE l.lendingNumber.lendingNumber = :lendingNumber")
    Optional<FineEntity> findByLendingNumber(@Param("lendingNumber") String lendingNumber);
}
