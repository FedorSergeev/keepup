package io.keepup.plugins.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.BasicEntity;
import io.keepup.cms.core.persistence.User;
import io.keepup.plugins.catalog.model.CatalogEntity;
import io.keepup.plugins.catalog.model.CatalogEntityListWrapper;
import io.keepup.plugins.catalog.model.DeleteCatalogEntityRequestResponseWrapper;
import io.keepup.plugins.catalog.model.Layout;
import io.keepup.plugins.catalog.rest.CatalogController;
import io.keepup.plugins.catalog.service.CatalogService;
import io.keepup.plugins.catalog.service.LayoutService;
import io.keepup.plugins.catalog.service.TestCatalogEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static io.keepup.cms.core.datasource.sql.EntityUtils.convertToLocalDateViaInstant;
import static java.lang.Long.MAX_VALUE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;

@AutoConfigureWebTestClient
@WebFluxTest(CatalogController.class)
@ActiveProfiles({"dev", "h2", "security"})
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "keepup.security.permitted-urls=/catalog/**",
        "keepup.plugins.catalog.enabled=true"
})
@ContextConfiguration(classes = {
        KeepupApplication.class,
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
        SecurityConfiguration.class,
        CatalogController.class,
        CatalogService.class,
        LayoutService.class
})
class CatalogControllerTest {
    @Autowired
    private CatalogService catalogService;
    @Autowired
    private CatalogController catalogController;
    @Autowired
    private LayoutService layoutService;
    @Autowired
    private DataSourceFacade dataSourceFacade;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ReactiveNodeEntityRepository nodeEntityRepository;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private WebTestClient client;
    @MockBean
    private WebSession webSession;

    @SpyBean
    private CatalogService mockCatalogService;

    @BeforeEach
    void setUp() {
        mapper.registerSubtypes(TestCatalogEntity.class);
    }

    @Test
    void getEmptyResult() {
        dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collect(Collectors.toList()).block();
        client.get().uri("/catalog/0").exchange()
                .expectStatus().isOk()
                .expectBody(CatalogEntityListWrapper.class).consumeWith(wrapper -> {
            assertNotNull(wrapper);
            assertNotNull(wrapper.getResponseBody());
            assertTrue(wrapper.getResponseBody().isSuccess());
            assertNotNull(wrapper.getResponseBody().getEntities());
            assertTrue(wrapper.getResponseBody().getEntities().isEmpty());
            assertNull(wrapper.getResponseBody().getError());
        });
    }

    @Test
    void getOneItemWithoutLayoutAndReceiveEmptyList() {
        CatalogEntity entity = dataSourceFacade.getContent()
                .map(BasicEntity::getId)
                .flatMap(id -> dataSourceFacade.deleteContent(id))
                .collect(Collectors.toList())
                .then(catalogService.save(new TestCatalogEntity(), 0L)).block();
        client.get().uri("/catalog/%s?children=true".formatted(entity.getId())).exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class).consumeWith(wrapper -> {
            assertNotNull(wrapper);
            assertNotNull(wrapper.getResponseBody());
            var body = wrapper.getResponseBody();
            assertNotNull(body.get("entities"));
            assertNotNull(body.get("entities").get(0));
            assertNotNull(body.get("entities").get(0).get("layoutName"));
            assertEquals("test entity", body.get("entities").get(0).get("layoutName").asText());
            assertNotNull(body.get("success"));
            assertTrue(body.get("success").asBoolean());
        });
    }

    @Test
    void getOneItemWithLayout() {
        CatalogEntity entity = layoutService.deleteAll()
                .then(layoutService.save(new TestCatalogEntity().getTestLayout()))
                .then(dataSourceFacade.getContent()
                        .map(BasicEntity::getId)
                        .flatMap(id -> dataSourceFacade.deleteContent(id))
                        .collectList())
                .then(catalogService.save(new TestCatalogEntity(), 0L)).block();
        client.get().uri("/catalog/%s?children=true".formatted(entity.getId())).exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class).consumeWith(response -> {
            JsonNode responseBody = response.getResponseBody();
            assertFalse(responseBody.findValues("entities").isEmpty());
            assertNotNull(responseBody.findValues("entities").get(0).get(0));
            assertNotNull(responseBody.findValues("entities").get(0).get(0).get("id"));
            assertNotNull(responseBody.findValues("entities").get(0).get(0).get("name"));
        });
    }

    @Test
    void getItemAndChildrenItemWithLayout() {
        Long entityId = layoutService.deleteAll()
                .then(layoutService.save(new TestCatalogEntity().getTestLayout()))
                .then(dataSourceFacade.getContent()
                        .map(BasicEntity::getId)
                        .flatMap(id -> dataSourceFacade.deleteContent(id))
                        .collectList())
                .then(catalogService.save(new TestCatalogEntity(), 0L))
                .flatMap(savedEntity -> {
                    TestCatalogEntity childEntity = new TestCatalogEntity();
                    childEntity.setName("childEntity");
                    return catalogService.save(childEntity, 0L, savedEntity.getId())
                            .thenReturn(savedEntity.getId());
                })
                .block();
        client.get().uri("/catalog/%s?children=true".formatted(entityId)).exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class).consumeWith(response -> {
            JsonNode responseBody = response.getResponseBody();
            assertFalse(responseBody.findValues("entities").isEmpty());
            assertEquals(2, responseBody.findValues("entities").get(0).size());
            assertNotNull(responseBody.findValues("entities").get(0).get(0));
            assertNotNull(responseBody.findValues("entities").get(0).get(0).get("id"));
            assertNotNull(responseBody.findValues("entities").get(0).get(0).get("name"));
            assertNotNull(responseBody.findValues("entities").get(0).get(1));
            assertNotNull(responseBody.findValues("entities").get(0).get(1).get("id"));
            assertNotNull(responseBody.findValues("entities").get(0).get(1).get("name"));
        });
    }

    @Test
    void getItemAndParentWithLayouts() {
        var contentId = 26L;
        CatalogEntity entity = new CatalogEntity() {
            @Override
            public Long getId() {
                return 25L;
            }

            @Override
            public String getLayoutName() {
                return "layout";
            }
        };
        CatalogEntity childEntity = new CatalogEntity() {
            @Override
            public Long getId() {
                return contentId;
            }

            @Override
            public String getLayoutName() {
                return "layout";
            }
        };
        CatalogEntity[] catalogEntities = {entity, childEntity};
        Mockito.when(mockCatalogService.getContentParents(anyLong(), anyLong()))
                .thenReturn(Flux.just(catalogEntities));

        client.get().uri("/catalog/%s?parents=true".formatted(contentId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class).consumeWith(response -> {
            JsonNode responseBody = response.getResponseBody();
            assertFalse(responseBody.findValues("parents").isEmpty());
            assertEquals(2, responseBody.findValues("parents").get(0).size());
            assertNotNull(responseBody.findValues("parents").get(0).get(0));
            assertNotNull(responseBody.findValues("parents").get(0).get(0).get("id"));
            assertNotNull(responseBody.findValues("parents").get(0).get(1));
            assertNotNull(responseBody.findValues("parents").get(0).get(1).get("id"));
        });
    }

    @Test
    void getAll() {
        layoutService.deleteAll().then(catalogService.getAll()
                .map(entity -> catalogService.delete(entity.getId()))
                .collectList()
                .then(save10EntitiesAnd1Layout())).block();
        List<NodeEntity> catalogEntities = nodeEntityRepository.findByParentIdsAndType(Collections.singletonList(0L), CatalogEntity.class.getTypeName()).collectList().block();
        client.get().uri("/catalog").exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class).consumeWith(response -> {
            JsonNode responseBody = response.getResponseBody();
            assertFalse(responseBody.findValues("entities").isEmpty());
            assertEquals(catalogEntities.size(), responseBody.get("entities").size());
            for (int i = 0; i < catalogEntities.size(); i++) {
                assertNotNull(responseBody.get("entities").get(i).get("id"));
                assertEquals("test entity", responseBody.get("entities").get(0).get("layoutName").asText());
            }
        });
        assertNotNull(catalogEntities);
    }

    @Test
    void getAllWithError() {
        Mockito.when(mockCatalogService.getAll())
               .thenReturn(Flux.error(new RuntimeException("Testing error in CatalogController#getAll")));
        client.get().uri("/catalog").exchange()
                .expectStatus().is5xxServerError()
                .expectBody(JsonNode.class).consumeWith(response -> {
            JsonNode responseBody = response.getResponseBody();

        });
    }

    @Test
    void getByIdWithError() {
        Mockito.when(mockCatalogService.get(anyLong()))
                .thenReturn(Mono.error(new RuntimeException("Testing error in CatalogController#get")));
        client.get().uri("/catalog/2").exchange()
                .expectStatus().is5xxServerError()
                .expectBody(JsonNode.class).consumeWith(response -> {
            JsonNode responseBody = response.getResponseBody();

        });
    }

    @Test
    void saveOk() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("user")));
        user.setExpirationDate(convertToLocalDateViaInstant(new Date(MAX_VALUE)));
        user.setAttributes(new HashMap<>());
        user.setUsername("test_%s".formatted(randomUUID().toString()));
        user.setPassword(passwordEncoder.encode("test"));
        user.setEnabled(true);

        client.mutateWith(SecurityMockServerConfigurers.csrf()).post()
                .uri("/catalog/0")
                .body(BodyInserters.fromValue(new TestCatalogEntity()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertTrue(responseBody.get("success").asBoolean());
                    assertTrue(responseBody.get("error").isEmpty());
                    try {
                        TestCatalogEntity value = mapper.treeToValue(responseBody.get("entity"), TestCatalogEntity.class);
                        assertNotNull(value.getId());
                        assertNull(value.getName());
                        assertEquals("test entity", value.getLayoutName());
                    } catch (JsonProcessingException e) {
                       fail(e.getMessage());
                    }
                });
    }

    @Test
    void saveWithoutParentId() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("user")));
        user.setExpirationDate(convertToLocalDateViaInstant(new Date(MAX_VALUE)));
        user.setAttributes(new HashMap<>());
        user.setUsername("test_%s".formatted(randomUUID().toString()));
        user.setPassword(passwordEncoder.encode("test"));
        user.setEnabled(true);

        client.mutateWith(SecurityMockServerConfigurers.csrf()).post()
                .uri("/catalog/")
                .body(BodyInserters.fromValue(new TestCatalogEntity()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertFalse(responseBody.get("success").asBoolean());
                    assertTrue(responseBody.get("error").isEmpty());
                    assertEquals("No parent identifier specified for catalog entity", responseBody.get("error").asText());
                });
    }

    @Test
    void saveWithWrongParentId() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("user")));
        user.setExpirationDate(convertToLocalDateViaInstant(new Date(MAX_VALUE)));
        user.setAttributes(new HashMap<>());
        user.setUsername("test_%s".formatted(randomUUID().toString()));
        user.setPassword(passwordEncoder.encode("test"));
        user.setEnabled(true);

        client.mutateWith(SecurityMockServerConfigurers.csrf()).post()
                .uri("/catalog/-1")
                .body(BodyInserters.fromValue(new TestCatalogEntity()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(JsonNode.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertFalse(responseBody.get("success").asBoolean());
                    assertEquals("Parent identifier cannot be negative", responseBody.get("error").asText());
                });
    }

    @Test
    void saveWithLayout() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("user")));
        user.setExpirationDate(convertToLocalDateViaInstant(new Date(MAX_VALUE)));
        user.setAttributes(new HashMap<>());
        user.setUsername("test_%s".formatted(randomUUID().toString()));
        user.setPassword(passwordEncoder.encode("test"));
        user.setEnabled(true);

        layoutService.deleteAll()
                .then(layoutService.save(new TestCatalogEntity().getTestLayout())).block();

        client.mutateWith(SecurityMockServerConfigurers.csrf()).post()
                .uri("/catalog/0")
                .body(BodyInserters.fromValue(new TestCatalogEntity()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertTrue(responseBody.get("success").asBoolean());
                    assertTrue(responseBody.get("error").isEmpty());
                    try {
                        TestCatalogEntity value = mapper.treeToValue(responseBody.get("entity"), TestCatalogEntity.class);
                        Layout layout = mapper.treeToValue(responseBody.get("layout"), Layout.class);
                        assertNotNull(value.getId());
                        assertNull(value.getName());
                        assertEquals("test entity", value.getLayoutName());
                        assertNotNull(layout.getId());
                        assertNotNull(layout.getName());
                        assertNotNull(layout.getAttributes());
                    } catch (JsonProcessingException e) {
                        fail(e.getMessage());
                    }
                });
    }

    @Test
    void delete() {
        CatalogEntity savedEntity = catalogService.save(new TestCatalogEntity(), 0L).block();

        client.mutateWith(SecurityMockServerConfigurers.csrf()).delete()
                .uri("/catalog/%s".formatted(savedEntity.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertTrue(responseBody.get("success").asBoolean());
                    assertTrue(responseBody.get("error").isEmpty());
                });
    }

    @Test
    void deleteWithError() {
        String exceptionMessage = "Test wrong exception";
        MockitoAnnotations.openMocks(this);
        Mockito.when(mockCatalogService.delete(anyLong()))
                .thenReturn(Mono.error(new RuntimeException(exceptionMessage)));
        CatalogController testCatalogController = new CatalogController(mockCatalogService, layoutService);
        CatalogEntity savedEntity = catalogService.save(new TestCatalogEntity(), 0L).block();

        ResponseEntity<DeleteCatalogEntityRequestResponseWrapper> result = testCatalogController.delete(savedEntity.getId(), webSession).block();

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody().getError());
        assertEquals(exceptionMessage, result.getBody().getError());
    }

    private Mono<Layout> save10EntitiesAnd1Layout() {
        for (int i = 0; i < 10; i++) {
            CatalogEntity block = catalogService.save(new TestCatalogEntity(), 0L).block();
            block.getLayoutName();
        }
        return layoutService.save(new TestCatalogEntity().getTestLayout());
    }
}