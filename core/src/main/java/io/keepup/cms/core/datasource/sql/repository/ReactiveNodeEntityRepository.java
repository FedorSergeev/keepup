package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive data access object for {@link NodeEntity} entities.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Repository
public interface ReactiveNodeEntityRepository extends ReactiveCrudRepository<NodeEntity, Long> {

    /**
     * Find all {@link NodeEntity} objects by primary identifiers
     * @param ids collection of node primary identifiers
     * @return    reactive stream publisher emitting all the found {@link NodeEntity} objects.
     */
    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
           "FROM node_entity as node WHERE node.id IN (:ids)")
    Flux<NodeEntity> findByIds(@Param("ids") Iterable<Long> ids);

    /**
     * Find all records by the parent id. Use 0 to get the list of root records.
     *
     * @param ids collection of parent node identifiers
     * @return    reactive stream publisher emitting all the found {@link NodeEntity} objects
     */
    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
            "FROM node_entity as node WHERE node.parent_id IN (:ids)")
    Flux<NodeEntity> findByParentIds(@Param("ids") Iterable<Long> ids);

    /**
     * Find all records by the parent id and entity type. Use 0 to get the list of root records.
     *
     * @param ids  collection of parent node identifiers
     * @param type entity type (Java class)
     * @return     reactive stream publisher emitting all the found {@link NodeEntity} objects
     */
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

    /**
     * Find all records by primary identifier and entity type.
     *
     * @param id   entity ID
     * @param type entity type (Java class)
     * @return     reactor.core.publisher.Mono emitting all the found {@link NodeEntity} objects
     */
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

    /**
     * FInd records by primary ID or parent record identifier.
     *
     * @param id primary identifier or parent node primary ID
     * @return   reactor.core.publisher.Mono emitting all the found {@link NodeEntity} objects
     */
    @Query("SELECT id, parent_id, owner_id, entity_type," +
            " owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege," +
            " role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege," +
            " other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
            "FROM node_entity as node WHERE node.id = :id OR node.parent_id = :id")
    Flux<NodeEntity> findByIdOrByParentId(@Param("id") Long id);

    /**
     * Fetches the sequence of parent records. Use this method only for PostgreSQL as a database.
     *
     * @param id     record identifier
     * @param offset number of parent records to fetch
     * @return       Flux that publishes found entity parents
     */
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
            "SELECT id, parent_id, owner_id, entity_type, " +
            "       owner_read_privilege, owner_write_privilege, owner_create_children_privilege, owner_execute_privilege, " +
            "       role_read_privilege, role_write_privilege, role_create_children_privilege, role_execute_privilege, " +
            "       other_read_privilege, other_write_privilege, other_create_children_privilege, other_execute_privilege " +
            "FROM r;")
    Flux<NodeEntity> findContentParents(@Param("id") Long id, @Param("offset") Long offset);
}
