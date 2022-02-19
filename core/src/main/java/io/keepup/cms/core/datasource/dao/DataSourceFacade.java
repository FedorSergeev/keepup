package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.FileWrapper;
import io.keepup.cms.core.persistence.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface for the basic DAO entity.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public interface DataSourceFacade {
    // region Content operations
    /**
     * @param id record ID
     * @return   Mono with record specified by ID or empty.
     * @see ContentDao#getContent() 
     */
    Mono<Content> getContent(Long id);

    /**
     * @see ContentDao#getContentByIdAndType(Long, String) 
     * @param id item identifier
     * @param type item type
     * @return Publisher signaling when the satisfying record is found
     */
    Mono<Content> getContentByIdAndType(Long id, String type);

    /**
     * @see ContentDao#getContentByIdWithChildren(Long) 
     * @param id record id or parent id, can be null though it makes no sense as no records will be found
     * @return Stream publisher for found records
     */
    Flux<Content> getContentByIdWithChildren(Long id);

    /**
     * @see ContentDao#getContent() 
     * @return publisher that emits all the records
     */
    Flux<Content> getContent();

    /**
     * Update {@link Content} record.
     * 
     * @see ContentDao#updateContent(Long, Map)
     * @param id            record identifier
     * @param newAttributes list of attributes to be added or updated
     * @return result saved map of record attributes
     */
    Mono<Map<String, Serializable>> updateContent(Long id, Map<String, Serializable> newAttributes);

    /**
     * Get {@link Content} attribute.
     *
     * @see ContentDao#getContentAttribute(Long, String)
     * @param contentId record identifier
     * @param attributeName name of attribute to be fetched
     * @return publisher for requested attribute
     */
    Mono<Serializable> getContentAttribute(Long contentId, String attributeName);

    /**
     * Update {@link Content} attribute.
     *
     * @see ContentDao#updateContentAttribute(Long, String, Serializable)
     * @param contentId {@link Content} record attribute
     * @param attributeName name of the field to be updated
     * @param attributeValue value to update the field
     * @return Publisher for the updated attribute value
     */
    Mono<Serializable> updateContentAttribute(Long contentId, String attributeName, Serializable attributeValue);

    /**
     * Find {@link Content} records by parent identifier which have attribute names as specofoed.
     *
     * @see ContentDao#getContentByParentIdAndByAttributeNames(Long, List) 
     * @param parentId parent record identifier
     * @param attributeNames list of record field names
     * @return Flux with records meeting the criterion
     */
    Flux<Content> getContentByParentIdAndByAttributeNames(Long parentId, List<String> attributeNames);

    /**
     * Find all {@link Content} records by parent identifier and attribute value.
     *
     * @see ContentDao#getContentByParentIdAndAttributeValue(Long, String, Serializable)
     * @param parentId       parent record identifier
     * @param attributeName  record field name
     * @param attributeValue record field value
     * @return               reactive stream emitting {@link Content} records meeting the specified condition
     */
    Flux<Content> getContentByParentIdAndAttributeValue(Long parentId, String attributeName, Serializable attributeValue);

    /**
     * Get {@link Content} records by one of parent identifiers
     *
     * @see ContentDao#getContentByParentIds(Iterable)
     * @param parentIds parent record identifiers
     * @return publisher for {@link Content} records
     */
    Flux<Content> getContentByParentIds(Iterable <Long> parentIds);

    /**
     * Get {@link Content} records by parent identifier and by Java class.
     *
     * @see ContentDao#getContentByParentIdsAndType(Iterable, String)
     * @param parentIds parent record identifiers
     * @param type name of entity, can be the name of entity class
     * @return publisher for {@link Content} records
     */
    Flux<Content> getContentByParentIdsAndType(Iterable <Long> parentIds, String type);

    /**
     * Find {@link Content} record by parent identifier.
     * 
     * @see ContentDao#getContentByParentId(Long)
     * @param parentId parent record identifier
     * @return publisher for {@link Content} records
     */
    Flux<Content> getContentByParentId(Long parentId);

    /**
     * Get {@link Content} parent sequence.
     *
     * @see ContentDao#getContentParents(Long, Long)
     * @param id       parent record identifier, can not be null
     * @param offsetId number of parent records to get, will be set to {@link Long#MAX_VALUE} if null
     * @return         publisher for the parent records sequence
     */
    Flux<Content> getContentParents(Long id, Long offsetId);

    /**
     * Create new {@link Content} record.
     *
     * @see ContentDao#createContent(Content) 
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
    // endregion

    // region File operations
    /**
     * Get an output stream of bytes from file.
     *
     * @param fileName name of file
     * @return         an output stream of bytes
     */
    Mono<OutputStream> getFileAsStream(String fileName);

    /**
     * Get meta information about file.
     *
     * @param filename name of file
     * @return         meta information about file
     * @see FileWrapper
     */
    Mono<FileWrapper> getFile(String filename);
    // endregion

    // region User operations
    /**
     * Save user in data source.
     *
     * @param user user ti be saved
     * @return     saved user instance
     */
    Mono<User> createUser(User user);

    /**
     * Find user by his ID.
     *
     * @param userId user's primary identifier
     * @return       reactor.core.publisher.Mono emitting the found user
     */
    Mono<User> getUser(long userId);

    /**
     * Find all users by role.
     *
     * @param roles collection of role names
     * @return      reactive stream publisher emitting al the found users who have at least one of the specified roles
     */
    Flux<User> getUsers(Iterable<String> roles);

    /**
     * Delete user.
     *
     * @param id user's ID
     * @return   Publisher emitting {@link Void} when the user is deleted
     */
    Mono<Void> deleteUser(long id);

    /**
     * Find {@link UserDetails} object by username.
     *
     * @param username name of user.
     * @return         reactor.core.publisher.Mono emitting the user description
     */
    Mono<UserDetails> getUserByName(String username);
    // endregion
}
