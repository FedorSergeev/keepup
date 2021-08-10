package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.sql.H2ConsoleService;
import io.keepup.cms.core.datasource.sql.entity.FileEntity;
import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveFileRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.persistence.BasicEntity;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.FileWrapper;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.keepup.cms.core.datasource.access.ContentPrivilegesFactory.STANDARD_PRIVILEGES;
import static io.keepup.cms.core.datasource.sql.EntityUtils.convertToLocalDateViaInstant;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mostly all the tests are blocking, but that does not affect the logic being checked
 */
@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "h2"})
@ContextConfiguration(classes = {
        KeepupApplication.class,
        KeepupCacheConfiguration.class,
        CacheAdapter.class,
        WebFluxConfig.class,
        ReactiveNodeEntityRepository.class,
        ReactiveNodeAttributeEntityRepository.class,
        DataSourceConfiguration.class,
        H2ConsoleService.class,
        R2dbcConfiguration.class,
        SqlContentDao.class,
        SqlFileDao.class,
        DataSourceFacadeImpl.class
})
@DataR2dbcTest
class SqlDataSourceFacadeTest {

    private static final String FILE_NAME = "file_%s".formatted(UUID.randomUUID().toString());
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ReactiveNodeEntityRepository reactiveNodeEntityRepository;
    @Autowired
    ReactiveNodeAttributeEntityRepository reactiveNodeAttributeEntityRepository;
    @Autowired
    ReactiveFileRepository reactiveFileRepository;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    CacheAdapter cacheAdapter;
    @Autowired
    DataSourceFacade dataSourceFacade;

    @BeforeEach
    void setUp() {
        reactiveNodeEntityRepository.save(getNodeEntity());
    }

    @Test
    void getContent() {
        Node node = getNode();
        Mono<Content> contentMono = dataSourceFacade.createContent(node).flatMap(id -> dataSourceFacade.getContent(id));
        Content fromDatabase = contentMono.block();
        assertNotNull(fromDatabase);
        node.setId(fromDatabase.getId());
        Cache.ValueWrapper cachedContent = Optional.ofNullable(cacheManager)
                .map(manager -> manager.getCache("content"))
                .map(cache -> cache.get(fromDatabase.getId())).orElse(null);
        assertNotNull(cachedContent);
        assertNotNull(cachedContent.get());
        assertEquals(cachedContent.get(), fromDatabase);
        // remove enhanced attributes because they will not be equal - to be solved
        fromDatabase.getAttributes().remove("enhanced");
        node.getAttributes().remove("enhanced");
        assertEquals(fromDatabase, node);
        List<Content> storedContent = dataSourceFacade.getContent()
                .collect(Collectors.toList())
                .block()
                .stream()
                .filter(element -> Objects.equals(element.getId(), fromDatabase.getId()))
                .collect(Collectors.toList());

        assertFalse(storedContent.isEmpty());
    }

    @Test
    void getEmptyContent() {
        Mono<Content> content = dataSourceFacade.getContent(Long.MAX_VALUE);
        assertNull(content.block());
    }

    @Test
    void createContent() {
        Mono<Long> content = dataSourceFacade.createContent(getNode());
        assertNotNull(content.block());
    }

    @Test
    void updateContent() {
        Content node = getNode();
        Long contentId = dataSourceFacade.createContent(node)
                .flatMap(id -> dataSourceFacade.getContent(id))
                .map(BasicEntity::getId).block();
        node.setId(contentId);
        node.setAttribute("attributeToUpdate", 456);
        node.setAttribute("testAttr", "newTestValue");

        Map<String, Serializable> newAttributes = dataSourceFacade.updateContent(contentId, node.getAttributes())
                .block();
        assertEquals(456, newAttributes.get("attributeToUpdate"));
        assertEquals("newTestValue", newAttributes.get("testAttr"));
        assertEquals(456, cacheAdapter.getContent(contentId).get().getAttribute("attributeToUpdate"));
        assertEquals("newTestValue", cacheAdapter.getContent(contentId).get().getAttribute("testAttr"));
    }

    @Test
    void deleteContent() {
        final AtomicLong identifier = new AtomicLong();
        Content node = getNode();
        Content result = dataSourceFacade.createContent(node)
                .flatMap(id -> dataSourceFacade.getContent(id))
                .map(content -> {
                    identifier.set(content.getId());
                    return content.getId();
                })
                .map(id -> dataSourceFacade.deleteContent(id))
                .then(dataSourceFacade.getContent(identifier.get())).block();
        assertTrue(cacheAdapter.getContent(identifier.get()).isEmpty());
        assertNull(result);
    }

    @Test
    void getContentAttribute() {
        Content node = getNode();
        node.setAttribute("attributeToGet", "someValue");

        Serializable attributeToGet = dataSourceFacade.createContent(node)
                .flatMap(id -> dataSourceFacade.getContentAttribute(id, "attributeToGet"))
                .block();
        assertNotNull(attributeToGet);
        assertEquals("someValue", attributeToGet);
    }

    @Test
    void updateContentAttribute() {
        String attributeToUpdate = "attributeToUpdate";
        final AtomicLong identifier = new AtomicLong();
        Content node = getNode();
        node.setAttribute(attributeToUpdate, "someValue");
        Serializable updatedValue = dataSourceFacade.createContent(node)
                .flatMap(id -> {
                    identifier.set(id);
                    return dataSourceFacade.updateContentAttribute(id, attributeToUpdate, 123);
                })
                .block();
        Serializable value = dataSourceFacade.getContentAttribute(identifier.get(), attributeToUpdate).block();
        Serializable storedEntityAttribute = dataSourceFacade.getContent(identifier.get()).block().getAttribute(attributeToUpdate);

        Content updatedOneMoreTime = dataSourceFacade.updateContentAttribute(identifier.get(), "attributeToUpdate", 1234)
                .then(dataSourceFacade.getContent(identifier.get()))
                .block();

        assertNotNull(updatedValue);
        assertEquals(123, updatedValue);
        assertNotNull(value);
        assertEquals(123, value);
        assertNotNull(storedEntityAttribute);
        assertEquals(123, storedEntityAttribute);
        assertNotNull(updatedOneMoreTime.getAttribute(attributeToUpdate));
        assertEquals(1234, updatedOneMoreTime.getAttribute(attributeToUpdate));

    }

    @Test
    void getContentByAttributeNames() {
        var node0 = getNode();
        node0.setAttribute("key", "value_0");
        var node1 = getNode();
        node1.setAttribute("key", "value_1");

        node0.setId(dataSourceFacade.createContent(node0).block());
        node1.setId(dataSourceFacade.createContent(node1).block());

        List<Content> contentByKey = dataSourceFacade.getContentByParentIdAndByAttributeNames(node0.getParentId(), Arrays.asList("key"))
                                        .collect(Collectors.toList())
                                        .block();
        assertTrue(dataSourceFacade.getContentByParentIdAndByAttributeNames(null, null).collect(Collectors.toList()).block().isEmpty());
        assertFalse(contentByKey.isEmpty());
        assertEquals(2, contentByKey.stream().filter(element -> element.getAttribute("key") != null).count());
    }

    @Test
    void getContentByAttributeValue() {
        var attr = Long.toString(new Date().getTime()).concat(UUID.randomUUID().toString());
        var node0 = getNode();
        node0.setAttribute("key", "value_0");
        var node1 = getNode();
        node1.setAttribute("key", attr);

        node0.setId(dataSourceFacade.createContent(node0).block());
        node1.setId(dataSourceFacade.createContent(node1).block());

        List<Content> contentByKey = dataSourceFacade.getContentByParentIdAndAttributeValue(node0.getParentId(), "key", attr)
                .collect(Collectors.toList())
                .block();

        assertTrue(dataSourceFacade.getContentByParentIdAndAttributeValue(null, null, null).collect(Collectors.toList()).block().isEmpty());
        assertFalse(contentByKey.isEmpty());
        assertEquals(1, contentByKey.stream().filter(element -> element.getAttribute("key") != null).count());
    }

    @Test
    void getContentByParentId() {
        var node0 = getNode();
        Long contentId = dataSourceFacade.createContent(node0)
                .flatMap(id -> {
                    var node1 = getNode();
                    node1.setParentId(id);
                    return dataSourceFacade.createContent(node1);
                }).block();

        var result = dataSourceFacade.getContent(contentId)
                .flatMap(content -> dataSourceFacade.getContentByParentId(content.getParentId())
                                              .collect(Collectors.toList())).block();

        assertEquals(1, result.size());
        assertTrue(dataSourceFacade.getContentByParentId(null).collect(Collectors.toList()).block().isEmpty());
    }

    @Test
    void getContentByParentIds() {
        var node0 = getNode();
        Long contentId = dataSourceFacade.createContent(node0)
                .flatMap(id -> {
                    var node1 = getNode();
                    node1.setParentId(id);
                    return dataSourceFacade.createContent(node1);
                }).block();

        var result = dataSourceFacade.getContent(contentId)
                .flatMap(content -> dataSourceFacade.getContentByParentIds(Collections.singletonList(content.getParentId()))
                        .collect(Collectors.toList())).block();

        assertEquals(1, result.size());
        assertTrue(dataSourceFacade.getContentByParentIds(null).collect(Collectors.toList()).block().isEmpty());
    }

    @Test
    void getFileAsStream() throws IOException {

        Long contentId = dataSourceFacade.createContent(getNode()).block();
        var testOutputStream = dataSourceFacade.getFileAsStream("test").block();

        File existingFile = new File("existing");
        existingFile.createNewFile();
        FileUtils.writeStringToFile(existingFile, "some content", UTF_8);
        FileEntity existingFileEntity = new FileEntity();
        existingFileEntity.setId(null);
        existingFileEntity.setFileName("existing");
        existingFileEntity.setPath("/app/files");
        existingFileEntity.setContentId(contentId);
        existingFileEntity.setCreationTime(convertToLocalDateViaInstant(new Date()));
        existingFileEntity.setModificationTime(convertToLocalDateViaInstant(new Date()));
        existingFileEntity.setContent(FileUtils.readFileToByteArray(existingFile));
        FileEntity savedExistingFileEntity = reactiveFileRepository.save(existingFileEntity).block();
        FileUtils.writeByteArrayToFile(existingFile, savedExistingFileEntity.getContent(), false);
        String result = FileUtils.readFileToString(existingFile, UTF_8);

        var existingFromDb = dataSourceFacade.getFileAsStream("existing").block();

        assertNull(testOutputStream);
        assertNotNull(savedExistingFileEntity);
        assertNotNull(result);
        assertNotNull(existingFromDb);
        assertEquals("some content", ((ByteArrayOutputStream) existingFromDb).toString(UTF_8));
        assertEquals("some content", result);
    }

    @Test
    void getFileWrapper() throws IOException {
        String fileContent = "some content";
        File existingFile = new File(FILE_NAME);
        existingFile.createNewFile();
        FileUtils.writeStringToFile(existingFile, fileContent, UTF_8);
        Long contentId = dataSourceFacade.createContent(getNode()).block();
        FileEntity existingFileEntity = new FileEntity(FILE_NAME, "/app/files", contentId, convertToLocalDateViaInstant(new Date()), convertToLocalDateViaInstant(new Date()), FileUtils.readFileToByteArray(existingFile));
        reactiveFileRepository.save(existingFileEntity).block();
        FileUtils.writeStringToFile(existingFile, fileContent, UTF_8);
        FileWrapper existingFileWrapper = dataSourceFacade.getFile(FILE_NAME).block();
        assertEquals(existingFile.getName(), existingFileWrapper.getName());
        assertTrue(existingFileWrapper.isExists());
        assertNotNull(existingFileWrapper.getPath());
        assertEquals(fileContent, existingFileWrapper.getContent().toString());
    }

    private Node getNode() {
        var node =  new Node();
        node.setId(25L);
        node.setOwnerId(1L);
        node.setParentId(0L);
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
        nodeEntity.setOwnerId(2L);
        nodeEntity.setParentId(0L);
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
        nodeAttributeEntity_0.setCreationTime(getTime());
        nodeAttributeEntity_0.setCreationTime(getTime());

        NodeAttributeEntity nodeAttributeEntity_1 = new NodeAttributeEntity();
        nodeAttributeEntity_1.setAttributeKey("testList");
        nodeAttributeEntity_1.setAttributeValue(nodeAttributeEntity_1.toByteArray(Arrays.asList(1, 2, 3)));
        nodeAttributeEntity_1.setJavaClass(String.class.getName());
        nodeAttributeEntity_1.setCreationTime(getTime());

        NodeAttributeEntity nodeAttributeEntity_2 = new NodeAttributeEntity(1L, "nullValue", null);

        NodeAttributeEntity nodeAttributeEntity_3 = new NodeAttributeEntity(3L, 1L, "notNullValue", "notNullValue");

        List<NodeAttributeEntity> nodeAttributeEntities = Arrays.asList(nodeAttributeEntity_0, nodeAttributeEntity_2, nodeAttributeEntity_2, nodeAttributeEntity_3);
        nodeAttributeEntities.forEach(nodeAttributeEntity -> log.info("Node entity: %s".formatted(nodeAttributeEntity.toString())));

        reactiveNodeAttributeEntityRepository.saveAll(nodeAttributeEntities);
        return nodeEntity;
    }

    private LocalDate getTime() {
        return new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

}