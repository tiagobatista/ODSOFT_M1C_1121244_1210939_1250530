package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.SQL;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Primary;  // ← ADICIONA ESTE IMPORT
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Mapper.FineEntityMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.SQL.FineEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("sql-redis")
@Primary  // ← ADICIONA ESTA ANOTAÇÃO
@Transactional
public class FineRepositoryImpl implements FineRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final FineEntityMapper mapper;

    public FineRepositoryImpl(FineEntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Fine> findByLendingNumber(String lendingNumber) {
        String jpql = "SELECT f FROM FineEntity f " +
                "JOIN f.relatedLending l " +
                "WHERE l.lendingNumber.lendingNumber = :lendingNumber";

        List<FineEntity> results = entityManager
                .createQuery(jpql, FineEntity.class)
                .setParameter("lendingNumber", lendingNumber)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(mapper.toModel(results.get(0)));
    }

    @Override
    public Iterable<Fine> findAll() {
        String jpql = "SELECT f FROM FineEntity f ORDER BY f.id";

        return entityManager
                .createQuery(jpql, FineEntity.class)
                .getResultList()
                .stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Fine save(Fine fine) {
        FineEntity entity = mapper.toEntity(fine);

        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entity = entityManager.merge(entity);
        }

        entityManager.flush();
        return mapper.toModel(entity);
    }
}
