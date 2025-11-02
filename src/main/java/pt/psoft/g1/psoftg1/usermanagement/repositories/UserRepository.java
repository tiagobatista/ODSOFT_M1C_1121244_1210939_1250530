package pt.psoft.g1.psoftg1.usermanagement.repositories;

import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    <S extends User> S save(S entity);

    <S extends User> List<S> saveAll(Iterable<S> entities);

    Optional<User> findById(Long id);

    User getById(Long id); // Este método retorna User, não UserEntity

    Optional<User> findByUsername(String username);

    List<User> findByNameName(String name);

    List<User> findByNameNameContains(String name);

    List<User> searchUsers(Page page, SearchUsersQuery query);

    void delete(User user); // Método delete
}