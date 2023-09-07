package io.keepup.cms.core.service;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.repository.ReactiveContentClassRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.exception.EntityValidationException;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "h2"})
@ContextConfiguration(classes = {
        KeepupApplication.class,
        KeepupCacheConfiguration.class,
        CacheAdapter.class,
        WebFluxConfig.class,
        ReactiveNodeEntityRepository.class,
        ReactiveNodeAttributeEntityRepository.class,
        ReactiveUserEntityRepository.class,
        DataSourceConfiguration.class,
        R2dbcConfiguration.class,
        SqlContentDao.class,
        SqlFileDao.class,
        SqlUserDao.class,
        DataSourceFacadeImpl.class,
        TestAbstractEntityOperationService.class,
        TestNotSerializableAttributesAbstractEntityOperationService.class,
        TestAbstractEntityWithoutDefaultConstructorOperationService.class,
        AbstractEntityWithFinalFieldOperationService.class,
        TestAbstractEntityInterfaceService.class
})
@DataR2dbcTest
class AbstractEntityOperationServiceTest {
    @Autowired
    TestAbstractEntityOperationService entityOperationService;
    @Autowired
    TestNotSerializableAttributesAbstractEntityOperationService notSerializableAttributesEntityOperationService;
    @Autowired
    TestAbstractEntityWithoutDefaultConstructorOperationService entityWithoutDefaultConstructorOperationService;
    @Autowired
    AbstractEntityWithFinalFieldOperationService entityWithFinalFieldOperationService;
    @Autowired
    TestAbstractEntityInterfaceService testEntityInterfaceService;
    @Autowired
    DataSourceFacade dataSourceFacade;
    @Autowired
    ReactiveContentClassRepository contentClassRepository;

    @Test
    void get() {
        var savedRef = new AtomicReference<TestEntity>();
        var updatedRef = new AtomicReference<TestEntity>();
        var example = new TestEntity();
        example.setTestId(null);
        example.setSomeValue("some value");
        final var testObject = entityOperationService.save(example, 0L)
                .flatMap(saved -> {
                    savedRef.set(saved);
                    return entityOperationService.save(saved, 0L);
                })
                .flatMap(updated -> {
                    updatedRef.set(updated);
                    return entityOperationService.get(savedRef.get().getTestId());
                }).block();

        assertNotNull(testObject);
        assertEquals(savedRef.get().getSomeValue(), testObject.getSomeValue());
        assertEquals(savedRef.get().getTestId(), testObject.getTestId());
        assertEquals(savedRef.get(), updatedRef.get());
    }

    @Test
    void getWithNullId() {
        assertNull(entityOperationService.get(null).block());
    }

    @Test
    void getForNotSerializableObjectAndAttribute() {
        var example = new TestNotSerializableEntity();
        example.setValue(new NotSerializableStringWrapper());
        example.getValue().setValue("some_string");

        final var saved = notSerializableAttributesEntityOperationService.save(example, 0L).block();
        final var testObject = notSerializableAttributesEntityOperationService.get(saved.getTestId()).block();
        assertNotNull(testObject);
        assertEquals(saved.getValue().getValue(), testObject.getValue().getValue());
        assertEquals(saved.getTestId(), testObject.getTestId());
    }

    @Test
    void saveNullObject() {
        assertNull(entityOperationService.save(null, 0).block());
    }

    /**
     * Add 2 different objects with the same parent id and then fetch by calling getAll by two
     * entity services.
     */
    @Test
    void getAll() {
        var record = new Node();
        record.setParentId(0L);
        record.setOwnerId(0L);
        record.setDefaultPrivileges();
        record.setId(dataSourceFacade.createContent(record).block());

        entityOperationService.setEntityParentIds(Collections.singletonList(record.getId()));
        notSerializableAttributesEntityOperationService.setEntityParentIds(Collections.singletonList(record.getId()));

        var testNotSerializableEntity = new TestNotSerializableEntity();
        testNotSerializableEntity.setValue(new NotSerializableStringWrapper());
        testNotSerializableEntity.getValue().setValue("another_value");
        var testEntity = new TestEntity();
        testEntity.setSomeValue("test");

        var result = entityOperationService.save(testEntity, 0L).block();
        var notSerializableResult = notSerializableAttributesEntityOperationService.save(testNotSerializableEntity, 0L).block();

        List<TestEntity> testResults = entityOperationService.getAll().collect(Collectors.toList()).block();
        List<TestNotSerializableEntity> notSerializableResults = notSerializableAttributesEntityOperationService.getAll().collect(Collectors.toList()).block();

        // region assert
        assertFalse(testResults.isEmpty());
        assertFalse(notSerializableResults.isEmpty());

        assertEquals(1, testResults.size());
        assertEquals(1, notSerializableResults.size());

        assertEquals(result.getSomeValue(), testResults.get(0).getSomeValue());
        assertEquals(notSerializableResult.getValue().getValue(), notSerializableResults.get(0).getValue().getValue());
        // endregion
    }

    @Test
    void testCannotCreateEntityWithoutDefaultConstructor() {
        TestEntityWithoutDefaultConstructor entity = new TestEntityWithoutDefaultConstructor(46L);
        var result = entityWithoutDefaultConstructorOperationService.save(entity, 0L).block();
        assertNull(result);
    }

    @Test
    void delete() {
        var example = new TestEntity();
        example.setSomeValue("value");
        final var saved = entityOperationService.save(example, 0L).block();
        entityOperationService.delete(saved.getTestId()).block();

        assertNull(entityOperationService.get(saved.getTestId()).block());
    }

    @Test
    void copy() {
        var example = new TestEntity();
        example.setSomeValue("value");
        final var saved = entityOperationService.save(example, 0L).block();
        Long copyId = entityOperationService.copy(saved.getTestId()).block();
        TestEntity copy = entityOperationService.get(copyId).block();
        assertNotNull(copy);
        assertEquals(saved.getSomeValue(), copy.getSomeValue());
    }

    @Test
    void getEntityParentIds() {
        var record0 = getNode();
        var record1 = getNode();
        var testEntity0 = new TestEntity();
        var testEntity1 = new TestEntity();

        dataSourceFacade.createContent(record0).map(id -> {record0.setId(id); return record0;}).block();
        dataSourceFacade.createContent(record1).map(id -> {record1.setId(id); return record1;}).block();

        entityOperationService.setEntityParentIds(Collections.singletonList(record0.getId()));
        entityOperationService.save(testEntity0, 0L).block();
        entityOperationService.setEntityParentIds(Collections.singletonList(record1.getId()));
        testEntity1 = entityOperationService.save(testEntity1, 0L).block();

        var entitiesBySecondParentId = entityOperationService.getAll().collect(Collectors.toList()).block();

        entityOperationService.setEntityParentIds(Arrays.asList(record0.getId(), record1.getId()));

        var allEntities = entityOperationService.getAll().collect(Collectors.toList()).block();

        // region assert
        assertEquals(1, entitiesBySecondParentId.size());
        assertEquals(testEntity1.getTestId(), entitiesBySecondParentId.get(0).getTestId());
        assertEquals(2, allEntities.size());
        assertEquals(entityOperationService.getEntityParentIds(), Arrays.asList(record0.getId(), record1.getId()));
        // endregion

    }

    @Test
    void wontFindWrongEntityType() {
        var testEntity = new TestEntity();
        testEntity.setSomeValue("some_value");
        testEntity = entityOperationService.save(testEntity, 0L).block();

        TestEntity anotherType = dataSourceFacade.getContent(testEntity.getTestId())
                .flatMap(content -> {
                    content.setEntityType("anotherType");
                    return dataSourceFacade.createContent(content);
                })
                .flatMap(contentId -> entityOperationService.get(contentId)).block();
        assertNull(anotherType);
    }

    @Test
    void canGetRightTypeWithWrongParameterMap() {
        var testEntity = new TestEntity();
        testEntity.setSomeValue("some_value");
        testEntity = entityOperationService.save(testEntity, 0L).block();

        TestEntity anotherType = dataSourceFacade.getContent(testEntity.getTestId())
                .flatMap(content -> {
                    content.getAttributes().remove("some_value");
                    content.addAttribute("wrong_attr", 123);
                    return dataSourceFacade.createContent(content);
                })
                .flatMap(contentId -> entityOperationService.get(contentId)).block();
        assertNotNull(anotherType);
    }

    @Test
    void canGetRightTypeWithWrongParameterType() {
        var testEntity = new TestEntity();
        testEntity.setSomeValue("some_value");
        testEntity = entityOperationService.save(testEntity, 0L).block();

        TestEntity anotherType = dataSourceFacade.getContent(testEntity.getTestId())
                .flatMap(content -> {
                    content.addAttribute("some_value", new TestEntity());
                    return dataSourceFacade.createContent(content);
                })
                .flatMap(contentId -> entityOperationService.get(contentId)).block();

        // region assert
        assertNotNull(anotherType);
        assertNull(anotherType.getSomeValue());
        // endregion
    }

    @Test
    void cannotGetByBullParenIds() {
        final List<Long> entityParentIds = new ArrayList<>();
        var testEntity = new TestEntity();
        testEntity.setSomeValue("some_value");
        var result = entityOperationService.save(testEntity, 0L)
                .then(Mono.just(entityParentIds.addAll(entityOperationService.getEntityParentIds()))
                .then(getObjectMono())
                .then(entityOperationService.getAll().collect(Collectors.toList()))).block();
        entityOperationService.setEntityParentIds(entityParentIds);

        assertTrue(result.isEmpty());
    }

    @Test
    void wontSetFinalFieldValue() {
        SecurityManager securityManager = System.getSecurityManager();

        var entity = new TestEntityWithFinalField();
        TestEntityWithFinalField saved = entityWithFinalFieldOperationService.save(entity, 0L)
                .flatMap(savedEntity -> dataSourceFacade.getContent(savedEntity.getTestId()))
                .flatMap(content -> dataSourceFacade.updateContentAttribute(content.getId(), "some_value", "changedValue").thenReturn(
                        content.getId()))
                .flatMap(value -> {
                    System.setSecurityManager(new TestSecurityManager());
                    return entityWithFinalFieldOperationService.get(value);})
                .block();

        System.setSecurityManager(securityManager);

        assertNotNull(saved);
    }

    @Test
    void convertWithNullEntityType() {
        Content content = getNode();
        content.setEntityType(null);
        assertThrows(RuntimeException.class, () -> entityOperationService.convert(content).block());
    }

    @Test
    void convertWithWrongClassNameInEntityType() {
        Throwable exception = null;
        Content content = getNode();
        content.setEntityType("WrongClassName");
        try {
            entityOperationService.convert(content).block();
        } catch (Throwable e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(ClassNotFoundException.class, exception.getCause().getClass());
    }

    @Test
    void convertWithAnotherClassNameInEntityType() {
        Throwable exception = null;
        Content content = getNode();
        content.setEntityType("io.keepup.cms.core.service.TestNotSerializableEntity");
        try {
            entityOperationService.convert(content).block();
        } catch (Throwable e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(ClassCastException.class, exception.getClass());
    }

    @Test
    void saveAndGetByInterfaceGenerifiedService() {
        TestEntityInterface testEntity = new TestEntityInterfaceImpl();
        var newEntity = testEntityInterfaceService.save(testEntity, 0L)
                .flatMap(savedEntity -> testEntityInterfaceService.get(savedEntity.getId()))
                .block();
        assertNotNull(newEntity);
        assertNull(newEntity.getName());
        assertNotNull(newEntity.getId());
    }

    @Test
    void saveAndGetByInterfaceGenerifiedServiceWithoutDefinedInterface() {
        TestEntityInterface testEntity = new TestEntityInterfaceImpl();
        final AtomicLong id = new AtomicLong();
        var newEntity = testEntityInterfaceService.save(testEntity, 0L)
                .map(savedEntity -> {
                    id.set(savedEntity.getId());
                    return savedEntity.getId();
                })
                .then(contentClassRepository.deleteAll())
                .then(testEntityInterfaceService.get(id.get()))
                .block();
        assertNull(newEntity);
    }

    @Test
    void saveEntityByNullIdAndReceiveNull() {
        assertNull(testEntityInterfaceService.save(null, 0L).block());
    }

    @Test
    void saveEntityByNullIdAndReceiveError() {
        assertThrows(EntityValidationException.class, () -> testEntityInterfaceService.save(new TestEntityInterfaceImpl(), 0L, null).block());
    }

    @NotNull
    private Mono<Object> getObjectMono() {
        entityOperationService.setEntityParentIds(null);
        return Mono.empty();
    }

    @NotNull
    private Node getNode() {
        var record = new Node();
        record.setParentId(0L);
        record.setOwnerId(0L);
        record.setDefaultPrivileges();
        record.setId(dataSourceFacade.createContent(record).block());
        return record;
    }
}