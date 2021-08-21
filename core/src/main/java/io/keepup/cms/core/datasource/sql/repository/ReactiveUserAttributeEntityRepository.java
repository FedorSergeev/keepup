package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.datasource.sql.entity.UserAttributeEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DAO for {@link io.keepup.cms.core.datasource.sql.entity.UserAttributeEntity} objects
 */
@Repository
public interface ReactiveUserAttributeEntityRepository extends ReactiveCrudRepository<UserAttributeEntity, Long> {

    /**
     * Finds all user attributes
     *
     * @param userId user's identifier
     * @return Flux publishing user's attributes
     */
    Flux<UserAttributeEntity> findAllByUserId(Long userId);

    /**
     * Finds all attributes for users specified by identifiers
     *
     * @param userIds user's identifiers
     * @return Flux publishing user's attributes
     */
    @Query("SELECT * FROM user_attributes " +
           "WHERE user_attributes.user_id IN (:userIds)")
    Flux<UserAttributeEntity> findAllByUserIds(Iterable<Long> userIds);

    /**
     * Delete all attributes for user specified by identifier
     *
     * @param id user's id
     * @return Mono signaling that the operation is executed
     */
    @Modifying
    Mono<Void> deleteByUserId(Long id);

    /**
     * Finds ALL user attributes for users that contain the specified attribute names
     *
     * @param userId {@link io.keepup.cms.core.persistence.User} parent id
     * @param attributeNames number of attribute names
     * @return Flux publishing all user attributes for the specified by condition {@link io.keepup.cms.core.persistence.User} objects
     */
    @Query("SELECT * FROM user_attributes " +
           "AS userAttribute " +
           "WHERE userAttribute.user_id IN " +
           " (SELECT user_id FROM user_attributes " +
           "  WHERE user_attributes.user_id = :userId " +
           "  AND user_attributes.attribute_key IN (:attributeNames))"
    )
    Flux<NodeAttributeEntity> findAllByUserIdWithAttributeNames(@Param("userId") Long userId,
                                                                @Param("attributeNames") Iterable<String> attributeNames);

}
