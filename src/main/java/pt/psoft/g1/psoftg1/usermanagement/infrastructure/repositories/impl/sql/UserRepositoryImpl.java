package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.Mapper.UserEntityMapper;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.LibrarianEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.ReaderEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.UserEntity;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springDataRepo;
    private final UserEntityMapper mapper;
    private final EntityManager entityManager;

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {
        List<UserEntity> entitiesToSave = StreamSupport.stream(entities.spliterator(), false)
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        List<UserEntity> savedEntities = springDataRepo.saveAll(entitiesToSave);

        return savedEntities.stream()
                .map(entity -> (S) convertToDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public <S extends User> S save(S user) {
        UserEntity entityToSave = convertToEntity(user);
        UserEntity savedEntity = springDataRepo.save(entityToSave);
        return (S) convertToDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long objectId) {
        return springDataRepo.findById(objectId)
                .map(this::convertToDomain);
    }

    @Override
    public User getById(Long id) {
        Optional<UserEntity> entityOptional = springDataRepo.findById(id);

        UserEntity entity = entityOptional
                .filter(UserEntity::isEnabled)
                .orElseThrow(() -> new NotFoundException(User.class, id));

        return convertToDomain(entity);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataRepo.findByUsername(username)
                .map(this::convertToDomain);
    }

    @Override
    public List<User> findByNameName(String name) {
        return springDataRepo.findByNameName(name).stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByNameNameContains(String name) {
        return springDataRepo.findByNameNameContains(name).stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> searchUsers(Page page, SearchUsersQuery query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = cq.from(UserEntity.class);

        List<Predicate> predicates = buildPredicates(cb, root, query);

        if (!predicates.isEmpty()) {
            cq.where(cb.or(predicates.toArray(new Predicate[0])));
        }

        cq.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<UserEntity> typedQuery = entityManager.createQuery(cq);
        typedQuery.setFirstResult((page.getNumber() - 1) * page.getLimit());
        typedQuery.setMaxResults(page.getLimit());

        return typedQuery.getResultList().stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(User user) {
        if (user != null && user.getId() != null) {
            springDataRepo.deleteById(user.getId());
        }
    }

    // ===== Métodos auxiliares privados =====

    private UserEntity convertToEntity(User user) {
        if (user instanceof Reader) {
            return mapper.toEntity((Reader) user);
        } else if (user instanceof Librarian) {
            return mapper.toEntity((Librarian) user);
        } else {
            return mapper.toEntity(user);
        }
    }

    private User convertToDomain(UserEntity entity) {
        if (entity instanceof ReaderEntity) {
            return mapper.toModel((ReaderEntity) entity);
        } else if (entity instanceof LibrarianEntity) {
            return mapper.toModel((LibrarianEntity) entity);
        } else {
            return mapper.toModel(entity);
        }
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<UserEntity> root,
            SearchUsersQuery query) {

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(query.getUsername())) {
            predicates.add(cb.equal(root.get("username"), query.getUsername()));
        }

        if (StringUtils.hasText(query.getName())) {
            predicates.add(cb.like(
                    cb.lower(root.get("name").get("name")),
                    "%" + query.getName().toLowerCase() + "%"
            ));
        }

        return predicates;
    }
}