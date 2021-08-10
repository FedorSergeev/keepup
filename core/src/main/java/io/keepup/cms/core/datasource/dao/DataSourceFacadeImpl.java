package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.FileWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Implementation of data source provider
 */
@Service
public class DataSourceFacadeImpl implements DataSourceFacade {

    private ContentDao contentDao;
    private FileDao fileDao;

    @Autowired
    public DataSourceFacadeImpl(ContentDao contentDao, FileDao fileDao) {
        this.contentDao = contentDao;
        this.fileDao = fileDao;
    }

    @Override
    public Mono<Content> getContent(Long id) {
        return contentDao.getContent(id);
    }

    @Override
    public Flux<Content> getContent() {
        return contentDao.getContent();
    }

    @Override
    public Mono<Map<String, Serializable>> updateContent(Long id, Map<String, Serializable> newAttributes) {
        return contentDao.updateContent(id, newAttributes);
    }

    @Override
    public Mono<Serializable> getContentAttribute(Long contentId, String attributeName) {
        return contentDao.getContentAttribute(contentId, attributeName);
    }

    @Override
    public Mono<Serializable> updateContentAttribute(Long contentId, String attributeName, Serializable attributeValue) {
        return contentDao.updateContentAttribute(contentId, attributeName, attributeValue);
    }

    @Override
    public Flux<Content> getContentByParentIdAndByAttributeNames(Long parentId, List<String> attributeNames) {
        return contentDao.getContentByParentIdAndByAttributeNames(parentId, attributeNames);
    }

    @Override
    public Flux<Content> getContentByParentIdAndAttributeValue(Long parentId, String attributeName, Serializable attributeValue) {
        return contentDao.getContentByParentIdAndAttributeValue(parentId, attributeName, attributeValue);
    }

    @Override
    public Flux<Content> getContentByParentIds(Iterable<Long> parentIds) {
        return contentDao.getContentByParentIds(parentIds);
    }

    @Override
    public Flux<Content> getContentByParentId(Long parentId) {
        return contentDao.getContentByParentId(parentId);
    }

    @Override
    public Mono<Long> createContent(Content content) {
        return contentDao.createContent(content);
    }

    @Override
    public Mono<Void> deleteContent(Long id) {
        return contentDao.deleteContent(id);
    }

    @Override
    public Mono<OutputStream> getFileAsStream(String fileName) {
        return fileDao.getFileAsStream(fileName);
    }

    @Override
    public Mono<FileWrapper> getFile(String filename) {
        return fileDao.getFile(filename);
    }
}