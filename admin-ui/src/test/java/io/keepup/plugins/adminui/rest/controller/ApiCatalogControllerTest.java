package io.keepup.plugins.adminui.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.JarHelper;
import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.SecurityConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.resources.StaticContentDeliveryService;
import io.keepup.cms.core.datasource.resources.StorageAccessor;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.plugins.adminui.service.AdminUiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(ApiCatalogController.class)
@KeepupRestControllerTest
@ContextConfiguration(classes = {
        KeepupApplication.class,
        AdminUiService.class,
        ApiCatalogController.class,
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
        StaticContentDeliveryService.class,
        ApplicationConfig.class,
        JarHelper.class
})
@TestPropertySource(properties = {
        "keepup.security.permitted-urls=/apicatalog/**",
        "keepup.security.default-web-filter-chain.enabled=true",
        "keepup.plugins.adminui.enabled=true"
})
class ApiCatalogControllerTest {
    @Autowired
    private AdminUiService adminUiService;
    @Autowired
    private WebTestClient client;
    @MockBean
    StorageAccessor<String> storageAccessor;


    @Test
    void apiCatalogPage() {
        Assertions.assertTrue(adminUiService.isEnabled());
        adminUiService.init();
        client.get().uri("/apicatalog/0").exchange()
                .expectStatus().isOk();
    }
}