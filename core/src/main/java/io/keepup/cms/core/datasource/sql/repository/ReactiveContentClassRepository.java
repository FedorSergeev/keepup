package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.ContentClass;
import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * DAO for linkages between {@link io.keepup.cms.core.persistence.Content} records and it's classes.
 * Used for cases when we save {@link io.keepup.cms.core.persistence.Content} item as interface type
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public interface ReactiveContentClassRepository extends ReactiveCrudRepository<ContentClass, Long> {
    /**
     * Finds all links between {@link NodeEntity} and Java classes representing business entity by entity ID.
     *
     * @param contentId Content record identifier
     * @return          Publisher which emit all the {@link ContentClass} records with the specified content record ID.
     */
    @Query("SELECT id, content_id, class_name FROM ENTITY_CLASSES " +
           "AS content_class " +
           "WHERE content_class.content_id = :contentId")
    Flux<ContentClass> findAllByContentId(@Param("contentId") Long contentId);

    /**
     * Delete class link by {@link io.keepup.cms.core.persistence.Content} record ID.
     *
     * @param contentId record identifier
     * @return          Reactive Streams Publisher with {@link Void}
     */
    @Query("DELETE FROM ENTITY_CLASSES " +
           "WHERE content_id = :contentId")
    Flux<Void> deleteByContentId(@Param("contentId") Long contentId);
}
