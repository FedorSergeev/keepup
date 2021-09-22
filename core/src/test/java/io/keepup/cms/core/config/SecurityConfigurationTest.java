package io.keepup.cms.core.config;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.EntityUtils;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.User;
import io.keepup.cms.core.testing.TestController;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Web security tests
 */
@ActiveProfiles({"dev", "h2", "security"})
@WebFluxTest(TestController.class)
@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "keepup.security.path-matchers=/testing,/testing/with-role,/with-role-admin",
        "keepup.security.csrf-enabled=false"
})
@ContextConfiguration(classes = {
        BCryptPasswordEncoder.class,
        SecurityWebFilterChain.class,
        SecurityConfiguration.class,
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
        DataSourceFacadeImpl.class
})
class SecurityConfigurationTest {

    public static final String SESSION = "SESSION";

    @Autowired
    private WebTestClient webClient;
    @Autowired
    private DataSourceFacade dataSourceFacade;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void loginRedirect() {
        webClient.get()
                .uri("/testing")
                .exchange()
                .expectStatus().isFound();
    }

    @Test
    void authorizedUser() {

        webClient.get()
                .uri("/testing")
                .cookie(SESSION, getSessionCookieValue(getCookieByLoggingIn()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void authorizedUserWithRoleUser() {
        webClient.get()
                .uri("/testing/with-role")
                .cookie(SESSION, getSessionCookieValue(getCookieByLoggingIn()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void authorizedUserWithoutRequestsRole() {
        webClient.get()
                .uri("/testing/with-role-admin")
                .cookie(SESSION, getSessionCookieValue(getCookieByLoggingIn()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @NotNull
    private AtomicReference<String> getCookieByLoggingIn() {
        final var cookieRef = new AtomicReference<String>();
        var user = new User();
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("user")));
        user.setExpirationDate(EntityUtils.convertToLocalDateViaInstant(new Date(Long.MAX_VALUE)));
        user.setAttributes(new HashMap<>());
        user.setUsername("test_%s".formatted(UUID.randomUUID().toString()));
        user.setPassword(passwordEncoder.encode("test"));
        user.setEnabled(true);
        dataSourceFacade.createUser(user).map(savedUser ->
                webClient.mutateWith(SecurityMockServerConfigurers.csrf()).post()
                        .uri("/login")
                        .body(BodyInserters.fromFormData("username", savedUser.getUsername())
                                .with("password", "test"))
                        .exchange()
                        .expectHeader().value("Set-Cookie", cookie -> cookieRef.set(cookie)
                )).block();
        return cookieRef;
    }

    private String getSessionCookieValue(AtomicReference<String> cookieRef) {
        return Arrays.asList(cookieRef.get().split(";"))
                .stream()
                .filter(cookie -> cookie.startsWith(SESSION))
                .findFirst()
                .orElse("")
                .trim()
                .split("=")[1];
    }
}