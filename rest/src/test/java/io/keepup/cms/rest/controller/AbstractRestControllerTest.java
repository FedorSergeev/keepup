package io.keepup.cms.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.SecurityConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.BasicEntity;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureWebTestClient
@WebFluxTest(SomeEntityController.class)
@ActiveProfiles({"dev", "h2", "security"})
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "keepup.security.permitted-urls=/rest-test,/rest-test/**",
})
@ContextConfiguration(classes = {
        KeepupApplication.class,
        SomeEntityController.class,
        SomeEntityService.class,
        ObjectMapper.class,
        DataSourceFacadeImpl.class,
        SqlUserDao.class,
        SqlContentDao.class,
        ReactiveNodeEntityRepository.class,
        WebFluxConfig.class,
        ReactiveNodeEntityRepository.class,
        ReactiveNodeAttributeEntityRepository.class,
        ReactiveUserEntityRepository.class,
        DataSourceConfiguration.class,
        R2dbcConfiguration.class,
        KeepupCacheConfiguration.class,
        CacheAdapter.class,
        SqlFileDao.class,
        BCryptPasswordEncoder.class,
        SecurityWebFilterChain.class,
        SecurityConfiguration.class
})
class AbstractRestControllerTest {
    @Autowired
    private SomeEntityController someEntityController;
    @Autowired
    private SomeEntityService someEntityService;
    @Autowired
    private DataSourceFacade dataSourceFacade;

    @Autowired
    private WebTestClient client;

    @Test
    void getAllEmptyResult() {
        dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collect(Collectors.toList()).block();

        client.get().uri("/rest-test").exchange()
                .expectStatus().isOk().expectBody().json("{\"entities\":[],\"success\":true,\"error\":null}");
    }

    @Test
    void getAllOneValue() {
        dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collect(Collectors.toList())
                .then(someEntityService.save(new SomeEntity(), 0L))
                .block();

        client.get()
                .uri("/rest-test")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(KeepupResponseListWrapper.class)
                .consumeWith(response -> {
                    var responseWrapper = response.getResponseBody();
                    Assertions.assertNotNull(responseWrapper);
                });
    }

    @Test
    void getAllManyValues() {
        dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collect(Collectors.toList())
                .then(someEntityService.save(new SomeEntity(), 0L))
                .then(someEntityService.save(new SomeEntity(), 0L))
                .then(someEntityService.save(new SomeEntity(), 0L))
                .block();

        client.get().uri("/rest-test").exchange()
                .expectStatus().isOk().expectBody(KeepupResponseListWrapper.class).consumeWith(response -> {
            KeepupResponseListWrapper responseWrapper = response.getResponseBody();
            Assertions.assertNotNull(responseWrapper);
            assertNull(responseWrapper.getError());
            Assertions.assertTrue(responseWrapper.isSuccess());
            Assertions.assertNotNull(responseWrapper.getEntities());
            assertEquals(3, responseWrapper.getEntities().size());
        });
    }

    @Test
    void getAllManyWithWrongValues() {
        dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collect(Collectors.toList())
                .then(someEntityService.save(new SomeEntity(), 0L))
                .then(someEntityService.save(new SomeEntity(), 0L))
                .then(someEntityService.save(new SomeEntity(), 0L))
                .thenMany(dataSourceFacade.getContentByParentIds(Collections.singletonList(someEntityService.getEntityParentIds().get(0))))
                .collectList()
                .flatMap(contentList -> dataSourceFacade.updateContentAttribute(contentList.get(0).getId(), "come_value", new ArrayList<>(Arrays.asList("s", "a"))))
                .block();

        client.get().uri("/rest-test").exchange()
                .expectStatus().isOk().expectBody(KeepupResponseListWrapper.class).consumeWith(response -> {
            KeepupResponseListWrapper responseWrapper = response.getResponseBody();
            Assertions.assertNotNull(responseWrapper);
            assertNull(responseWrapper.getError());
            Assertions.assertTrue(responseWrapper.isSuccess());
            Assertions.assertNotNull(responseWrapper.getEntities());
            assertEquals(3, responseWrapper.getEntities().size());
        });
    }

    @Test
    void getAllNpe() throws IllegalAccessException {
        var service = FieldUtils.readField(someEntityController, "operationService", true);
        FieldUtils.writeField(someEntityController, "operationService", null, true);

        client.get().uri("/rest-test").exchange()
                .expectStatus().is5xxServerError();

        FieldUtils.writeField(someEntityController, "operationService", service, true);
    }

    @Test
    void getALlServiceError() throws IllegalAccessException {
        var service = FieldUtils.readField(someEntityController, "operationService", true);

        FieldUtils.writeField(someEntityController, "operationService", new WrongEntityService(), true);
        client.get().uri("/rest-test").exchange()
                .expectStatus().is5xxServerError()
                .expectBody(KeepupResponseListWrapper.class).consumeWith(response -> {
            var responseWrapper = response.getResponseBody();
            Assert.assertFalse(responseWrapper.isSuccess());
            Assertions.assertNotNull(responseWrapper.getEntities());
            Assertions.assertTrue(responseWrapper.getEntities().isEmpty());
            Assertions.assertNotNull(responseWrapper.getError());
            assertEquals("java.lang.RuntimeException: Wrong entity service getAll method invoked", responseWrapper.getError());
        });

        FieldUtils.writeField(someEntityController, "operationService", service, true);
    }

    @Test
    void getEmptyResult() {
        dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collect(Collectors.toList()).block();

        client.get().uri("/rest-test/1").exchange()
                .expectStatus().isOk().expectBody(KeepupResponseWrapper.class)
                .consumeWith(body -> {
                    assertEquals(HttpStatus.OK, body.getStatus());
                    assertNull(body.getResponseBody());
                });
    }

    @Test
    void getCorrectResult() {
        var value = UUID.randomUUID().toString();
        SomeEntity entity = new SomeEntity();
        entity.setValue(value);
        var savedEntity = dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collectList()
                .then(someEntityService.save(entity, 0L))
                .block();

        client.get().uri("/rest-test/%s".formatted(savedEntity.getId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(KeepupResponseWrapper.class)
                .consumeWith(body -> {
                    var response = body.getResponseBody();
                    assertEquals(HttpStatus.OK, body.getStatus());
                    assertNotNull(response);
                    assertEquals(value, ((Map<String, Object>) response.getEntity()).get("value"));
                });
    }

    @Test
    void getNpe() throws IllegalAccessException {
        var service = FieldUtils.readField(someEntityController, "operationService", true);
        FieldUtils.writeField(someEntityController, "operationService", null, true);

        var value = UUID.randomUUID().toString();
        SomeEntity entity = new SomeEntity();
        entity.setValue(value);
        var savedEntity = dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collectList()
                .then(someEntityService.save(entity, 0L))
                .block();
        client.get().uri("/rest-test/%s".formatted(savedEntity.getId())).exchange()
                .expectStatus().is5xxServerError();

        FieldUtils.writeField(someEntityController, "operationService", service, true);
    }

    @Test
    void getServiceError() throws IllegalAccessException {
        var service = FieldUtils.readField(someEntityController, "operationService", true);

        FieldUtils.writeField(someEntityController, "operationService", new WrongEntityService(), true);

        var value = UUID.randomUUID().toString();
        SomeEntity entity = new SomeEntity();
        entity.setValue(value);
        var savedEntity = dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collectList()
                .then(someEntityService.save(entity, 0L))
                .block();

        client.get().uri("/rest-test/%s".formatted(savedEntity.getId())).exchange()
                .expectStatus().is5xxServerError()
                .expectBody(KeepupResponseWrapper.class)
                .consumeWith(response -> {
                    var responseWrapper = response.getResponseBody();
                    Assert.assertFalse(responseWrapper.isSuccess());
                    Assertions.assertNull(responseWrapper.getEntity());
                    Assertions.assertNotNull(responseWrapper.getError());
                    assertEquals("java.lang.RuntimeException: Wrong entity service get method invoked", responseWrapper.getError());
                });

        FieldUtils.writeField(someEntityController, "operationService", service, true);
    }

    @Test
    void save() {
        var entity = new SomeEntity();
        entity.setValue("new_value");
        client.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/rest-test")
                .body(Mono.just(entity), SomeEntity.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void delete() {
        var entity = new SomeEntity();
        entity.setValue("new_value");
        someEntityService.save(entity, 0L)
                .map(saved ->
        client.mutateWith(SecurityMockServerConfigurers.csrf())
                .delete()
                .uri("/rest-test/%d".formatted(saved.getId()))
                .exchange()
                .expectStatus().isOk())
                .block();
    }
}