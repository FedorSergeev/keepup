package io.keepup.plugins.catalog.rest;

import io.keepup.cms.core.persistence.User;
import io.keepup.plugins.catalog.model.*;
import io.keepup.plugins.catalog.service.CatalogService;
import io.keepup.plugins.catalog.service.LayoutService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.HashSet;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;
import static reactor.core.publisher.Mono.just;

/**
 * REST controller witch provides CRUD operations for catalog entities (objects implementing interface
 * {@link io.keepup.plugins.catalog.model.CatalogEntity}) and their views described through
 * {@link io.keepup.plugins.catalog.model.Layout} objects
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@RestController
@RequestMapping("/catalog")
@ConditionalOnProperty(prefix = "keepup.plugins.catalog", name = "enabled", havingValue = "true")
public class CatalogController {

    private static final String SESSION_ID_WITH_RESPONSE = "Session id: %s, Send response: %s";
    private final Log log = LogFactory.getLog(getClass());
    private final CatalogService catalogService;
    private final LayoutService layoutService;

    public CatalogController(CatalogService catalogService,
                             LayoutService layoutService) {
        this.catalogService = catalogService;
        this.layoutService = layoutService;
    }

    /**
     * Get catalog entity possibly with it's children, with corresponding
     * {@link io.keepup.plugins.catalog.model.Layout} objects
     *
     * @param id       entity primary identifier
     * @param children flag for getting children as well
     * @return publisher signaling when entities are fetched
     * with {@link io.keepup.plugins.catalog.model.Layout} views
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CatalogEntityListWrapper<CatalogEntity>>> get(@PathVariable("id") final Long id,
                                                                             @RequestParam(value = "children",
                                                                                     required = false,
                                                                                     defaultValue = "false") final boolean children,
                                                                             @RequestParam(value = "parents",
                                                                                     required = false,
                                                                                     defaultValue = "false") final boolean parents,
                                                                             @RequestParam(value = "parentOffsetId",
                                                                                     required = false) Long offset,
                                                                             WebSession webSession) {
        log.info("Session id: %s, Received request to get entity id = %d  with%s children and with%s parents"
                .formatted(webSession.getId(), id, getWithoutChildrenSuffix(children), getWithoutChildrenSuffix(parents)));
        final var layoutNames = new HashSet<String>();

        return catalogService.getCatalogEntitiesWithLayouts(id, children)
                .filter(CatalogEntityWrapper::isSuccess)
                .map(CatalogEntityBaseWrapper::getEntity)
                .doOnNext(entity -> layoutNames.add(entity.getLayoutName()))
                .collectList()
                .flatMap(CatalogEntityListWrapper::success)
                .flatMap(wrapper -> getCatalogEntityListWrapperWithLayouts(layoutNames, wrapper))
                .flatMap(wrapper -> {
                    if (parents) {
                        return catalogService.getContentParents(id, ofNullable(offset).orElse(Long.MAX_VALUE))
                                .collectList()
                                .map(parentEntities -> {
                                    wrapper.setParents(parentEntities);
                                    return wrapper;
                                });
                    }
                    return Mono.just(wrapper);
                })
                .doOnNext(response -> tryLogResponse(response, webSession.getId()))
                .onErrorResume(errorResponse -> Mono.just(CatalogEntityListWrapper.error(errorResponse.getMessage())))
                .map(responseEntity -> responseEntity.isSuccess()
                            ? ok(responseEntity)
                            : internalServerError().body(responseEntity));
    }

    /**
     * Get all catalog entities and layouts.
     *
     * @return Publisher for ResponseEntity wrapping catalog entities with layouts
     */
    @GetMapping
    public Mono<ResponseEntity<CatalogEntityListWrapper<CatalogEntity>>> getAll(WebSession webSession) {
        log.info("Session id: %s, Received request to read all values".formatted(webSession.getId()));
        final var layoutNames = new HashSet<String>();
        return catalogService.getAllWithLayouts()
                .filter(CatalogEntityWrapper::isSuccess)
                .map(CatalogEntityBaseWrapper::getEntity)
                .doOnNext(entity -> layoutNames.add(entity.getLayoutName()))
                .collectList()
                .flatMap(CatalogEntityListWrapper::success)
                .flatMap(wrapper -> getCatalogEntityListWrapperWithLayouts(layoutNames, wrapper))
                .doOnNext(response -> tryLogResponse(response, webSession.getId()))
                .map(ResponseEntity::ok)
                .doOnError(response -> log.error("Session id: %s, Send error: %s"
                        .formatted(webSession.getId(), response.toString())));
    }

    /**
     * Create or update catalog entity. Logic is a little bit complicated because of possible inconsistency between
     * {@link CatalogEntity} interface and it's implementation.
     *
     * @param parentId      identifier of record witch will be current entity's parent node
     * @param catalogEntity entity to be saved or updated
     * @return Publisher for ResponseEntity wrapping the created catalog entity
     */
    @PostMapping(value = {"/{parentId}", EMPTY}, consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CatalogEntityWrapper<CatalogEntity>>> save(@PathVariable(name = "parentId", required = false) final Long parentId,
                                                                          @RequestBody CatalogEntity catalogEntity,
                                                                          WebSession webSession) {
        log.info("Session id: %s, Received request to save catalog entity %s"
                .formatted(webSession.getId(), catalogEntity.toString()));
        if (parentId != null && parentId < 0) {
            var errorMessage = "Parent identifier cannot be negative";
            log.error(errorMessage);
            return CatalogEntityWrapper.error(errorMessage)
                    .map(wrapper -> ResponseEntity.badRequest().body(wrapper));
        }
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(User.class::cast)
                .map(User::getId)
                .switchIfEmpty(just(0L))
                .flatMap(userId -> catalogService.save(catalogEntity, userId, parentId))
                .flatMap(savedCatalogEntity -> CatalogEntityWrapper.success(savedCatalogEntity, layoutService.getByName(savedCatalogEntity.getLayoutName())))
                .onErrorResume(CatalogController::applyError)
                .map(ResponseEntity::ok)
                .doOnNext(response -> log.info(SESSION_ID_WITH_RESPONSE
                        .formatted(webSession.getId(), response.toString())));
    }

    /**
     * Removes entity by id if is served by {@link CatalogService}
     *
     * @return publisher witch produces information about delete operation result
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<DeleteCatalogEntityRequestResponseWrapper>> delete(@PathVariable("id") final Long id,
                                                                                  WebSession webSession) {
        return catalogService.delete(id)
                .thenReturn(ResponseEntity.ok(DeleteCatalogEntityRequestResponseWrapper.success()))
                .onErrorResume(error -> Mono.just(internalServerError()
                        .body(DeleteCatalogEntityRequestResponseWrapper.error(error.getMessage()))))
                .doOnNext(response -> log.info(SESSION_ID_WITH_RESPONSE
                        .formatted(webSession.getId(), response.toString())));
    }

    // region private methods
    private String getWithoutChildrenSuffix(boolean children) {
        return children ? EMPTY : "out";
    }

    private void tryLogResponse(CatalogEntityListWrapper<CatalogEntity> response, String sessionId) {
        log.info(SESSION_ID_WITH_RESPONSE.formatted(sessionId, response.toString()));
    }

    private static Mono<? extends CatalogEntityWrapper<CatalogEntity>> applyError(Throwable throwable) {
        return CatalogEntityWrapper.error(throwable.getMessage());
    }

    @NotNull
    private Mono<CatalogEntityListWrapper<CatalogEntity>> getCatalogEntityListWrapperWithLayouts(HashSet<String> layoutNames, CatalogEntityListWrapper<CatalogEntity> wrapper) {
        if (layoutNames.isEmpty()) {
            return Mono.just(wrapper);
        }
        return layoutService.getByNames(layoutNames)
                .collectList()
                .map(layouts -> {
                    wrapper.setLayouts(layouts);
                    return wrapper;
                });
    }
    // endregion
}
