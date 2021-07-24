package io.keepup.cms.core.datasource.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static io.keepup.cms.core.datasource.access.ContentPrivilegesFactory.STANDARD_PRIVILEGES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes = {KeepupApplication.class, KeepupCacheConfiguration.class})
@DataR2dbcTest
class SqlDataSourceTest {

    private final Log log = LogFactory.getLog(getClass());

    @Mock
    ReactiveNodeEntityRepository reactiveNodeEntityRepository;
    @Mock
    ReactiveNodeAttributeEntityRepository reactiveNodeAttributeEntityRepository;
    @Mock
    ObjectMapper objectMapper;
    @Autowired
    CacheManager cacheManager;

    DataSource dataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(getClass());
        dataSource = new SqlDataSource(reactiveNodeEntityRepository, reactiveNodeAttributeEntityRepository, objectMapper, cacheManager);
    }

    @Test
    void getContent() {
        Mockito.when(reactiveNodeEntityRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(getNodeEntity()));
        Mono<Content> content = dataSource.getContent(1L);
        assertNotNull(content.block());
        Cache.ValueWrapper content1 = cacheManager.getCache("content").get(content.block().getId());
        assertNotNull(content1);
    }

    @Test
    void getEmptyContent() {
        Mockito.when(reactiveNodeEntityRepository.findById(ArgumentMatchers.anyLong()))
               .thenReturn(Mono.empty());
        Mono<Content> content = dataSource.getContent(1L);
        assertNull(content.block());
    }

    @Test
    void createContent() {
        Mockito.when(reactiveNodeEntityRepository.save(ArgumentMatchers.any(NodeEntity.class)))
                .thenReturn(Mono.just(getNodeEntity()));
        Mockito.when(reactiveNodeAttributeEntityRepository.saveAll(ArgumentMatchers.anyCollection()))
               .thenReturn(Flux.just(new NodeAttributeEntity()));
        Mono<Long> content = dataSource.createContent(getNode());
        assertNotNull(content.block());
    }

    private Node getNode() {
        var node =  new Node();
        node.setId(25L);
        node.setOwnerId(1L);
        node.setDefaultPrivileges();
        node.setAttribute("testAttr", "testValue");
        log.info("Attribute value set: %s".formatted(node.getAttribute("testAttr")));

        var additionalAttributes = new HashMap<String, Serializable>();
        additionalAttributes.put("anotherKey", "anotherValue");
        node.addAttributes(additionalAttributes);
        assertEquals(2, node.getAttributes().size());

        node.addAttribute("integer", 1);
        assertEquals(3, node.getAttributes().size());
        assertTrue(node.hasAttribute("integer"));

        node.addAttribute("enhanced", new NotSerializable());
        assertEquals(4, node.getAttributes().size());
        return node;
    }

    private NodeEntity getNodeEntity() {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setId(new Random().nextLong());
        nodeEntity.setOwnerReadPrivilege(STANDARD_PRIVILEGES.getOwnerPrivileges().canRead());
        nodeEntity.setOwnerWritePrivilege(STANDARD_PRIVILEGES.getOwnerPrivileges().canWrite());
        nodeEntity.setOwnerCreateChildrenPrivilege(STANDARD_PRIVILEGES.getOwnerPrivileges().canCreateChildren());
        nodeEntity.setRoleReadPrivilege(STANDARD_PRIVILEGES.getRolePrivileges().canRead());
        nodeEntity.setRoleWritePrivilege(STANDARD_PRIVILEGES.getOtherPrivileges().canWrite());
        nodeEntity.setRoleCreateChildrenPrivilege(STANDARD_PRIVILEGES.getRolePrivileges().canCreateChildren());
        nodeEntity.setOtherReadPrivilege(STANDARD_PRIVILEGES.getOtherPrivileges().canRead());
        nodeEntity.setOtherWritePrivilege(STANDARD_PRIVILEGES.getOtherPrivileges().canWrite());
        nodeEntity.setOtherCreateChildrenPrivilege(STANDARD_PRIVILEGES.getOtherPrivileges().canCreateChildren());

        NodeAttributeEntity nodeAttributeEntity_0 = new NodeAttributeEntity();
        nodeAttributeEntity_0.setAttributeKey("testKey");
        nodeAttributeEntity_0.setAttributeValue("testValue".getBytes(UTF_8));
        nodeAttributeEntity_0.setJavaClass(String.class.getName());
        nodeAttributeEntity_0.setCreationTime(new Date());
        nodeEntity.getAttributes().add(nodeAttributeEntity_0);

        NodeAttributeEntity nodeAttributeEntity_1 = new NodeAttributeEntity();
        nodeAttributeEntity_1.setAttributeKey("testList");
        nodeAttributeEntity_1.setAttributeValue(nodeAttributeEntity_1.toByteArray(Arrays.asList(1, 2, 3)));
        nodeAttributeEntity_1.setJavaClass(String.class.getName());
        nodeAttributeEntity_1.setCreationTime(new Date());
        nodeEntity.getAttributes().add(nodeAttributeEntity_1);

        NodeAttributeEntity nodeAttributeEntity_2 = new NodeAttributeEntity(1L, "nullValue", null);
        nodeEntity.getAttributes().add(nodeAttributeEntity_2);

        NodeAttributeEntity nodeAttributeEntity_3 = new NodeAttributeEntity(3L, 1L, "notNullValue", "notNullValue");
        nodeEntity.getAttributes().add(nodeAttributeEntity_3);

        nodeEntity.getAttributes().forEach(nodeAttributeEntity -> log.info("Node entity: %s".formatted(nodeAttributeEntity.toString())));

        return nodeEntity;
    }

}