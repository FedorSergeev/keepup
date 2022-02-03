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
 */
@Repository
public interface ReactiveNodeAttributeEntityRepository extends ReactiveCrudRepository<NodeAttributeEntity, Long> {

    Flux<NodeAttributeEntity> findAllByContentId(Long contentId);

    Mono<NodeAttributeEntity> findByContentIdAndAttributeKey(Long contentId, String attributeKey);

    @Modifying
    Mono<Void> deleteByContentId(Long id);

    /**
     * Finds ALL node attributes for the records witch contain the specified attribute names
     * @param parentId {@link Content} parent id
     * @param attributeNames number of attribute names
     * @return all node attributes for the specified by condition {@link Content} nodes
     */
    @Query("SELECT * " +
           "FROM node_attribute AS nodeAttribute " +
           "WHERE nodeAttribute.content_id " +
           "IN (SELECT content_id FROM node_attribute " +
           "    AS attribute  " +
           "    WHERE attribute.content_id " +
           "    IN (SELECT id FROM node_entity " +
           "        AS node " +
           "        WHERE node.parent_id = :contentParentId) " +
           "    AND attribute.attribute_key in (:attributeNames))")
    Flux<NodeAttributeEntity> findAllByContentParentIdWithAttributeNames(@Param("contentParentId") Long parentId,
                                                                         @Param("attributeNames") List<String> attributeNames);

    /**
     * Finds ALL node attributes for the records witch contain the specified attribute name and value
     * @param parentId {@link Content} parent id
     * @param attributeName attribute field name
     * @param attributeValue attribute field value
     * @return all node attributes for the specified by condition {@link Content} nodes
     */
    @Query("SELECT * " +
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
