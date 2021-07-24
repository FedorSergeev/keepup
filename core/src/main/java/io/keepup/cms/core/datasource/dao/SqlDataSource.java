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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    public Mono<Content> getContent(Long id) {
        return nodeEntityRepository.findById(id)
                .map(this::buildNode);
    }


    @Override
    public Mono<Long> createContent(Content content) {
        AtomicReference<Long> contentId = new AtomicReference<>();
        if (content.getId() != null) {
            content.setId(null);
        }
        var entity = new NodeEntity(content, true);
        return nodeEntityRepository.save(entity)
                .map(saved -> getNodeAttributeEntityFlux(content, contentId, saved))
                .map(r -> contentId.get());
    }

    private Flux<NodeAttributeEntity> getNodeAttributeEntityFlux(Content content, AtomicReference<Long> contentId, NodeEntity saved) {
        contentId.set(saved.getId());
        Map<String, Serializable> contentAttributes = content.getAttributes();
        List<NodeAttributeEntity> nodeAttributes = contentAttributes.entrySet().stream()
                .map(entry -> new NodeAttributeEntity(saved.getId(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return nodeAttributeEntityRepository.saveAll(nodeAttributes);
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

    Content buildNode(NodeEntity nodeEntity) {
        // as we call this method from reactive chain there is no success result with null NodeEntity
        final Content content = new Node(nodeEntity.getId());
        content.setParentId(nodeEntity.getParentId());
        content.setOwnerId(nodeEntity.getOwnerId());
        content.setContentPrivileges(new ContentPrivileges());
        setOwnerPrivileges(nodeEntity, content);
        setOtherPrivileges(nodeEntity, content);
        setRolePrivileges(nodeEntity, content);
        nodeEntity.getAttributes().forEach(nodeAttributeDao -> addNodeAttributeToContent(content, nodeAttributeDao));
        Optional.ofNullable(cacheManager)
                .map(manager -> manager.getCache(CONTENT_CACHE_NAME))
                .ifPresent(cache -> cache.put(content.getId(), content));
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
