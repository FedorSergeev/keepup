package io.keepup.plugins.adminui.rest.controller;

import com.wordnik.swagger.annotations.ApiOperation;
import io.keepup.plugins.adminui.service.AdminUiService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * Controller for catalog module. Finally should be moved to separate plugin depending on Admin UI plugin, but
 * it hasn't been moved in release 2.0.0 because of testing purposes.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@ConditionalOnProperty(prefix = "keepup.plugins.adminui", name = "enabled", havingValue = "true")
@RestController
@RequestMapping("apicatalog")
public class ApiCatalogController {

    private final Log log = LogFactory.getLog(getClass());
    private final AdminUiService adminUiService;

    public ApiCatalogController(AdminUiService adminUiService) {
        this.adminUiService = adminUiService;
    }

    /**
     * Get content of admin page with catalog module
     *
     * @return publisher for UI page content
     * @throws io.keepup.plugins.adminui.exception.ApiCatalogPageNotFoundException is thrown when no page was loaded by
     *         service component after application startup
     */
    @ApiOperation("Get HTML content of admin page with catalog module")
    @GetMapping("/**")
    public Mono<ResponseEntity<String>> apiCatalogPage(WebSession webSession) {
        log.info("Session id: %s,Catalog module for administrative panel has been requested"
                .formatted(webSession.getId()));
        return adminUiService.getApiCatalogPage()
                .map(ResponseEntity::ok)
                .doOnError(throwable -> ResponseEntity.internalServerError().body(throwable.getMessage()));
    }
}
