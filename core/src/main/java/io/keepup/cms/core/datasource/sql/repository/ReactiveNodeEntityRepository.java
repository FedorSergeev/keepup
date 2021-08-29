package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveNodeEntityRepository extends ReactiveCrudRepository<NodeEntity, Long> {

    @Query("SELECT * FROM node_entity as node WHERE node.id IN (:ids)")
    Flux<NodeEntity> findByIds(@Param("ids") Iterable<Long> ids);

    @Query("SELECT * from NODE_ENTITY as node WHERE node.parent_id IN (:ids)")
    Flux<NodeEntity> findByParentIds(@Param("ids") Iterable<Long> ids);

    @Query("SELECT * from NODE_ENTITY as node WHERE node.parent_id IN (:ids) AND node.entity_type = :type")
    Flux<NodeEntity> findByParentIdsAndType(@Param("ids") Iterable<Long> ids, @Param("type") String type);

    @Query("SELECT * from NODE_ENTITY as node WHERE node.id = :id AND node.entity_type = :type")
    Mono<NodeEntity> findByIdAndType(@Param("id") Long id, @Param("type") String type);
}
