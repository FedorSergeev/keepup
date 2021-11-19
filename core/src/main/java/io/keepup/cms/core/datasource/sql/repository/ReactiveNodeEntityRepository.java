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

    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
           "FROM node_entity as node WHERE node.id IN (:ids)")
    Flux<NodeEntity> findByIds(@Param("ids") Iterable<Long> ids);

    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
            "FROM node_entity as node WHERE node.parent_id IN (:ids)")
    Flux<NodeEntity> findByParentIds(@Param("ids") Iterable<Long> ids);

    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
           "FROM node_entity " +
           "AS node WHERE node.parent_id IN (:ids) " +
           "AND (node.entity_type = :type " +
           "     OR node.id IN (SELECT content_id FROM ENTITY_CLASSES " +
           "                    WHERE ENTITY_CLASSES.class_name = :type))")
    Flux<NodeEntity> findByParentIdsAndType(@Param("ids") Iterable<Long> ids, @Param("type") String type);

    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
           "FROM node_entity " +
            "AS node WHERE node.id IN (:ids) " +
            "AND (node.entity_type = :type " +
            "     OR node.id IN (SELECT content_id FROM ENTITY_CLASSES " +
            "                 WHERE ENTITY_CLASSES.class_name = :type))")
    Mono<NodeEntity> findByIdAndType(@Param("id") Long id, @Param("type") String type);

    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
            "FROM node_entity as node WHERE node.id = :id OR node.parent_id = :id")
    Flux<NodeEntity> findByIdOrByParentId(@Param("id") Long id);

    @Query("WITH RECURSIVE r AS (" +
            "   SELECT id, parent_id, owner_id, entity_type, " +
            "   owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            "   role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            "   other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege, 1::INT AS depth" +
            "   FROM node_entity" +
            "   WHERE id = :id " +
            "   UNION" +
            "   SELECT node_entity.id, node_entity.parent_id, node_entity.owner_id, node_entity.entity_type, " +
            "   node_entity.owner_read_privilege, node_entity.owner_write_privilege, node_entity.owner_create_children_privilege, node_entity.owner_execute_privilege," +
            "   node_entity.role_read_privilege, node_entity.role_write_privilege, node_entity.role_create_children_privilege, node_entity.role_execute_privilege," +
            "   node_entity.other_read_privilege, node_entity.other_write_privilege, node_entity.other_create_children_privilege, node_entity.other_execute_privilege, 1::INT AS depth " +
            "   FROM node_entity" +
            "      JOIN r" +
            "          ON node_entity.id = r.parent_id AND r.depth < :offset" +
            ")" +
            "SELECT * FROM r;")
    Flux<NodeEntity> findContentParents(@Param("id") Long id, @Param("offset") Long offset);
}
