package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "embedded-postgres"})
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
                ConnectionFactory.class
        }
)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.r2dbc.pool.enabled=true",
        "spring.r2dbc.pool.initial-size=10",
        "spring.r2dbc.pool.max-idle-time=1m",
        "spring.r2dbc.pool.max-size=30",
        "spring.data.r2dbc.repositories.enabled=true",
})
@Testcontainers
class PostgresContainerDataSourceTest {

    @Autowired
    DataSourceFacade dataSourceFacade;

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
        registry.add(        "spring.r2dbc.password", () -> "test");
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://localhost:%d/keepup-db?currentSchema=public&TC_DAEMON=true&TC_IMAGE_TAG=11.1".formatted(postgres.getFirstMappedPort()));
    }

    @Autowired
    ReactiveNodeEntityRepository nodeEntityRepository;

    @Test
    void getContentParents() {
        Content content = new Node();
        content.setAttribute("parentNodeAttribute", "parent");
        content.setParentId(0L);
        content.setOwnerId(0L);
        content.setDefaultPrivileges();
        List<Content> parentRecords = dataSourceFacade.createContent(content)
                .flatMap(parentId -> {
                    Content child = new Node();
                    child.setAttribute("childNodeAttribute", "child");
                    child.setParentId(parentId);
                    child.setOwnerId(0L);
                    child.setDefaultPrivileges();
                    return dataSourceFacade.createContent(child);
                })
                .flatMap(childId -> dataSourceFacade.getContentParents(childId, Long.MAX_VALUE).collectList())
                .block();

        assertNotNull(parentRecords);
        assertFalse(parentRecords.isEmpty());
        assertEquals(2, parentRecords.size());
    }

    @Test
    void getContentParentsWithNullId() {
        List<Content> parentRecords = dataSourceFacade.getContentParents(null, Long.MAX_VALUE).collectList()
                .block();
        assertNotNull(parentRecords);
        assertTrue(parentRecords.isEmpty());
    }

    @Test
    void getContentParentsWithNullOffset() {
        Content content = new Node();
        content.setAttribute("parentNodeAttribute", "parent");
        content.setParentId(0L);
        content.setOwnerId(0L);
        content.setDefaultPrivileges();
        List<Content> parentRecords = dataSourceFacade.createContent(content)
                .flatMap(parentId -> {
                    Content child = new Node();
                    child.setAttribute("childNodeAttribute", "child");
                    child.setParentId(parentId);
                    child.setOwnerId(0L);
                    child.setDefaultPrivileges();
                    return dataSourceFacade.createContent(child);
                })
                .flatMap(childId -> dataSourceFacade.getContentParents(childId, null).collectList())
                .block();
        assertNotNull(parentRecords);
        assertFalse(parentRecords.isEmpty());
        assertEquals(2, parentRecords.size());
    }
}
