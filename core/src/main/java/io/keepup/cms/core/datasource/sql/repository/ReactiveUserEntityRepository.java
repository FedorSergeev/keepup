package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.UserEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * reactive data access object for {@link io.keepup.cms.core.datasource.sql.entity.UserAttributeEntity} entities.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Repository
public interface ReactiveUserEntityRepository extends ReactiveCrudRepository<UserEntity, Long> {
    /**
     * Looks for users with the specified usernames, mostly used for security purposes
     *
     * @param name name of user
     * @return Mono signaling the {@link UserEntity} witch suits the name condition
     */
    Mono<UserEntity> findByUsername(@Param("name") String name);
}
