package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.FileWrapper;
import io.keepup.cms.core.persistence.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
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

    private final ContentDao contentDao;
    private final FileDao fileDao;
    private final UserDao userDao;

    @Autowired
    public DataSourceFacadeImpl(ContentDao contentDao, FileDao fileDao, UserDao userDao) {
        this.contentDao = contentDao;
        this.fileDao = fileDao;
        this.userDao = userDao;
    }

    @Override
    public Mono<Content> getContent(Long id) {
        return contentDao.getContent(id);
    }

    @Override
    public Mono<Content> getContentByIdAndType(Long id, String type) {
        return contentDao.getContentByIdAndType(id, type);
    }

    /**
     * Looks for {@link Content} records witch have id as primary identifier or identifier of parent record.
     *
     * @param id record id or parent id, can be null though it makes no sense as no records will be found
     * @return Stream publisher for found records
     */
    @Override
    public Flux<Content> getContentByIdWithChildren(Long id)  {
        return contentDao.getContentByIdWithChildren(id);
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
    public Flux<Content> getContentByParentIdsAndType(Iterable <Long> parentIds, String type) {
        return contentDao.getContentByParentIdsAndType(parentIds, type);
    }

    @Override
    public Flux<Content> getContentByParentId(Long parentId) {
        return contentDao.getContentByParentId(parentId);
    }

    /**
     * Fetch a sequence of parents for the record specified by identifier.
     *
     * @param id       parent record identifier, can not be null
     * @param offsetId number of parent records to get, will be set to {@link Long#MAX_VALUE} if null
     * @return         publisher for the parent records sequence
     */
    @Override
    public Flux<Content> getContentParents(@NotNull Long id, @Nullable Long offsetId) {
        return contentDao.getContentParents(id, offsetId);
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

    @Override
    public Mono<User> createUser(User user) {
        return userDao.saveUser(user);
    }

    @Override
    public Mono<User> getUser(long userId) {
        return userDao.getUser(userId);
    }

    @Override
    public Flux<User> getUsers(Iterable<String> roles) {
        return userDao.getUsers(roles);
    }

    @Override
    public Mono<Void> deleteUser(long id) {
        return userDao.deleteUser(id);
    }

    @Override
    public Mono<UserDetails> getUserByName(String username) {
        return userDao.getByName(username);
    }
}
