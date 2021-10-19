package io.keepup.cms.core.config;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.service.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        TestEntityOperationService.class,
        TestNotSerializableAttributesEntityOperationService.class,
        TestEntityWithoutDefaultConstructorOperationService.class,
        EntityWithFinalFieldOperationService.class,
        TestEntityInterfaceService.class,
        ReactiveWebServerFactory.class
})
@DataR2dbcTest
class WebFluxConfigTest {

    @Autowired
    ReactiveWebServerFactory reactiveWebServerFactory;

    @Test
    void reactiveWebServerFactory() {
        assertNotNull(reactiveWebServerFactory);
    }
}