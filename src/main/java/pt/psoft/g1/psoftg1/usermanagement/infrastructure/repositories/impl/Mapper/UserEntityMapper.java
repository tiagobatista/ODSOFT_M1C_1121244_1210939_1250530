package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.Mapper;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.LibrarianEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.ReaderEntity;
import pt.psoft.g1.psoftg1.usermanagement.model.sql.UserEntity;

@Component
public class UserEntityMapper {

    // User mappings
    public User toModel(UserEntity entity) {
        if (entity == null) return null;

        User user;
        if (entity instanceof ReaderEntity) {
            user = new Reader(entity.getUsername(), entity.getPassword());
        } else if (entity instanceof LibrarianEntity) {
            user = new Librarian(entity.getUsername(), entity.getPassword());
        } else {
            user = new User(entity.getUsername(), entity.getPassword());
        }

        mapCommonFieldsToModel(entity, user);

        return user;
    }

    public UserEntity toEntity(User model) {
        if (model == null) return null;

        UserEntity entity;
        if (model instanceof Reader) {
            entity = new ReaderEntity(model.getUsername(), model.getPassword());
        } else if (model instanceof Librarian) {
            entity = new LibrarianEntity(model.getUsername(), model.getPassword());
        } else {
            entity = new UserEntity(model.getUsername(), model.getPassword());
        }

        mapCommonFieldsToEntity(model, entity);

        return entity;
    }

    // Librarian mappings
    public Librarian toModel(LibrarianEntity entity) {
        if (entity == null) return null;

        Librarian librarian = new Librarian(entity.getUsername(), entity.getPassword());
        mapCommonFieldsToModel(entity, librarian);

        return librarian;
    }

    public LibrarianEntity toEntity(Librarian user) {
        if (user == null) return null;

        LibrarianEntity entity = new LibrarianEntity(user.getUsername(), user.getPassword());
        mapCommonFieldsToEntity(user, entity);

        return entity;
    }

    // Reader mappings
    public Reader toModel(ReaderEntity entity) {
        if (entity == null) return null;

        Reader reader = new Reader(entity.getUsername(), entity.getPassword());
        mapCommonFieldsToModel(entity, reader);

        return reader;
    }

    public ReaderEntity toEntity(Reader user) {
        if (user == null) return null;

        ReaderEntity entity = new ReaderEntity(user.getUsername(), user.getPassword());
        mapCommonFieldsToEntity(user, entity);

        return entity;
    }

    // Conversion utility for UserEntity to ReaderEntity
    public ReaderEntity convertToReaderEntity(UserEntity userEntity) {
        if (userEntity == null) return null;

        ReaderEntity readerEntity = new ReaderEntity(
                userEntity.getUsername(),
                userEntity.getPassword()
        );

        // Não copia createdAt, modifiedAt, createdBy, modifiedBy
        readerEntity.setId(userEntity.getId());
        readerEntity.setVersion(userEntity.getVersion());
        readerEntity.setEnabled(userEntity.isEnabled());

        if (userEntity.getName() != null) {
            readerEntity.setName(userEntity.getName().toString());
        }

        userEntity.getAuthorities().forEach(readerEntity::addAuthority);

        return readerEntity;
    }

    // Métodos auxiliares privados
    private void mapCommonFieldsToModel(UserEntity entity, User user) {
        user.setId(entity.getId());
        user.setVersion(entity.getVersion());
        user.setEnabled(entity.isEnabled());
        user.setCreatedAt(entity.getCreatedAt());
        user.setModifiedAt(entity.getModifiedAt());
        user.setCreatedBy(entity.getCreatedBy());
        user.setModifiedBy(entity.getModifiedBy());

        if (entity.getName() != null) {
            user.setName(entity.getName().toString());
        }

        entity.getAuthorities().forEach(user::addAuthority);
    }

    private void mapCommonFieldsToEntity(User user, UserEntity entity) {
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        if (user.getVersion() != null) {
            entity.setVersion(user.getVersion());
        }

        entity.setEnabled(user.isEnabled());

        if (user.getName() != null) {
            entity.setName(user.getName().toString());
        }

        user.getAuthorities().forEach(entity::addAuthority);
    }
}