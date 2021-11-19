package io.keepup.plugins.catalog.service;

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
import io.keepup.plugins.catalog.LayoutService;
import io.keepup.plugins.catalog.model.CatalogEntity;
import io.keepup.plugins.catalog.model.CatalogEntityWrapper;
import io.keepup.plugins.catalog.model.Layout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.List;

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
        R2dbcConfiguration.class,
        SqlContentDao.class,
        SqlFileDao.class,
        SqlUserDao.class,
        DataSourceFacadeImpl.class,
        CatalogService.class,
        LayoutService.class
})
@TestPropertySource(properties = {
        "keepup.plugins.catalog.enabled=true",
})
@DataR2dbcTest
class CatalogServiceTest {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private CatalogService catalogService;
    @Autowired
    private DataSourceFacade dataSourceFacade;
    @Autowired
    private LayoutService layoutService;
    @Autowired
    private ReactiveContentClassRepository contentClassRepository;
    @Autowired
    private ReactiveNodeEntityRepository nodeEntityRepository;

    private TestCatalogEntity testCatalogEntity;

    @BeforeEach
    void setUp() {
        layoutService.deleteAll()
                .then(createNewLayout())
                .block();
    }

    @Test
    void getWithChildren() {
        var catalogEntities = catalogService.save(testCatalogEntity, 0L)
                .flatMap(saved -> catalogService.getWithChildren(saved.getId()).collectList())
                .block();

        // region assert
        assertNotNull(catalogEntities);
        assertFalse(catalogEntities.isEmpty());
        // endregion
    }

    @Test
    void getWithChildrenWithNoCatalogEntity() {
        var catalogEntities = catalogService.save(testCatalogEntity, 0L)
                .flatMap(saved -> nodeEntityRepository.findById(saved.getId())
                            .flatMap(entity -> {
                                entity.setEntityType("someWrongType");
                                return nodeEntityRepository.save(entity);
                            }))
                .flatMap(node -> catalogService.getWithChildren(node.getId()).collectList())
                .block();

        // region assert
        assertNotNull(catalogEntities);
        assertTrue(catalogEntities.isEmpty());
        // endregion
    }

    @Test
    void getWithChildrenWithNullEntityType() {
        var catalogEntities = catalogService.save(testCatalogEntity, 0L)
                .flatMap(saved -> nodeEntityRepository.findById(saved.getId())
                        .flatMap(entity -> {
                            entity.setEntityType(null);
                            return nodeEntityRepository.save(entity);
                        }))
                .flatMap(node -> catalogService.getWithChildren(node.getId()).collectList())
                .block();

        // region assert
        assertNotNull(catalogEntities);
        assertTrue(catalogEntities.isEmpty());
        // endregion
    }

    @Test
    void getCatalogEntitiesWithLayouts() {

        var testCatalogEntity = new TestCatalogEntity();
        List<CatalogEntityWrapper<CatalogEntity>> catalogEntitiesWithLayouts = catalogService.save(testCatalogEntity, 0L)
                .flatMap(catalogEntity -> catalogService.getCatalogEntitiesWithLayouts(catalogEntity.getId(), true)
                        .collectList()).block();

        // region assert
        assertNotNull(catalogEntitiesWithLayouts);
        assertFalse(catalogEntitiesWithLayouts.isEmpty());
        assertEquals(1, catalogEntitiesWithLayouts.size());
        assertTrue(catalogEntitiesWithLayouts.get(0).isSuccess());
        assertNull(catalogEntitiesWithLayouts.get(0).getError());
        assertNotNull(catalogEntitiesWithLayouts.get(0).getEntity());
        assertTrue(catalogEntitiesWithLayouts.get(0).getEntity() instanceof TestCatalogEntity);
        assertEquals(testCatalogEntity.getLayoutName(), catalogEntitiesWithLayouts.get(0).getEntity().getLayoutName());
        assertEquals(testCatalogEntity.getName(), ((TestCatalogEntity)catalogEntitiesWithLayouts.get(0).getEntity()).getName());
        assertNotNull(catalogEntitiesWithLayouts.get(0).getLayout());
        assertEquals(testCatalogEntity.getTestLayout().getName(), catalogEntitiesWithLayouts.get(0).getLayout().getName());
        assertEquals(testCatalogEntity.getTestLayout().getHtml(), catalogEntitiesWithLayouts.get(0).getLayout().getHtml());
        assertEquals(testCatalogEntity.getTestLayout().getAttributes(), catalogEntitiesWithLayouts.get(0).getLayout().getAttributes());
        // endregion
    }

    @Test
    void getCatalogEntitiesWithLayoutsWithoutChildrenFlag() {

        var testCatalogEntity = new TestCatalogEntity();
        List<CatalogEntityWrapper<CatalogEntity>> catalogEntitiesWithLayouts = catalogService.save(testCatalogEntity, 0L)
                .flatMap(catalogEntity ->
                {
                    log.info("Catalog entity saved with id = %d".formatted(catalogEntity.getId()));
                    return catalogService.getCatalogEntitiesWithLayouts(catalogEntity.getId(), false)
                            .collectList();
                })
                .block();

        // region assert
        assertNotNull(catalogEntitiesWithLayouts);
        assertFalse(catalogEntitiesWithLayouts.isEmpty());
        assertEquals(1, catalogEntitiesWithLayouts.size());
        assertTrue(catalogEntitiesWithLayouts.get(0).isSuccess());
        assertNull(catalogEntitiesWithLayouts.get(0).getError());
        assertNotNull(catalogEntitiesWithLayouts.get(0).getEntity());
        assertTrue(catalogEntitiesWithLayouts.get(0).getEntity() instanceof TestCatalogEntity);
        assertEquals(testCatalogEntity.getLayoutName(), catalogEntitiesWithLayouts.get(0).getEntity().getLayoutName());
        assertEquals(testCatalogEntity.getName(), ((TestCatalogEntity)catalogEntitiesWithLayouts.get(0).getEntity()).getName());
        assertNotNull(catalogEntitiesWithLayouts.get(0).getLayout());
        assertEquals(testCatalogEntity.getTestLayout().getName(), catalogEntitiesWithLayouts.get(0).getLayout().getName());
        assertEquals(testCatalogEntity.getTestLayout().getHtml(), catalogEntitiesWithLayouts.get(0).getLayout().getHtml());
        assertEquals(testCatalogEntity.getTestLayout().getAttributes(), catalogEntitiesWithLayouts.get(0).getLayout().getAttributes());
        // endregion
    }

    @Test
    void getCatalogEntitiesWithLayoutsWithNullId() {
        var testCatalogEntity = new TestCatalogEntity();
        List<CatalogEntityWrapper<CatalogEntity>> catalogEntitiesWithLayouts = catalogService.save(testCatalogEntity, 0L)
                .flatMap(catalogEntity -> catalogService.getCatalogEntitiesWithLayouts(null, false)
                        .collectList()).block();

        // region assert
        assertNotNull(catalogEntitiesWithLayouts);
        assertFalse(catalogEntitiesWithLayouts.isEmpty());
        assertEquals(1, catalogEntitiesWithLayouts.size());
        assertFalse(catalogEntitiesWithLayouts.get(0).isSuccess());
        assertEquals("Can not find content by null id value", catalogEntitiesWithLayouts.get(0).getError());
        // endregion
    }

    private Mono<Layout> createNewLayout() {
        testCatalogEntity = new TestCatalogEntity();
        return layoutService.save(testCatalogEntity.getTestLayout());
    }
}