package io.keepup.cms.core.datasource.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.datasource.access.ContentPrivileges;
import io.keepup.cms.core.datasource.access.Privilege;
import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Abstract representation of the data source provider service.
 *
 * @author Fedor Sergeev
 */
public class SqlDataSource implements DataSource {

    public static final String CONTENT_CACHE_NAME = "content";
    private final Log log = LogFactory.getLog(getClass());
    private final ReactiveNodeEntityRepository nodeEntityRepository;
    private final ReactiveNodeAttributeEntityRepository nodeAttributeEntityRepository;
    private final ObjectMapper mapper;
    private final CacheManager cacheManager;

    @Autowired
    public SqlDataSource(ReactiveNodeEntityRepository reactiveNodeEntityRepository,
                         ReactiveNodeAttributeEntityRepository reactiveNodeAttributeEntityRepository,
                         ObjectMapper objectMapper,
                         CacheManager manager) {
        nodeEntityRepository = reactiveNodeEntityRepository;
        nodeAttributeEntityRepository = reactiveNodeAttributeEntityRepository;
        mapper = objectMapper;
        cacheManager = manager;
    }

    /**
     * Finds {@link Content} record and returns as it is ready
     *
     * @param id record identifier
     * @return record by identifier
     */
    @Override
    public Mono<Content> getContent(final Long id) {
        var valueWrapper = ofNullable(cacheManager.getCache(CONTENT_CACHE_NAME))
                .map(cache -> cache.get(id))
                .orElse(null);
        if (valueWrapper != null) {
            return ofNullable(valueWrapper.get())
                    .map(o -> Mono.just((Content)o))
                    .orElse(Mono.empty());
        }

        final List <NodeAttributeEntity> attributeEntities = new ArrayList<>();
        return nodeAttributeEntityRepository.findAllByContentId(id)
                .collect(Collectors.toList())
                .flatMap(attributes -> {
                    attributeEntities.addAll(attributes);
                    return nodeEntityRepository.findById(id);
                })
                .map(entity -> buildNode(entity, attributeEntities));
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
                            .map(nodeAttributeEntities -> buildNode(node, nodeAttributeEntities)));
    }


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
     * @param content          {@link Content} record
     * @param nodeAttributeDao attribute entity to paste
     */
    private void addNodeAttributeToContent(Content content, NodeAttributeEntity nodeAttributeDao) {
        try {
            Object newValue;
            Class<?> attributeType = Class.forName(nodeAttributeDao.getJavaClass());

            if (List.class.isAssignableFrom(attributeType) || nodeAttributeDao.getJavaClass().contains("$ArrayList")) {
                nodeAttributeDao.setJavaClass(ArrayList.class.getName());
            }
            newValue = mapper.readValue(nodeAttributeDao.getAttributeValue(), attributeType);
            content.setAttribute(nodeAttributeDao.getAttributeKey(), (Serializable) newValue);
        } catch (IOException ex) {
            log.error("Failed to serialize value from persistent content: %s".formatted(ex));
        } catch (ClassNotFoundException e) {
            log.error("Class %s not found in classpath: %s".formatted(nodeAttributeDao.getJavaClass(), e.getMessage()));
        }
    }

    @Cacheable
    Content buildNode(NodeEntity nodeEntity, List <NodeAttributeEntity> attributeEntities) {
        // as we call this method from reactive chain there is no success result with null NodeEntity
        final Content content = new Node(nodeEntity.getId());
        content.setParentId(nodeEntity.getParentId());
        content.setOwnerId(nodeEntity.getOwnerId());
        content.setContentPrivileges(new ContentPrivileges());
        setOwnerPrivileges(nodeEntity, content);
        setOtherPrivileges(nodeEntity, content);
        setRolePrivileges(nodeEntity, content);
        ofNullable(cacheManager)
                .map(manager -> manager.getCache(CONTENT_CACHE_NAME))
                .ifPresent(cache -> cache.putIfAbsent(content.getId(), content));

        attributeEntities.forEach(nodeAttributeEntity -> addNodeAttributeToContent(content, nodeAttributeEntity));
        return content;
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
}
