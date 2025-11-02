/*
 * Copyright (c) 2022-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.SQL;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.usermanagement.model.SQL.UserEntity;

/**
 * Based on https://github.com/Yoh0xFF/java-spring-security-example
 *
 */
@CacheConfig(cacheNames = "users")
public interface SpringDataUserRepository extends CrudRepository<UserEntity, Long> {

    @CacheEvict(allEntries = true)
    <S extends UserEntity> List<S> saveAll(Iterable<S> entities);

    @Caching(evict = { @CacheEvict(key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(key = "#p0.username", condition = "#p0.username != null") })
    <S extends UserEntity> S save(S entity);

    /**
     * findById searches a specific user and returns an optional
     */

    @Cacheable
    Optional<UserEntity> findById(Long objectId);

    /**
     * getById explicitly loads a user or throws an exception if the user does not
     * exist or the account is not enabled
     *
     * @param id
     * @return
     */
    @Cacheable
    default UserEntity getById(final Long id) {
        final Optional<UserEntity> maybeUser = findById(id);
        // throws 404 Not Found if the user does not exist or is not enabled
        return maybeUser.filter(UserEntity::isEnabled).orElseThrow(() -> new NotFoundException(UserEntity.class, id));
    }

    @Cacheable
    @Query("SELECT u FROM UserEntity u WHERE u.username = ?1")
    Optional<UserEntity> findByUsername(String username);

    @Cacheable
    @Query("SELECT u FROM UserEntity u WHERE u.name.name = ?1")
    List<UserEntity> findByNameName(String name);

    @Cacheable
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.name.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<UserEntity> findByNameNameContains(@Param("namePart") String namePart);
}

