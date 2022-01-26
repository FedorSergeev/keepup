package io.keepup.plugins.adminui.service;

import io.keepup.cms.core.annotation.Deploy;
import io.keepup.cms.core.annotation.Plugin;
import io.keepup.cms.core.plugins.AbstractKeepupDeployBean;
import io.keepup.plugins.adminui.exception.ApiCatalogPageNotFoundException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

/**
 * KeepUP plugin service providing special UI for administrative purposes
 */
@Plugin
@Deploy
@Service
@ConditionalOnProperty(prefix = "keepup.plugins.adminui", name = "enabled", havingValue = "true")
public class AdminUiService extends AbstractKeepupDeployBean {

    private static final String PATH_TO_ADMINUI_INDEX_PAGE = "META-INF/server/index.html";
    @Value("${keepup.plugins.adminui.uri:/**}")
    private String dashboardUri;
    @Value("${keepup.plugins.adminui.enabled:false}")
    private boolean isEnabled;

    private String apiCatalogPage;

    public AdminUiService() {
        super("AdminUi");
    }

    @Bean
    public RouterFunction<ServerResponse> imgRouter() {
        logger.debug("Setting URI for administrative dashboard with value = %s".formatted(dashboardUri));
        return RouterFunctions.resources(dashboardUri,
                new FileSystemResource("%s/resources".formatted(applicationConfig.getDocumentRoot())));
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void init() {
        InputStream resource = getClass().getClassLoader().getResourceAsStream(PATH_TO_ADMINUI_INDEX_PAGE);
        try {
            apiCatalogPage = IOUtils.toString(ofNullable(resource).orElse(new ByteArrayInputStream(new byte[0])), UTF_8.name());
            logger.info("API catalog page loaded");
        } catch (IOException e) {
            logger.error("Failed to load catalog page: %s".formatted(e.toString()));
        }
    }

    public Mono<String> getApiCatalogPage() {
        return ofNullable(apiCatalogPage)
                .map(Mono::just)
                .orElse(Mono.error(new ApiCatalogPageNotFoundException("Api catalog page was not loaded")));
    }
}