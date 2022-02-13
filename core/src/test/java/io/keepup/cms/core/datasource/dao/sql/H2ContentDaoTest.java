package io.keepup.cms.core.datasource.dao.sql;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.sql.H2ConsoleService;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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
        H2ConsoleService.class,
        H2ConsoleService.class,
        R2dbcConfiguration.class,
        H2ContentDao.class,
})
@DataR2dbcTest
class H2ContentDaoTest {

    @Autowired
    H2ContentDao h2ContentDao;

    @Test
    void getContentParents() {
        h2ContentDao.getNodeAttributeEntityRepository().deleteAll()
                .then(h2ContentDao.getNodeEntityRepository().deleteAll())
                .then(h2ContentDao.createContent(createRecord(0L, Pair.of("key", "firstParent"))))
                .flatMap(grandParent -> h2ContentDao.createContent(createRecord(grandParent, Pair.of("key", "secondParent"))))
                .flatMap(parent -> h2ContentDao.createContent(createRecord(parent, Pair.of("key", "child"))))
                .flatMap(childId -> h2ContentDao.getContentParents(childId, 2L).collectList())
                .doOnNext(parents -> {
                    assertNotNull(parents);
                    assertFalse(parents.isEmpty());
                    assertEquals(2, parents.size());
                    assertEquals(parents.get(0).getParentId(), parents.get(1).getId());
                    assertEquals(0, parents.get(1).getParentId());
                })
                .block();
    }

    @Test
    void getContentParentsByNullId() {
        h2ContentDao.getNodeAttributeEntityRepository().deleteAll()
                .then(h2ContentDao.getNodeEntityRepository().deleteAll())
                .then(h2ContentDao.createContent(createRecord(0L, Pair.of("key", "firstParent"))))
                .flatMap(grandParent -> h2ContentDao.createContent(createRecord(grandParent, Pair.of("key", "secondParent"))))
                .flatMap(parent -> h2ContentDao.createContent(createRecord(parent, Pair.of("key", "child"))))
                .map(id -> {
                    try {
                        return h2ContentDao.getContentParents(null, 2L);
                    } catch (Throwable e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .doOnError(error ->
                    assertEquals(NullPointerException.class, error.getClass()))
                .onErrorReturn(Flux.error(new RuntimeException("error")))
                .block();
    }

    @Test
    void getContentParentsByNullOffset() {
        h2ContentDao.getNodeAttributeEntityRepository().deleteAll()
                .then(h2ContentDao.getNodeEntityRepository().deleteAll())
                .then(h2ContentDao.createContent(createRecord(0L, Pair.of("key", "firstParent"))))
                .flatMap(grandParent -> h2ContentDao.createContent(createRecord(grandParent, Pair.of("key", "secondParent"))))
                .flatMap(parent -> h2ContentDao.createContent(createRecord(parent, Pair.of("key", "child"))))
                .flatMap(childId -> h2ContentDao.getContentParents(childId, null).collectList())
                .doOnNext(parents -> {
                    assertNotNull(parents);
                    assertFalse(parents.isEmpty());
                    assertEquals(2, parents.size());
                    assertEquals(parents.get(0).getParentId(), parents.get(1).getId());
                    assertEquals(0, parents.get(1).getParentId());
                })
                .block();
    }

    @Test
    void getContentParentsByVeryLongOffset() {
        h2ContentDao.getNodeAttributeEntityRepository().deleteAll()
                .then(h2ContentDao.getNodeEntityRepository().deleteAll())
                .then(h2ContentDao.createContent(createRecord(0L, Pair.of("key", "firstParent"))))
                .flatMap(grandParent -> h2ContentDao.createContent(createRecord(grandParent, Pair.of("key", "secondParent"))))
                .flatMap(parent -> h2ContentDao.createContent(createRecord(parent, Pair.of("key", "child"))))
                .flatMap(childId -> h2ContentDao.getContentParents(childId, Long.MAX_VALUE).collectList())
                .doOnNext(parents -> {
                    assertNotNull(parents);
                    assertFalse(parents.isEmpty());
                    assertEquals(2, parents.size());
                    assertEquals(parents.get(0).getParentId(), parents.get(1).getId());
                    assertEquals(0, parents.get(1).getParentId());
                })
                .block();
    }

    @Test
    void getContentParentsByOffsetIsOne() {
        h2ContentDao.getNodeAttributeEntityRepository().deleteAll()
                .then(h2ContentDao.getNodeEntityRepository().deleteAll())
                .then(h2ContentDao.createContent(createRecord(0L, Pair.of("key", "firstParent"))))
                .flatMap(grandParent -> h2ContentDao.createContent(createRecord(grandParent, Pair.of("key", "secondParent"))))
                .flatMap(parent -> h2ContentDao.createContent(createRecord(parent, Pair.of("key", "child"))))
                .flatMap(childId -> h2ContentDao.getContentParents(childId, 1L).collectList())
                .doOnNext(parents -> {
                    assertNotNull(parents);
                    assertFalse(parents.isEmpty());
                    assertEquals(1, parents.size());
                })
                .block();
    }

    Content createRecord(long parentId, Pair<String, String> attribute) {
        Content node = new Node();
        node.setDefaultPrivileges();
        node.setParentId(parentId);
        node.setOwnerId(0L);
        node.setAttribute(attribute.getLeft(), attribute.getRight());
        return  node;
    }
}