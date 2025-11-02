package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.sql;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.shared.model.sql.ForbiddenNameEntity;

import java.util.List;
import java.util.Optional;

@Profile("sql-redis")
public interface SpringDataForbiddenNameRepository extends CrudRepository<ForbiddenNameEntity, Long> {

    @Query("SELECT fn FROM ForbiddenNameEntity fn " +
            "WHERE :pat LIKE CONCAT('%', fn.forbiddenName, '%')")
    List<ForbiddenNameEntity> findByForbiddenNameIsContained(@Param("pat") String pat);

    @Query("SELECT fn FROM ForbiddenNameEntity fn " +
            "WHERE fn.forbiddenName = :forbiddenName")
    Optional<ForbiddenNameEntity> findByForbiddenName(@Param("forbiddenName") String forbiddenName);

    @Modifying
    @Query("DELETE FROM ForbiddenNameEntity fn " +
            "WHERE fn.forbiddenName = :forbiddenName")
    int deleteForbiddenName(@Param("forbiddenName") String forbiddenName);
}
