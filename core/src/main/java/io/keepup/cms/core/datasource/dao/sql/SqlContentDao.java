package io.keepup.cms.core.datasource.dao.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.datasource.access.ContentPrivileges;
import io.keepup.cms.core.datasource.access.Privilege;
import io.keepup.cms.core.datasource.dao.ContentDao;
import io.keepup.cms.core.datasource.sql.EntityUtils;
import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.keepup.cms.core.cache.CacheNames.CONTENT_CACHE_NAME;
import static io.keepup.cms.core.datasource.sql.EntityUtils.convertToLocalDateViaInstant;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static reactor.core.publisher.Mono.empty;

@Service
public class SqlContentDao implements ContentDao {

    public static final String PARENT_ID_PARAMETER = "parentId";

    private final Log log = LogFactory.getLog(getClass());
    private final ReactiveNodeEntityRepository nodeEntityRepository;
    private final ReactiveNodeAttributeEntityRepository nodeAttributeEntityRepository;

    private final ObjectMapper mapper;
    private final CacheManager cacheManager;
    private final CacheAdapter cacheAdapter;

    @Autowired
    public SqlContentDao(ReactiveNodeEntityRepository reactiveNodeEntityRepository,
                               ReactiveNodeAttributeEntityRepository reactiveNodeAttributeEntityRepository,
                               ObjectMapper objectMapper,
                               CacheManager manager,
                               CacheAdapter adapter) {
        nodeEntityRepository = reactiveNodeEntityRepository;
        nodeAttributeEntityRepository = reactiveNodeAttributeEntityRepository;
        mapper = objectMapper;
        cacheManager = manager;
        cacheAdapter = adapter;
    }

    // region public API
    /**
     * Finds {@link Content} record and returns as it is ready
     *
     * @param id record identifier
     * @return record by identifier
     */
    @Override
    public Mono<Content> getContent(final Long id) {
        if (id == null) {
            return empty();
        }
        var valueWrapper = ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .map(cache -> cache.get(id))
                .orElse(null);
        if (valueWrapper != null) {
            return ofNullable(valueWrapper.get())
                    .map(o -> Mono.just((Content) o))
                    .orElse(empty());
        }

        final List<NodeAttributeEntity> attributeEntities = new ArrayList<>();
        return nodeAttributeEntityRepository.findAllByContentId(id)
                .collect(Collectors.toList())
                .flatMap(attributes -> {
                    attributeEntities.addAll(attributes);
                    return nodeEntityRepository.findById(id);
                })
                .map(entity -> buildNode(entity, attributeEntities));
    }

    /**
     * Looks up for the record and filters it by type
     *
     * @param id item identifier
     * @param type item type
     * @return Publisher signaling when the satisfying record is found
     */
    @Override
    public Mono<Content> getContentByIdAndType(Long id, String type) {
        if (id == null) {
            return empty();
        }
        var valueWrapper = ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .map(cache -> cache.get(id))
                .orElse(null);
        if (valueWrapper != null) {
            return ofNullable(valueWrapper.get())
                    .map(o -> Mono.just((Content) o))
                    .orElse(empty());
        }

        final List<NodeAttributeEntity> attributeEntities = new ArrayList<>();
        return nodeAttributeEntityRepository.findAllByContentId(id)
                .collectList()
                .flatMap(attributes -> {
                    attributeEntities.addAll(attributes);
                    return nodeEntityRepository.findByIdAndType(id, type);
                })
                .map(entity -> buildNode(entity, attributeEntities));
    }

    /**
     * Looks for {@link Content} node with the specified identifier and for it's children
     *
     * @param id node identifier
     * @return Publisher signaling when objects specified by id or parent id found
     */
    public Flux<Content> getContentByIdWithChildren(Long id) {
        return nodeEntityRepository.findByIdOrByParentId(id)
                .flatMap(node ->
                        nodeAttributeEntityRepository.findAllByContentId(node.getId())
                                .collect(Collectors.toList())
                                .map(nodeAttributeEntities -> buildNode(node, nodeAttributeEntities)))
                .doOnNext(cacheAdapter::updateContent);
    }

    /**
     * Find all {@link Content} records from the data source
     *
     * @return publisher that emits all the records
     */
    @Override
    public Flux<Content> getContent() {
        return nodeEntityRepository.findAll()
                .flatMap(node ->
                        nodeAttributeEntityRepository.findAllByContentId(node.getId())
                                .collect(Collectors.toList())
                                .map(nodeAttributeEntities -> buildNode(node, nodeAttributeEntities)))
                .doOnNext(cacheAdapter::updateContent);
    }


    /**
     * Save the new {@link Content} record to database
     *
     * @param content local record instance
     * @return created record identifier
     */
    @Override
    public Mono<Long> createContent(final Content content) {
        AtomicReference<Long> contentId = new AtomicReference<>();
        if (content.getId() != null) {
            content.setId(null);
        }
        final var entity = new NodeEntity(content);
        return nodeEntityRepository.save(entity)
                .flatMap(saved -> getNodeAttributeEntityFlux(content, contentId, saved));
    }

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
    @Transactional
    public Mono<Map<String, Serializable>> updateContent(Long id, Map<String, Serializable> newAttributes) {
        final List<NodeAttributeEntity> nodeAttributeEntities = new ArrayList<>();
        final Map<String, Serializable> result = new HashMap<>();
        return nodeAttributeEntityRepository.findAllByContentId(id)
                .doOnNext(oldAttribute -> {
                    var attributeKey = oldAttribute.getAttributeKey();
                    var newValue = newAttributes.get(attributeKey);
                    if (newValue != null) {
                        nodeAttributeEntities.add(new NodeAttributeEntity(id, oldAttribute.getContentId(), attributeKey, newValue));
                        newAttributes.remove(attributeKey);
                    }
                })
                .collect(Collectors.toList())
                .map(attributes -> {
                    newAttributes.forEach((key, value) -> nodeAttributeEntities.add(new NodeAttributeEntity(id, key, value)));
                    return nodeAttributeEntities;
                })
                .then(nodeAttributeEntityRepository.saveAll(nodeAttributeEntities)
                        .collect(Collectors.toList()))
                .map(savedElements -> {
                    savedElements.forEach(element -> result.put(element.getAttributeKey(), getContentAttribute(element)));
                    return result;
                })
                .map(res -> {
                    cacheAdapter.getContent(id).ifPresent(content -> {
                        res.forEach(content::setAttribute);
                        cacheAdapter.updateContent(content);
                    });
                    return res;
                });
    }

    /**
     * Remove record from database with all its attributes
     *
     * @param id record identifier
     * @return actually nothing but you can synchronize further actions
     */
    @Override
    public Mono<Void> deleteContent(Long id) {
        return nodeAttributeEntityRepository
                .deleteByContentId(id)
                .then(doDeleteContent(id));
    }

    /**
     * Finds the {@link Content} attribute
     *
     * @param contentId record identifier
     * @param attributeName name of attribute to be fetched
     * @return publisher for requested attribute
     */
    @Override
    public Mono<Serializable> getContentAttribute(Long contentId, String attributeName) {
        return cacheAdapter.getContent(contentId).map(content -> content.getAttribute(attributeName)).map(Mono::just)
                .orElse(nodeAttributeEntityRepository.findByContentIdAndAttributeKey(contentId, attributeName)
                        .map(this::getContentAttribute)
                        .onErrorResume(throwable -> {
                            log.error("Error while getting Content attribute by name: " + throwable.toString());
                            return empty();
                        }));
    }

    /**
     * Put the new value for the {@link Content} record field according to it's name.
     *
     * @param contentId {@link Content} record attribute
     * @param attributeName name of the field to be updated
     * @param attributeValue value to update the field
     * @return Publisher for the updated attribute value
     */
    @Override
    public Mono<Serializable> updateContentAttribute(final Long contentId, final String attributeName, final Serializable attributeValue) {
        if (contentId == null) {
            log.error("Attempt to save attribute without Content record");
            return empty();
        }
        log.debug("Update content attribute: contentId = %d, attributeName = %s".formatted(contentId, attributeName));

        return nodeAttributeEntityRepository.findByContentIdAndAttributeKey(contentId, attributeName)
                .flatMap(nodeAttributeEntity -> saveContentAttribute(attributeName, attributeValue, nodeAttributeEntity));
    }

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
    @Override
    public Flux<Content> getContentByParentIdAndByAttributeNames(Long parentId, List<String> attributeNames) {
        if (attributeNames == null || parentId == null) {
            log.error("Null params passed to getContentByAttributeNames method: %s %s"
                    .formatted(getNullParameterName(parentId, PARENT_ID_PARAMETER), getNullParameterName(attributeNames, "attributeNames")));
            return Flux.empty();
        }

        return nodeAttributeEntityRepository.findAllByContentParentIdWithAttributeNames(parentId, attributeNames)
                .collect(Collectors.groupingBy(NodeAttributeEntity::getContentId, Collectors.toList()))
                .flatMapMany(attributesByContentId -> nodeEntityRepository.findByIds(attributesByContentId.keySet())
                                                                          .map(entity -> buildNode(entity, attributesByContentId.get(entity.getId()))))
                .map(cacheAdapter::updateContent);
    }

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
    @Override
    public Flux<Content> getContentByParentIdAndAttributeValue(Long parentId, String attributeName, Serializable attributeValue) {
        if (attributeName == null || attributeValue == null || parentId == null) {
            log.error("Null params passed to getContentByAttributeNames method: %s %s %s"
                    .formatted(getNullParameterName(parentId, PARENT_ID_PARAMETER),
                            getNullParameterName(attributeName, "attributeName"),
                            getNullParameterName(attributeValue, "attributeValue")));
            return Flux.empty();
        }
        return nodeAttributeEntityRepository.findAllByParentIdAndAttributeNameAndContentId(parentId, attributeName, EntityUtils.toByteArray(attributeValue))
                .collect(Collectors.groupingBy(NodeAttributeEntity::getContentId, Collectors.toList()))
                .flatMapMany(attributesByContentId -> {
                    if (attributesByContentId.isEmpty()) {
                        return Flux.empty();
                    }
                    return nodeEntityRepository.findByIds(attributesByContentId.keySet())
                                               .map(entity -> buildNode(entity, attributesByContentId.get(entity.getId())));
                })
                .map(cacheAdapter::updateContent);
    }

    /**
     * Finds and returns all {@link Content} records witch are children of records with the specified identifiers.
     * Result of the operation is being cached.
     *
     * @param parentIds parent record identifiers
     * @return publisher for {@link Content} records
     */
    @Override
    public Flux<Content> getContentByParentIds(Iterable <Long> parentIds) {
        if (parentIds == null) {
            log.error("Null parameter parentIds was passed to getContentByParentIds method");
            return Flux.empty();
        }
        return nodeEntityRepository.findByParentIds(parentIds)
                .flatMap(getNodeEntityPublisherFunction());
    }

    /**
     * Like getContentByParentIds method, finds and returns {@link Content} records that are children of records
     * with the specified identifiers, but also filters entities by type. Mostly used by custom user types serving.
     * Result of the operation is being cached.
     *
     * @param parentIds parent record identifiers
     * @param type name of entity, can be the name of entity class
     * @return publisher for {@link Content} records
     */
    @Override
    public Flux<Content> getContentByParentIdsAndType(Iterable <Long> parentIds, String type) {
        if (parentIds == null) {
            log.error("Null parameter parentIds was passed to getContentByParentIds method");
            return Flux.empty();
        }
        return nodeEntityRepository.findByParentIdsAndType(parentIds, type)
                .flatMap(getNodeEntityPublisherFunction());
    }

    /**
     * Finds and returns all {@link Content} records witch are children of record with the specified identifier.
     * Result of the operation is being cached. Difference between this method and getContentByParentIds is just in
     * signature as SQL query is the same
     *
     * @param parentId parent record identifier
     * @return publisher for {@link Content} records
     */
    @Override
    public Flux<Content> getContentByParentId(Long parentId) {
        if (parentId == null) {
            log.error("Null parameter parentId was passed to getContentByParentId method");
            return Flux.empty();
        }
        return nodeEntityRepository.findByParentIds(Collections.singletonList(parentId))
                .flatMap(getNodeEntityPublisherFunction());
    }

    /**
     * Recursively fetches all {@link Content} records until the root or the specified offset record is found
     *
     * @param id     first record identifier, in case of null empty Flux will be returned
     * @param offset depth of search, in case of null will be set to {@link Long#MAX_VALUE}
     * @return       publisher for the sequence of records inheriting each other till the record with the specified
     *               parent id (excluding this record itself)
     */
    @Override
    public Flux<Content> getContentParents(@NotNull Long id, @Nullable Long offset) {
        if (id == null) {
            log.error("Null parameter id was passed to getContentParents method");
            return Flux.empty();
        }
        if (offset == null) {
            offset = Long.MAX_VALUE;
        }
        return nodeEntityRepository.findContentParents(id, offset)
                .flatMap(getNodeEntityPublisherFunction());
    }

    // endregion

    @NotNull
    private Function<NodeEntity, Publisher<? extends Content>> getNodeEntityPublisherFunction() {
        return nodeEntity -> nodeAttributeEntityRepository.findAllByContentId(nodeEntity.getId())
                .collectList()
                .map(nodeAttributeEntities -> buildNode(nodeEntity, nodeAttributeEntities))
                .map(cacheAdapter::updateContent);
    }

    private Mono<Serializable> saveContentAttribute(String attributeName, Serializable attributeValue, NodeAttributeEntity nodeAttributeEntity) {
        nodeAttributeEntity.setModificationTime(convertToLocalDateViaInstant(new Date()));

        if (attributeValue != null) {
            try {
                nodeAttributeEntity.setAttributeValue(mapper.writeValueAsBytes(attributeValue));
                nodeAttributeEntity.setJavaClass(getValueTypeAsString(attributeValue));
            } catch (IOException ex) {
                log.error("Unable to convert attribute value o byte array: %s".formatted(ex.getMessage()));
                nodeAttributeEntity.setAttributeValue(new byte[0]);
                nodeAttributeEntity.setJavaClass(Byte.class.getName());
            }
        } else {
            nodeAttributeEntity.setAttributeValue(null);
            nodeAttributeEntity.setJavaClass(null);
        }
        return nodeAttributeEntityRepository.save(nodeAttributeEntity)
                .mapNotNull(updatedNodeAttributeEntity -> updateContentAttributeCache(attributeName, attributeValue, updatedNodeAttributeEntity));
    }

    @NotNull
    private String getValueTypeAsString(Serializable attributeValue) {
        return ofNullable(attributeValue)
                .map(Object::getClass)
                .map(Class::toString)
                .map(str -> str.substring(6))
                .orElse(EMPTY);
    }

    private Serializable updateContentAttributeCache(String attributeName, Serializable attributeValue, NodeAttributeEntity updatedNodeAttributeEntity) {
        log.debug("Node attribute updated: %s".formatted(updatedNodeAttributeEntity.toString()));
        cacheAdapter.updateContent(updatedNodeAttributeEntity.getContentId(), attributeName, attributeValue);
        return attributeValue;
    }

    @NotNull
    private Mono<Void> doDeleteContent(Long id) {
        cacheAdapter.deleteContent(id);
        return nodeEntityRepository.deleteById(id);
    }

    private Mono<Long> getNodeAttributeEntityFlux(Content content, AtomicReference<Long> contentId, NodeEntity saved) {
        contentId.set(saved.getId());
        Map<String, Serializable> contentAttributes = content.getAttributes();
        List<NodeAttributeEntity> nodeAttributes = contentAttributes.entrySet().stream()
                .map(entry -> new NodeAttributeEntity(saved.getId(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return nodeAttributeEntityRepository.saveAll(nodeAttributes)
                .then(Mono.just(saved.getId()));
    }

    /**
     * We should think about this approach a little bit more. The plan is to separate different
     * content types into different blocks and provide content like content.getStringAttribute(name)
     * or content.getIntegerAttribute(), but this approach leads to many requests or huge joins,
     * so for now we just serialize values from byte arrays.
     *
     * @param content             {@link Content} record
     * @param nodeAttributeEntity attribute entity to paste
     */
    private void addNodeAttributeToContent(Content content, NodeAttributeEntity nodeAttributeEntity) {
        content.setAttribute(nodeAttributeEntity.getAttributeKey(), getContentAttribute(nodeAttributeEntity));
    }

    private Serializable getContentAttribute(NodeAttributeEntity nodeAttributeEntity) {
        if (nodeAttributeEntity.getJavaClass() == null || nodeAttributeEntity.getAttributeValue() == null) {
            return null;
        }
        try {
            Class<?> attributeType = Class.forName(nodeAttributeEntity.getJavaClass());
            if (List.class.isAssignableFrom(attributeType) || nodeAttributeEntity.getJavaClass().contains("$ArrayList")) {
                nodeAttributeEntity.setJavaClass(ArrayList.class.getName());
            }
            return (Serializable) mapper.readValue(nodeAttributeEntity.getAttributeValue(), attributeType);
        } catch (IOException ex) {
            log.error("Failed to serialize value from persistent content: %s".formatted(ex));
        } catch (ClassNotFoundException e) {
            log.error("Class %s not found in classpath: %s".formatted(nodeAttributeEntity.getJavaClass(), e.getMessage()));
        }
        return null;
    }

    Content buildNode(NodeEntity nodeEntity, List<NodeAttributeEntity> attributeEntities) {
        // as we call this method from reactive chain there is no success result with null NodeEntity
        final Content content = new Node(nodeEntity.getId());
        content.setEntityType(nodeEntity.getEntityType());
        content.setParentId(nodeEntity.getParentId());
        content.setOwnerId(nodeEntity.getOwnerId());
        content.setContentPrivileges(new ContentPrivileges());
        setOwnerPrivileges(nodeEntity, content);
        setOtherPrivileges(nodeEntity, content);
        setRolePrivileges(nodeEntity, content);
        saveContentToCache(content);

        attributeEntities.forEach(nodeAttributeEntity -> addNodeAttributeToContent(content, nodeAttributeEntity));
        return content;
    }

    /**
     * One decided not use cache via AOP for two reasons:
     * - reactive streaming and possible problems with caching
     * - additional proxy configurations for intercepting methods and classes annotated for caching
     *
     * @param content record to be cached
     */
    private void saveContentToCache(Content content) {
        ofNullable(cacheManager)
                .map(manager -> manager.getCache(CONTENT_CACHE_NAME))
                .ifPresent(cache -> cache.putIfAbsent(content.getId(), content));
    }

    private void setOwnerPrivileges(NodeEntity nodeEntity, Content content) {
        var ownerPrivileges = new Privilege();
        ownerPrivileges.setRead(nodeEntity.isOwnerReadPrivilege());
        ownerPrivileges.setWrite(nodeEntity.isOwnerWritePrivilege());
        ownerPrivileges.setExecute(nodeEntity.isOwnerExecutePrivilege());
        ownerPrivileges.setCreateChildren(nodeEntity.isOwnerCreateChildrenPrivilege());
        content.getContentPrivileges().setOwnerPrivileges(ownerPrivileges);
    }

    private void setOtherPrivileges(NodeEntity nodeEntity, Content content) {
        var otherPrivileges = new Privilege();
        otherPrivileges.setRead(nodeEntity.isOtherReadPrivilege());
        otherPrivileges.setWrite(nodeEntity.isOtherWritePrivilege());
        otherPrivileges.setExecute(nodeEntity.isOtherExecutePrivilege());
        otherPrivileges.setCreateChildren(nodeEntity.isOtherCreateChildrenPrivilege());
        content.getContentPrivileges().setOtherPrivileges(otherPrivileges);
    }

    private void setRolePrivileges(NodeEntity nodeEntity, Content content) {
        var rolePrivileges = new Privilege();
        rolePrivileges.setRead(nodeEntity.isRoleReadPrivilege());
        rolePrivileges.setWrite(nodeEntity.isRoleWritePrivilege());
        rolePrivileges.setExecute(nodeEntity.isRoleExecutePrivilege());
        rolePrivileges.setCreateChildren(nodeEntity.isRoleCreateChildrenPrivilege());
        content.getContentPrivileges().setRolePrivileges(rolePrivileges);
    }

    /**
     * Get variable name if it is null for logging
     *
     * @param parameter parameter object
     * @param parameterName parameter object name in context
     * @return variable name in case it is null
     */
    private String getNullParameterName(Object parameter, String parameterName) {
        if (parameterName == null) {
            return "null";
        }
        return parameter == null
                ? parameterName
                : EMPTY;
    }
}
