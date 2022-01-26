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
import io.keepup.cms.core.persistence.User;
import io.keepup.plugins.adminui.rest.model.UserInfo;
import io.keepup.plugins.adminui.rest.service.TestReactiveUserDetailsService;
import io.keepup.plugins.adminui.service.AdminUiService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebFluxTest(AdminPanelController.class)
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
        TestReactiveUserDetailsService.class,
        JarHelper.class
})
@TestPropertySource(properties = {
        "keepup.security.permitted-urls=/admin-ui/userinfo",
        "keepup.plugins.adminui.enabled=true"
})
class AdminPanelControllerTest {
    private static final String SOMEONE = "Someone";
    @Autowired
    WebTestClient client;
    @MockBean
    StorageAccessor<String> storageAccessor;

    @BeforeAll
    static void beforeAll() {
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(getUser(), Collections.emptyList()));
    }

    /**
     * Checking out whether '/admin-ui/userinfo' works correctly for authenticated user
     */
    @Test
    @WithUserDetails(value=SOMEONE, userDetailsServiceBeanName="testReactiveUserDetailsService")
    void getUserInfo() {
        client.get().uri("/admin-ui/userinfo").exchange()
                .expectStatus().isOk()
                .expectBody(UserInfo.class)
                .consumeWith(userInfoEntityExchangeResult -> {
                    UserInfo responseBody = userInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody);
                    assertNotNull(responseBody.getName());
                    assertEquals(SOMEONE, responseBody.getName());
                });
    }

    /**
     * Checking out whether '/admin-ui/userinfo' works correctly for unauthenticated request
     */
    @Test
    @WithAnonymousUser
    void getUserInfoWithoutAuthentication() {
        client.get().uri("/admin-ui/userinfo").exchange()
                .expectStatus().isOk()
                .expectBody(UserInfo.class)
                .consumeWith(userInfoEntityExchangeResult -> {
                    UserInfo responseBody = userInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody);
                    assertNotNull(responseBody.getName());
                    assertEquals("Anonymous", responseBody.getName());
                });

    }

    private static UserDetails getUser() {
        User user = new User();
        user.setEnabled(true);
        user.setUsername("name");
        user.setPassword("somePass");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_MOCK")));
        user.setExpirationDate(LocalDate.MAX);
        user.setAdditionalInfo("{}");
        user.setId(1L);
        return user;
    }
}