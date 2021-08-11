package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReactiveNodeEntityRepository extends ReactiveCrudRepository<NodeEntity, Long> {

    @Query("SELECT * FROM node_entity as node WHERE node.id IN (:ids)")
    Flux<NodeEntity> findByIds(Iterable<Long> ids);

    @Query("SELECT * from NODE_ENTITY as node WHERE node.parent_id IN (:ids)")
    Flux<NodeEntity> findByParentIds(Iterable<Long> ids);
}
