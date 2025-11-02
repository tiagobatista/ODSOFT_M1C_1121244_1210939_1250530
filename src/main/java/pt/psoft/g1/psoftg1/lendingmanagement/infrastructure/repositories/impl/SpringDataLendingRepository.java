package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pt.psoft.g1.psoftg1.lendingmanagement.model.SQL.LendingEntity;

import java.util.*;

public interface SpringDataLendingRepository extends CrudRepository<LendingEntity, Long> {

    @Query("SELECT l " +
            "FROM LendingEntity l " +
            "WHERE l.lendingNumber.lendingNumber = :lendingNumber")
    Optional<LendingEntity> findByLendingNumber(@Param("lendingNumber") String lendingNumber);

    @Query("SELECT l " +
            "FROM LendingEntity l " +
            "JOIN l.book b " +
            "JOIN l.readerDetails r " +
            "WHERE b.isbn.isbn = :isbn " +
            "AND r.readerNumber.readerNumber = :readerNumber")
    List<LendingEntity> listByReaderNumberAndIsbn(
            @Param("readerNumber") String readerNumber,
            @Param("isbn") String isbn);

    @Query("SELECT COUNT(l) " +
            "FROM LendingEntity l " +
            "WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();

    @Query("SELECT l " +
            "FROM LendingEntity l " +
            "JOIN l.readerDetails r " +
            "WHERE r.readerNumber.readerNumber = :readerNumber " +
            "AND l.returnedDate IS NULL")
    List<LendingEntity> listOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);

    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM Lending l " +
                    "WHERE l.returned_date IS NOT NULL",
            nativeQuery = true)
    Double getAverageDuration();

    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
                    "FROM Lending l " +
                    "JOIN BOOK b ON l.BOOK_FK = b.PK " +
                    "WHERE b.ISBN = :isbn " +
                    "AND l.returned_date IS NOT NULL",
            nativeQuery = true)
    Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);
}