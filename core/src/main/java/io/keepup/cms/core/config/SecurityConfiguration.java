package io.keepup.cms.core.config;

import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.WebSessionServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.apache.commons.lang3.ArrayUtils.addAll;

/**
 * Default security configuration. Can be switched off by removing 'security' profile from the
 * list of active profiles.
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@ConditionalOnProperty(prefix = "keepup.security", name = "enabled", havingValue = "true")
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    private static final String JSESSIONID = "JSESSIONID";
    private static final String LOGOUT_LOCATION = "%s?logout";
    private final Log log = LogFactory.getLog(getClass());
    private final DataSourceFacade dataSourceFacade;

    /**
     * URLs witch require authenticated access
     */
    @Value("${keepup.security.path-matchers:/}")
    private String[] pathMatchers;

    /**
     * URLs witch do not require authenticated access (e.g. metrics in some cases)
     */
    @Value("${keepup.security.permitted-urls:/actuator/**}")
    private String[] permittedUrls;

    /**
     * URL for logging in
     */
    @Value("${keepup.security.login-url:/login}")
    private String loginUrl;

    /**
     * URL for logging out
     */
    @Value("${keepup.security.logout-url:/logout}")
    private String logoutUrl;

    /**
     * CSRF protectyion toggle
     */
    @Value("${keepup.security.csrf-enabled:true}")
    private boolean csrfEnabled;

    public SecurityConfiguration(DataSourceFacade dataSourceFacade) {
        this.dataSourceFacade = dataSourceFacade;
        log.debug("Security configuration instantiated with data source facade");
    }

    @Bean
    WebSessionServerCsrfTokenRepository webSessionServerCsrfTokenRepository() {
        log.debug("Instantiating web session CSRF token repository");
        return new WebSessionServerCsrfTokenRepository();
    }

    @Bean
    @Profile("security")
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.debug("Configuring server HTTP security");
        return http.authorizeExchange()
                .pathMatchers(pathMatchers)
                    .authenticated()
                .pathMatchers(addAll(permittedUrls, loginUrl))
                    .permitAll()
                .and()
                .formLogin()
                .and()
                .logout()
                    .logoutUrl(logoutUrl)
                    .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, logoutUrl))
                    .logoutSuccessHandler((exchange, authentication)  -> getLogoutSuccessHandler(exchange))
                .and()
                .csrf(csrfSpec -> {
                    if (!csrfEnabled) csrfSpec.disable();
                })
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Instantiating the password encoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        log.debug("Instantiating the UserDetails service");
        return dataSourceFacade::getUserByName;
    }

    @NotNull
    private Mono<Void> getLogoutSuccessHandler(org.springframework.security.web.server.WebFilterExchange exchange) {
        ServerHttpResponse response = exchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(LOGOUT_LOCATION.formatted(loginUrl)));
        response.getCookies().remove(JSESSIONID);
        return exchange.getExchange().getSession()
                .flatMap(WebSession::invalidate);
    }
}
