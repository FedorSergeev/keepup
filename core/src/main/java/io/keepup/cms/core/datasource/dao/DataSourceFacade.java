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
 */
public interface DataSourceFacade {
    // region Content operations
    Mono<Content> getContent(Long id);
    Mono<Content> getContentByIdAndType(Long id, String type);
    Flux<Content> getContentByIdWithChildren(Long id);
    Flux<Content> getContent();
    Mono<Map<String, Serializable>> updateContent(Long id, Map<String, Serializable> newAttributes);
    Mono<Serializable> getContentAttribute(Long contentId, String attributeName);
    Mono<Serializable> updateContentAttribute(Long contentId, String attributeName, Serializable attributeValue);
    Flux<Content> getContentByParentIdAndByAttributeNames(Long parentId, List<String> attributeNames);
    Flux<Content> getContentByParentIdAndAttributeValue(Long parentId, String attributeName, Serializable attributeValue);
    Flux<Content> getContentByParentIds(Iterable <Long> parentIds);
    Flux<Content> getContentByParentIdsAndType(Iterable <Long> parentIds, String type);
    Flux<Content> getContentByParentId(Long parentId);
    Flux<Content> getContentParents(Long id, Long offsetId);
    Mono<Long> createContent(Content content);
    Mono<Void> deleteContent(Long id);
    // endregion

    // region File operations
    Mono<OutputStream> getFileAsStream(String fileName);
    Mono<FileWrapper> getFile(String filename);
    // endregion

    // region User operations
    Mono<User> createUser(User user);
    Mono<User> getUser(long userId);
    Flux<User> getUsers(Iterable<String> roles);
    Mono<Void> deleteUser(long id);
    Mono<UserDetails> getUserByName(String username);
    // endregion
}
