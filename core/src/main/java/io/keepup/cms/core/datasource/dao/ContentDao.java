package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Data access object for {@link io.keepup.cms.core.persistence.Content} entities
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 * @see Content
 */
public interface ContentDao {
    /**
     * Get publisher for {@link Content} record which will produce record specified by ID.
     *
     * @param id record ID
     * @return   Mono with record specified by ID or empty.
     */
    Mono<Content> getContent(Long id);

    /**
     * Looks up for the record and filters it by type
     *
     * @param id item identifier
     * @param type item type
     * @return Publisher signaling when the satisfying record is found
     */
    Mono<Content> getContentByIdAndType(Long id, String type);

    /**
     * Looks for {@link Content} node with the specified identifier and for it's children
     *
     * @param id node identifier
     * @return Publisher signaling when objects specified by id or parent id found
     */
    Flux<Content> getContentByIdWithChildren(Long id);

    /**
     * Find all {@link Content} records from the data source
     *
     * @return publisher that emits all the records
     */
    Flux<Content> getContent();

    /**
     * First takes persistent attributes and intersects it with new ones, then adds
     * new elements from argument map and save all elements, finally collects saved elements
     * to the result map of {@link Content} attributes.
     * <p>
     * Yet the logic is a little bit complicated while we should use KISS...
     *
     * @param id            record identifier
     * @param newAttributes list of attributes to be added or updated
     * @return result saved map of record attributes
     */
    Mono<Map<String, Serializable>> updateContent(Long id, Map<String, Serializable> newAttributes);

    /**
     * Finds the {@link Content} attribute
     *
     * @param contentId record identifier
     * @param attributeName name of attribute to be fetched
     * @return publisher for requested attribute
     */
    Mono<Serializable> getContentAttribute(Long contentId, String attributeName);

    /**
     * Put the new value for the {@link Content} record field according to it's name.
     *
     * @param contentId {@link Content} record attribute
     * @param attributeName name of the field to be updated
     * @param attributeValue value to update the field
     * @return Publisher for the updated attribute value
     */
    Mono<Serializable> updateContentAttribute(Long contentId, String attributeName, Serializable attributeValue);

    /**
     * Fetches reactive set of {@link Content} records with the same parent identifier and the same specified set of
     * attributes. These records can have also some additional fields, but the criteria is to have the number of
     * concrete fields.
     *
     * Please beware that the result of operation will be cached but we cannot look up for all the records in cache
     * as there can be new records in the data source witch were not put in cache.
     *
     * @param parentId parent record identifier
     * @param attributeNames list of record field names
     * @return Flux with records meeting the criterion
     */
    Flux<Content> getContentByParentIdAndByAttributeNames(Long parentId, List<String> attributeNames);

    /**
     * Finds all {@link Content} records witch have the specified by name and value attribute.
     *
     * Note that this operation is very expensive as we check attribute values equality byte per byte on
     * database side so we do not recommend to use him in really high-load workflows.
     *
     * Please beware that the result of operation will be cached but we cannot look up for all the records in cache
     * as there can be new records in the data source witch were not put in cache.
     *
     * @param parentId parent record identifier
     * @param attributeName record field name
     * @param attributeValue record field value
     * @return reactive sequence of {@link Content} records meeting the specified condition
     */
    Flux<Content> getContentByParentIdAndAttributeValue(Long parentId, String attributeName, Serializable attributeValue);

    /**
     * Finds and returns all {@link Content} records witch are children of records with the specified identifiers.
     * Result of the operation is being cached.
     *
     * @param parentIds parent record identifiers
     * @return publisher for {@link Content} records
     */
    Flux<Content> getContentByParentIds(Iterable <Long> parentIds);

    /**
     * Like getContentByParentIds method, finds and returns {@link Content} records that are children of records
     * with the specified identifiers, but also filters entities by type. Mostly used by custom user types serving.
     * Result of the operation is being cached.
     *
     * @param parentIds parent record identifiers
     * @param type name of entity, can be the name of entity class
     * @return publisher for {@link Content} records
     */
    Flux<Content> getContentByParentIdsAndType(Iterable <Long> parentIds, String type);

    /**
     * Finds and returns all {@link Content} records witch are children of record with the specified identifier.
     * Result of the operation is being cached. Difference between this method and getContentByParentIds is just in
     * signature as SQL query is the same
     *
     * @param parentId parent record identifier
     * @return publisher for {@link Content} records
     */
    Flux<Content> getContentByParentId(Long parentId);

    /**
     * Fetch a sequence of parents for the record specified by identifier. In current realization works only with
     * PostgreSQL database as data source.
     *
     * @param id       parent record identifier
     * @param offsetId number of parent records to get
     * @return         publisher for the parent records sequence
     */
    Flux<Content> getContentParents(@NotNull Long id, @Nullable Long offsetId);

    /**
     * Save the new {@link Content} record to database
     *
     * @param content local record instance
     * @return created record identifier
     */
    Mono<Long> createContent(Content content);

    /**
     * Remove record from database with all its attributes
     *
     * @param id record identifier
     * @return actually nothing but you can synchronize further actions
     */
    Mono<Void> deleteContent(Long id);
}
