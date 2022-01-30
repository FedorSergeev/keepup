package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.Content;
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
 */
public interface ContentDao {
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

    /**
     * Fetch a sequence of parents for the record specified by identifier. In current realization works only with
     * PostgreSQL database as data source.
     *
     * @param id       child record identifier
     * @param offsetId number of parent records to get
     * @return         publisher for the parent records sequence
     */
    Flux<Content> getContentParents(Long id, Long offsetId);
    Mono<Long> createContent(Content content);
    Mono<Void> deleteContent(Long id);
}
