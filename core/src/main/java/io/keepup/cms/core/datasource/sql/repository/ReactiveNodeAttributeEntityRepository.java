package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DAO for {@link io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity} objects
 */
@Repository
public interface ReactiveNodeAttributeEntityRepository extends ReactiveCrudRepository<NodeAttributeEntity, Long> {

    Flux<NodeAttributeEntity> findAllByContentId(Long contentId);

    Mono<NodeAttributeEntity> findByContentIdAndAttributeKey(Long contentId, String attributeKey);

    @Modifying
    Mono<Void> deleteByContentId(Long id);
}
