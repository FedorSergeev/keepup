package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.RoleByUserIdEntity;
import io.keepup.cms.core.datasource.sql.entity.RoleEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DAO for {@link io.keepup.cms.core.datasource.sql.entity.RoleByUserIdEntity}
 */
@Repository
public interface ReactiveRoleByUserEntityRepository extends ReactiveCrudRepository<RoleByUserIdEntity, Long> {

    /**
     * Looks for all entities bu userId who has pair with specified role. Helps to construct set of user's roles
     * by only one of them
     *
     * @param usersRoles - user roles, should not be null
     * @return all records with user ids witch contain one of the specified role
     */
    @Query("SELECT * FROM user_roles " +

           "WHERE user_roles.user_id IN " +
           " (SELECT user_id FROM user_roles " +
           "  WHERE user_roles.role IN (:roles))")
    Flux<RoleByUserIdEntity> findAllWhoHasRoles(@Param("roles") Iterable<String> usersRoles);

    /**
     * Looks up for all objects representing links between user and his roles
     *
     * @param userIds identifiers of users to look their roles for
     * @return Flux publishing the found links
     */
    @Query("SELECT * FROM user_roles " +
            "AS user_role " +
            "WHERE user_role.user_id IN (:userIds)")
    Flux<RoleByUserIdEntity> findAllByUserIds(@Param("userIds") Iterable<Long> userIds);

    /**
     * Finds all roles connected to the specified user
     *
     * @param userId user's identifier
     * @return Flux for publishing the wrapped objects with just one String field representing the user's role
     */
    @Query(value = "SELECT role FROM user_roles WHERE user_id = :userId")
    Flux<RoleEntity> findRolesByUserId(@Param("userId") Long userId);

    /**
     * Inserts a new role to user binding via userId if the same does not already exist in the database
     *
     * @param userId user's identifier
     * @param role   user's role to bind
     * @return Mono signaling that the operation is executed
     */
    @Query("INSERT INTO user_roles" +
            "    (user_id, role) " +
            "SELECT :userId, :role " +
            "WHERE " +
            "    NOT EXISTS (" +
            "        SELECT user_id, role FROM user_roles WHERE user_id = :userId AND role = :role" +
            "    )")
    Mono<Void> updateRole(@Param("userId") Long userId, @Param("role") String role);

    /**
     * Removes user role bindings
     *
     * @param id identifier of the user
     * @return Mono signaling the operation execution
     */
    @Modifying
    Mono<Void> deleteByUserId(Long id);
}
