package io.keepup.plugins.catalog.service;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CatalogService} component using PostgreSQL Docker container
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ContextConfiguration(
        classes = {
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
                DataSource.class,
                DataSourceFacadeImpl.class,
                CatalogService.class
        }
)
@TestPropertySource(properties = {
        "keepup.plugins.catalog.enabled=true",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.r2dbc.pool.enabled=true",
        "spring.r2dbc.pool.initial-size=10",
        "spring.r2dbc.pool.max-idle-time=1m",
        "spring.r2dbc.pool.max-size=30",
        "spring.data.r2dbc.repositories.enabled=true",
})
@Testcontainers
class PostgresCatalogServiceTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:11")
            .withDatabaseName("keepup-db")
            .withUsername("test")
            .withPassword("test")
            .withExposedPorts(5432);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.jdbc-url",
                () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.r2dbc.username", () -> "test");
        registry.add("spring.r2dbc.password", () -> "test");
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://localhost:%d/keepup-db?currentSchema=public&TC_DAEMON=true&TC_IMAGE_TAG=11.1".formatted(postgres.getFirstMappedPort()));
    }

    @Autowired
    CatalogService catalogService;

    @Test
    void getContentParents() {
        String parentValue = "parentValue";
        CatalogEntityTestImpl catalogEntity = new CatalogEntityTestImpl();
        catalogEntity.setValue(parentValue);
        CatalogEntityTestImpl childEntity = new CatalogEntityTestImpl();
        childEntity.setValue("childValue");
        var parents = catalogService.save(catalogEntity, 0L, 0L)
                .flatMap(savedParent -> {
                    childEntity.setParentId(savedParent.getId());
                    return catalogService.save(childEntity, 0L, savedParent.getId());
                })
                .flatMap(savedChildEntity -> catalogService.getContentParents(((CatalogEntityTestImpl)savedChildEntity).getParentId(), null)
                        .collectList())
                .block();

        assertNotNull(parents);
        assertFalse(parents.isEmpty());
        assertEquals(1, parents.size());
        assertEquals(parentValue, ((CatalogEntityTestImpl)parents.get(0)).getValue());
    }

    @Test
    void getContentParentsByNUllId() {
        var contentParents = catalogService.getContentParents(null, null)
                .collectList()
                .block();
        assertNotNull(contentParents);
        assertTrue(contentParents.isEmpty());
    }

}
