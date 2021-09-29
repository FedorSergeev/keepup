package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.ContentClass;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * DAO for linkages between {@link io.keepup.cms.core.persistence.Content} records and it's classes.
 * Used for cases when we save {@link io.keepup.cms.core.persistence.Content} item as interface type
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
public interface ReactiveContentClassRepository extends ReactiveCrudRepository<ContentClass, Long> {
    @Query("SELECT * FROM ENTITY_CLASSES " +
           "AS content_class " +
           "WHERE content_class.content_id = :contentId")
    Flux<ContentClass> findAllByContentId(@Param("contentId") final Long contentId);

    @Query("DELETE FROM ENTITY_CLASSES " +
           "WHERE content_id = :contentId")
    Flux<Void> deleteByContentId(@Param("contentId") final Long contentId);
}
