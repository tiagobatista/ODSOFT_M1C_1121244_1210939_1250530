package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Mapper.LendingEntityMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.SpringDataLendingRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.LendingEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.sql.ReaderDetailsRepositoryImpl;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("sql-redis")

@RequiredArgsConstructor
public class LendingRepositoryImpl implements LendingRepository {

    private final SpringDataLendingRepository lendingRepo;
    private final LendingEntityMapper lendingEntityMapper;
    private final EntityManager em;
    private final BookRepository bookRepo;
    private final ReaderDetailsRepositoryImpl readerDetailsRepo;

    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        return lendingRepo.findByLendingNumber(lendingNumber)
                .map(lendingEntityMapper::toModel);
    }

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) {
        return lendingRepo.listByReaderNumberAndIsbn(readerNumber, isbn).stream()
                .map(lendingEntityMapper::toModel)
                .toList();
    }

    @Override
    public int getCountFromCurrentYear() {
        return lendingRepo.getCountFromCurrentYear();
    }

    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber) {
        return lendingRepo.listOutstandingByReaderNumber(readerNumber).stream()
                .map(lendingEntityMapper::toModel)
                .toList();
    }

    @Override
    public Double getAverageDuration() {
        return lendingRepo.getAverageDuration();
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        return lendingRepo.getAvgLendingDurationByIsbn(isbn);
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LendingEntity> cq = cb.createQuery(LendingEntity.class);
        Root<LendingEntity> root = cq.from(LendingEntity.class);

        // Select overdue lendings: not returned and past due date
        Predicate notReturned = cb.isNull(root.get("returnedDate"));
        Predicate pastDue = cb.lessThan(root.get("limitDate"), LocalDate.now());

        cq.where(cb.and(notReturned, pastDue));
        cq.orderBy(cb.asc(root.get("limitDate")));

        TypedQuery<LendingEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList().stream()
                .map(lendingEntityMapper::toModel)
                .toList();
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn,
                                        Boolean returned, LocalDate startDate, LocalDate endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LendingEntity> cq = cb.createQuery(LendingEntity.class);
        Root<LendingEntity> lendingRoot = cq.from(LendingEntity.class);
        Join<LendingEntity, BookEntity> bookJoin = lendingRoot.join("book");
        Join<LendingEntity, ReaderDetailsEntity> readerDetailsJoin = lendingRoot.join("readerDetails");

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(readerNumber)) {
            predicates.add(cb.like(
                    readerDetailsJoin.get("readerNumber").get("readerNumber"),
                    readerNumber
            ));
        }
        if (StringUtils.hasText(isbn)) {
            predicates.add(cb.like(
                    bookJoin.get("isbn").get("isbn"),
                    isbn
            ));
        }
        if (returned != null) {
            if (returned) {
                predicates.add(cb.isNotNull(lendingRoot.get("returnedDate")));
            } else {
                predicates.add(cb.isNull(lendingRoot.get("returnedDate")));
            }
        }
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    lendingRoot.get("startDate"),
                    startDate
            ));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    lendingRoot.get("startDate"),
                    endDate
            ));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(lendingRoot.get("lendingNumber")));

        TypedQuery<LendingEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList().stream()
                .map(lendingEntityMapper::toModel)
                .toList();
    }

    @Override
    public Lending save(Lending lending) {
        LendingEntity entity = lendingEntityMapper.toEntity(lending);

        // Get managed entity references
        Book bookModel = bookRepo.findByIsbn(lending.getBorrowedBook().getIsbn().toString())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book not found with ISBN: " + lending.getBorrowedBook().getIsbn().toString()));

        ReaderDetails readerDetailsModel = readerDetailsRepo.findByReaderNumber(
                        lending.getBorrower().getReaderNumber())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reader not found with number: " + lending.getBorrower().getReaderNumber()));

        // Set managed entity references
        entity.setBook(em.getReference(BookEntity.class, bookModel.getPk()));
        entity.setReaderDetails(em.getReference(ReaderDetailsEntity.class, readerDetailsModel.getPk()));

        LendingEntity savedEntity = lendingRepo.save(entity);
        return lendingEntityMapper.toModel(savedEntity);
    }

    @Override
    public void delete(Lending lending) {
        // CRITICAL FIX: Find and delete the managed entity, don't create a new one
        lendingRepo.findByLendingNumber(lending.getLendingNumber())
                .ifPresent(lendingRepo::delete);
    }
}
