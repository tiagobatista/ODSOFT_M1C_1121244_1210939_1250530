package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.SQL;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.SpringDataReaderRepositoryImpl;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Mapper.ReaderDetailsEntityMapper;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.SQL.UserRepositoryImpl;
import pt.psoft.g1.psoftg1.usermanagement.model.SQL.ReaderEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.SQL.UserEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("sql-redis")

@Repository
@RequiredArgsConstructor
public class ReaderDetailsRepositoryImpl implements ReaderRepository
{
    private final SpringDataReaderRepositoryImpl readerRepo;
    private final UserRepositoryImpl userRepo;
    private final ReaderDetailsEntityMapper readerEntityMapper;
    private final EntityManager entityManager;

    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber)
    {
        Optional<ReaderDetailsEntity> entityOpt = readerRepo.findByReaderNumber(readerNumber);
        if (entityOpt.isPresent())
        {
            return Optional.of(readerEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber)
    {
        List<ReaderDetails> readers = new ArrayList<>();
        for (ReaderDetailsEntity r: readerRepo.findByPhoneNumber(phoneNumber))
        {
            readers.add(readerEntityMapper.toModel(r));
        }

        return readers;
    }

    @Override
    public Optional<ReaderDetails> findByUsername(String username)
    {
        Optional<ReaderDetailsEntity> entityOpt = readerRepo.findByUsername(username);
        if (entityOpt.isPresent())
        {
            return Optional.of(readerEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ReaderDetails> findByUserId(Long userId)
    {
        Optional<ReaderDetailsEntity> entityOpt = readerRepo.findByUserId(userId);
        if (entityOpt.isPresent())
        {
            return Optional.of(readerEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public int getCountFromCurrentYear()
    {
        return readerRepo.getCountFromCurrentYear();
    }

    @Override
    public ReaderDetails save(ReaderDetails readerDetails)
    {
        // Convert the domain model (readerDetails) to a JPA entity (ReaderDetailsEntity)
        ReaderDetailsEntity readerDetailsEntity = readerEntityMapper.toEntity(readerDetails);

        // Retrieve the existing User model from the repository
        // Throws an exception if the user is not found
        User userModel = userRepo.findByUsername(readerDetails.getReader().getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //TODO: No futuro aqui vai ter de deixar de ser ID
        // Get the managed JPA reference for the UserEntity using its database ID
        // This ensures we use the existing UserEntity instead of creating a new one
        ReaderEntity userEntity = entityManager.getReference(ReaderEntity.class, userModel.getId());

        readerDetailsEntity.setReader(userEntity);
        return readerEntityMapper.toModel(readerRepo.save(readerDetailsEntity));
    }

    @Override
    public Iterable<ReaderDetails> findAll()
    {
        List<ReaderDetails> readerDetails = new ArrayList<>();
        for (ReaderDetailsEntity r: readerRepo.findAll())
        {
            readerDetails.add(readerEntityMapper.toModel(r));
        }

        return readerDetails;
    }

    @Override
    public Page<ReaderDetails> findTopReaders(Pageable pageable)
    {
        return readerRepo.findTopReaders(pageable).map(readerEntityMapper::toModel);
    }

    @Override
    public Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate)
    {
        return readerRepo.findTopByGenre(pageable, genre, startDate, endDate);
    }

    @Override
    public void delete(ReaderDetails readerDetails)
    {
        readerRepo.delete(readerEntityMapper.toEntity(readerDetails));
    }

    @Override
    public List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query)
    {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ReaderDetailsEntity> cq = cb.createQuery(ReaderDetailsEntity.class);
        final Root<ReaderDetailsEntity> readerDetailsRoot = cq.from(ReaderDetailsEntity.class);
        Join<ReaderDetailsEntity, UserEntity> userJoin = readerDetailsRoot.join("reader");

        cq.select(readerDetailsRoot);

        final List<Predicate> where = new ArrayList<>();
        if (StringUtils.hasText(query.getName()))
        {
            //'contains' type search
            where.add(cb.like(userJoin.get("name").get("name"), "%" + query.getName() + "%"));
            cq.orderBy(cb.asc(userJoin.get("name")));
        }
        if (StringUtils.hasText(query.getEmail()))
        {
            //'exatct' type search
            where.add(cb.equal(userJoin.get("username"), query.getEmail()));
            cq.orderBy(cb.asc(userJoin.get("username")));
        }
        if (StringUtils.hasText(query.getPhoneNumber()))
        {
            //'exatct' type search
            where.add(cb.equal(readerDetailsRoot.get("phoneNumber").get("phoneNumber"), query.getPhoneNumber()));
            cq.orderBy(cb.asc(readerDetailsRoot.get("phoneNumber").get("phoneNumber")));
        }

        // search using OR
        if (!where.isEmpty())
        {
            cq.where(cb.or(where.toArray(new Predicate[0])));
        }


        final TypedQuery<ReaderDetailsEntity> q = entityManager.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List<ReaderDetails> readerDetails = new ArrayList<>();

        for (ReaderDetailsEntity readerDetail : q.getResultList())
        {
            readerDetails.add(readerEntityMapper.toModel(readerDetail));
        }

        return readerDetails;
    }
}
