package io.keepup.plugins.adminui.service;

import io.keepup.cms.core.annotation.Deploy;
import io.keepup.cms.core.annotation.Plugin;
import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.plugins.AbstractKeepupDeployBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * KeepUP plugin service providing special UI for administrative purposes
 */
@Plugin
@Deploy
@Service
@ConditionalOnProperty(prefix = "keepup.plugins.adminui", name = "enabled", havingValue = "true")
public class AdminUiService extends AbstractKeepupDeployBean {

    @Value("${keepup.plugins.adminui.uri:/**}")
    private String dashboardUri;
    @Value("${keepup.plugins.adminui.enabled:false}")
    private boolean isEnabled;

    @Autowired
    private ApplicationConfig applicationConfig;

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
}
