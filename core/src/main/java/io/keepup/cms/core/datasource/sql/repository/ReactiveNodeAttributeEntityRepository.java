package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.persistence.Content;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * DAO for {@link io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity} objects
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Repository
public interface ReactiveNodeAttributeEntityRepository extends ReactiveCrudRepository<NodeAttributeEntity, Long> {

    /**
     * Find all attributes by {@link io.keepup.cms.core.datasource.sql.entity.NodeEntity} primary identifier.
     *
     * @param contentId content record identifier
     * @return          Publisher emitting found records which match the condition
     */
    Flux<NodeAttributeEntity> findAllByContentId(Long contentId);

    /**
     * Find all {@link NodeAttributeEntity} objects by {@link io.keepup.cms.core.datasource.sql.entity.NodeEntity} ID
     * and attribute key.
     *
     * @param contentId    {@link io.keepup.cms.core.datasource.sql.entity.NodeEntity} primary identifier
     * @param attributeKey name of attribute to be found
     * @return             reactor.core.publisher.Mono emitting the found attribute entity
     */
    Mono<NodeAttributeEntity> findByContentIdAndAttributeKey(Long contentId, String attributeKey);

    /**
     * Delete attribute entity specified by the ID.
     *
     * @param id attribute primary identifier
     * @return   Publisher emitting Void when operation is done
     */
    @Modifying
    Mono<Void> deleteByContentId(Long id);

    /**
     * Finds ALL node attributes for the records witch contain the specified attribute names.
     *
     * @param parentId {@link Content} parent id
     * @param attributeNames number of attribute names
     * @return all node attributes for the specified by condition {@link Content} nodes
     */
    @Query("SELECT id, content_id, attribute_key, attribute_value,  java_class " +
           "FROM node_attribute AS nodeAttribute " +
           "WHERE nodeAttribute.content_id " +
           "IN (SELECT content_id FROM node_attribute " +
           "   inner join node_entity on (node_attribute.content_id = node_entity.id " +
           "   and node_entity.parent_id = :contentParentId" +
           "   and node_attribute.attribute_key in (:attributeNames)))")
    Flux<NodeAttributeEntity> findAllByContentParentIdWithAttributeNames(@Param("contentParentId") Long parentId,
                                                                         @Param("attributeNames") List<String> attributeNames);

    /**
     * Finds ALL node attributes for the records witch contain the specified attribute name and value.
     *
     * @param parentId {@link Content} parent id
     * @param attributeName attribute field name
     * @param attributeValue attribute field value
     * @return all node attributes for the specified by condition {@link Content} nodes
     */
    @Query("SELECT id, content_id, attribute_key, attribute_value,  java_class " +
            "FROM node_attribute AS nodeAttribute " +
            "WHERE nodeAttribute.content_id " +
            "IN (SELECT content_id FROM node_attribute " +
            "    AS attribute  " +
            "    WHERE attribute.content_id " +
            "    IN (SELECT id FROM node_entity " +
            "        AS node " +
            "        WHERE node.parent_id = :parentId) " +
            "    AND attribute.attribute_key = :attributeName" +
            "    AND attribute.attribute_value = :attributeValue)")
    Flux<NodeAttributeEntity> findAllByParentIdAndAttributeNameAndContentId(@Param("parentId") Long parentId,
                                                                            @Param("attributeName") String attributeName,
                                                                            @Param("attributeValue")byte[] attributeValue);
}
