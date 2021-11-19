package io.keepup.plugins.catalog.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.persistence.User;
import io.keepup.plugins.catalog.LayoutService;
import io.keepup.plugins.catalog.model.*;
import io.keepup.plugins.catalog.service.CatalogService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashSet;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static reactor.core.publisher.Mono.just;

/**
 * REST controller witch provides CRUD operations for catalog entities (objects implementing interface
 * {@link io.keepup.plugins.catalog.model.CatalogEntity}) and their views described through
 * {@link io.keepup.plugins.catalog.model.Layout} objects
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@RestController
@RequestMapping("/catalog")
@ConditionalOnProperty(prefix = "keepup.plugins.catalog", name = "enabled", havingValue = "true")
public class CatalogController {

    private final Log log = LogFactory.getLog(getClass());
    private final CatalogService catalogService;
    private final LayoutService layoutService;
    private final ObjectMapper mapper;

    public CatalogController(CatalogService catalogService,
                             LayoutService layoutService,
                             ObjectMapper mapper) {
        this.catalogService = catalogService;
        this.layoutService = layoutService;
        this.mapper = mapper;
    }

    /**
     * Get catalog entity possibly with it's children, with corresponding
     * {@link io.keepup.plugins.catalog.model.Layout} objects
     *
     * @param id       entity primary identifier
     * @param children flag for getting children as well
     * @return         publisher signaling when entities are fetched
     * with {@link io.keepup.plugins.catalog.model.Layout} views
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CatalogEntityListWrapper<CatalogEntity>>> get(@PathVariable("id") final Long id,
                                                                             @RequestParam(value = "children",
                                                                                           required = false,
                                                                                           defaultValue = "false") final boolean children) {
        log.info("Received request to get entity id = %d  with %s children".formatted(id, getWithoutChildrenSuffix(children)));
        final var layoutNames = new HashSet<String>();
        return catalogService.getCatalogEntitiesWithLayouts(id, children)
                .filter(CatalogEntityWrapper::isSuccess)
                .map(CatalogEntityBaseWrapper::getEntity)
                .doOnNext(entity -> layoutNames.add(entity.getLayoutName()))
                .collectList()
                .flatMap(CatalogEntityListWrapper::success)
                .flatMap(wrapper -> getCatalogEntityListWrapperWithLayouts(layoutNames, wrapper))
                .map(ResponseEntity::ok)
                .onErrorResume(error -> CatalogEntityListWrapper.error(error.getMessage())
                        .doOnNext(this::tryLogResponse)
                        .map(ResponseEntity::ok));
    }

    /**
     * Get all catalog entities and layouts.
     *
     * @return Publisher for ResponseEntity wrapping catalog entities with layouts
     */
    @GetMapping
    public Mono<ResponseEntity<CatalogEntityListWrapper<CatalogEntity>>> getAll() {
        log.info("Received request to read all values");
        final var layoutNames = new HashSet<String>();
        return catalogService.getAllWithLayouts()
                .filter(CatalogEntityWrapper::isSuccess)
                .map(CatalogEntityBaseWrapper::getEntity)
                .doOnNext(entity -> layoutNames.add(entity.getLayoutName()))
                .collectList()
                .flatMap(CatalogEntityListWrapper::success)
                .flatMap(wrapper -> getCatalogEntityListWrapperWithLayouts(layoutNames, wrapper))
                .doOnNext(this::tryLogResponse)
                .map(ResponseEntity::ok);
    }

    /**
     * Create or update catalog entity. Logic is a little bit complicated because of possible inconsistency between
     * {@link CatalogEntity} interface and it's implementation.
     *
     * @param parentId      identifier of record witch will be current entity's parent node
     * @param catalogEntity entity to be saved or updated
     * @return              Publisher for ResponseEntity wrapping the created catalog entity
     */
    @PostMapping(value = {"/{parentId}", EMPTY}, consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CatalogEntityWrapper<CatalogEntity>>> save(@PathVariable(name = "parentId", required = false) final Long parentId,
                                                                          @RequestBody CatalogEntity catalogEntity) {
        log.info("Received request to save catalog entity %s".formatted(catalogEntity.toString()));
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
                .map(ResponseEntity::ok);
    }

    /**
     * Removes entity by id if is served by {@link CatalogService}
     *
     * @return publisher witch produces information about delete operation result
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<DeleteCatalogEntityRequestResponseWrapper>> delete(@PathVariable("id") final Long id) {
        return catalogService.delete(id)
                .thenReturn(ResponseEntity.ok(DeleteCatalogEntityRequestResponseWrapper.success()))
                .onErrorResume(error -> Mono.just(ResponseEntity.internalServerError()
                        .body(DeleteCatalogEntityRequestResponseWrapper.error(error.getMessage()))));
    }

    // region private methods
    private String getWithoutChildrenSuffix(boolean children) {
        return children ? EMPTY : "out";
    }

    private void tryLogResponse(CatalogEntityListWrapper<CatalogEntity> response) {
        try {
            log.info("Send response: %s".formatted(mapper.writeValueAsString(response)));
        } catch (JsonProcessingException e) {
            log.error("Failed to convert response: %s".formatted(e.toString()));
        }
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
