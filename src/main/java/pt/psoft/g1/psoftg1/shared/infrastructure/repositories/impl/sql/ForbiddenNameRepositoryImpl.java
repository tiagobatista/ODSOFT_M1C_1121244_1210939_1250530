package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.Mapper.ForbiddenNameEntityMapper;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.sql.ForbiddenNameEntity;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;

/**
 * SQL implementation for ForbiddenName repository
 * Handles conversion between domain models and JPA entities
 */
@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class ForbiddenNameRepositoryImpl implements ForbiddenNameRepository {

    private final SpringDataForbiddenNameRepository springDataRepo;
    private final ForbiddenNameEntityMapper entityMapper;

    @Override
    public Iterable<ForbiddenName> findAll() {
        List<ForbiddenName> result = new ArrayList<>();
        Iterable<ForbiddenNameEntity> entities = springDataRepo.findAll();

        for (ForbiddenNameEntity entity : entities) {
            result.add(entityMapper.toModel(entity));
        }

        return result;
    }

    @Override
    public List<ForbiddenName> findByForbiddenNameIsContained(String pattern) {
        List<ForbiddenName> result = new ArrayList<>();
        List<ForbiddenNameEntity> entities = springDataRepo.findByForbiddenNameIsContained(pattern);

        for (ForbiddenNameEntity entity : entities) {
            result.add(entityMapper.toModel(entity));
        }

        return result;
    }

    @Override
    public ForbiddenName save(ForbiddenName forbiddenName) {
        ForbiddenNameEntity entityToSave = entityMapper.toEntity(forbiddenName);
        ForbiddenNameEntity savedEntity = springDataRepo.save(entityToSave);
        return entityMapper.toModel(savedEntity);
    }

    @Override
    public Optional<ForbiddenName> findByForbiddenName(String forbiddenName) {
        Optional<ForbiddenNameEntity> entityOptional = springDataRepo.findByForbiddenName(forbiddenName);

        if (entityOptional.isPresent()) {
            return Optional.of(entityMapper.toModel(entityOptional.get()));
        }

        return Optional.empty();
    }

    @Override
    public int deleteForbiddenName(String forbiddenName) {
        return springDataRepo.deleteForbiddenName(forbiddenName);
    }
}
