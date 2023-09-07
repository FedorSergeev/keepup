package io.keepup.cms.core.config;

import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
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
 * Please disable this configuration if you want to customize your own
 *
 * @author Fedor Sergeev
 * @since 2.0.0
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
     * CSRF protection toggle
     */
    @Value("${keepup.security.csrf-enabled:true}")
    private boolean csrfEnabled;

    /**
     * Defines whether to override the default authentication form
     */
    @Value("${keepup.security.override-web-login:false}")
    private boolean overrideWebLoginForm;

    /**
     * Security configuration constructor with DAO component injection.
     *
     * @param dataSourceFacade main KeepUP data access object
     */
    public SecurityConfiguration(final DataSourceFacade dataSourceFacade) {
        this.dataSourceFacade = dataSourceFacade;
        log.debug("Security configuration instantiated with data source facade");
    }

    /**
     * A ServerCsrfTokenRepository that stores the CsrfToken in the HttpSession.
     *
     * @return CSRF tokens data access object
     */
    @Bean
    public WebSessionServerCsrfTokenRepository webSessionServerCsrfTokenRepository() {
        log.debug("Instantiating web session CSRF token repository");
        return new WebSessionServerCsrfTokenRepository();
    }

    /**
     * Instantiates a filter chain which is capable of being matched against a ServerWebExchange in order to decide
     * whether it applies to that request.
     *
     * @param http reactive http security object
     * @return     configured security filter chain component
     */
    @Bean
    @ConditionalOnProperty(prefix = "keepup.security.default-web-filter-chain", name = "enabled", havingValue = "true")
    @Profile("security")
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.debug("Configuring server HTTP security");
        final ServerHttpSecurity csrf = http.authorizeExchange()
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
                });
        return overrideWebLoginForm
                ? csrf.build()
                : csrf.formLogin().loginPage(loginUrl).and().build();
    }

    /**
     * Instantiates the service for encoding passwords.
     *
     * @return password encoding service
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Instantiating the password encoder");
        return new BCryptPasswordEncoder();
    }

    /**
     * Instantiates reactive service for fetching information about users.
     *
     * @return reactive service that provides core user information
     */
    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        log.debug("Instantiating the UserDetails service");
        return dataSourceFacade::getUserByName;
    }

    private Mono<Void> getLogoutSuccessHandler(final WebFilterExchange exchange) {
        final var response = exchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(LOGOUT_LOCATION.formatted(loginUrl)));
        response.getCookies().remove(JSESSIONID);
        return exchange.getExchange().getSession()
                .flatMap(WebSession::invalidate);
    }
}
