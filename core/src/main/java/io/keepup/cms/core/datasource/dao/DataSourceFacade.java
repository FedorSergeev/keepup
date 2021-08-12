package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.FileWrapper;
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
    Mono<Content> getContent(Long id);
    Flux<Content> getContent();
    Mono<Map<String, Serializable>> updateContent(Long id, Map<String, Serializable> newAttributes);
    Mono<Serializable> getContentAttribute(Long contentId, String attributeName);
    Mono<Serializable> updateContentAttribute(Long contentId, String attributeName, Serializable attributeValue);
    Flux<Content> getContentByParentIdAndByAttributeNames(Long parentId, List<String> attributeNames);
    Flux<Content> getContentByParentIdAndAttributeValue(Long parentId, String attributeName, Serializable attributeValue);
    Flux<Content> getContentByParentIds(Iterable <Long> parentIds);
    Flux<Content> getContentByParentId(Long parentId);
    Mono<Long> createContent(Content content);
    Mono<Void> deleteContent(Long id);
    Mono<OutputStream> getFileAsStream(String fileName);
    Mono<FileWrapper> getFile(String filename);

}
