package io.keepup.plugins.catalog.rest;

import io.keepup.cms.core.persistence.User;
import io.keepup.cms.rest.controller.KeepupResponseWrapper;
import io.keepup.plugins.catalog.model.*;
import io.keepup.plugins.catalog.service.CatalogServiceAbstract;
import io.keepup.plugins.catalog.service.LayoutService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
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
    private final CatalogServiceAbstract catalogService;
    private final LayoutService layoutService;

    /**
     * Constructs a new service entity.
     *
     * @param catalogService business layer service for catalog entities
     * @param layoutService  component responsible for operations with entity views
     */
    public CatalogController(final CatalogServiceAbstract catalogService,
                             final LayoutService layoutService) {
        this.catalogService = catalogService;
        this.layoutService = layoutService;
    }

    /**
     * Get catalog entity possibly with it's children, with corresponding
     * {@link io.keepup.plugins.catalog.model.Layout} objects
     *
     * @param id         entity primary identifier
     * @param children   flag for getting children as well
     * @param parents    include parent nodes to the result list
     * @param offset     number of parent records to get, will be set to {@link Long#MAX_VALUE} if null
     * @param webSession server-side session data abstraction
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
                                                                                     required = false) final Long offset,
                                                                             final WebSession webSession) {
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
     * @param webSession server-side session data abstraction
     * @return Publisher for ResponseEntity wrapping catalog entities with layouts
     */
    @GetMapping
    public Mono<ResponseEntity<CatalogEntityListWrapper<CatalogEntity>>> getAll(final WebSession webSession) {
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
     * @param webSession    server-side session data abstraction
     * @return Publisher for ResponseEntity wrapping the created catalog entity
     */
    @PostMapping(value = {"/{parentId}", EMPTY}, consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CatalogEntityWrapper<CatalogEntity>>> save(@PathVariable(name = "parentId", required = false) final Long parentId,
                                                                          final @RequestBody CatalogEntity catalogEntity,
                                                                          final WebSession webSession) {
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
     * Upload file and set a link to it as an attribute for the specified entity.
     *
     * @param id         identifier of the entity whose attribute is to be updated as a file
     * @param name       name if file to be set as a new entity attribute
     * @param isPublic   flag defines whether a new file shoud be visible worldwide or not
     * @param filePart   publisher for file part object emitting
     * @param webSession server-side session abstraction
     * @return publisher for link to the file
     */
    @PostMapping(value = {"/{id}/attribute"}, consumes = MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<UpdateAttributeAsFileResponse>> updateContentAttributeAsFile(@PathVariable("id") final Long id,
                                                                     @RequestParam("name") final String name,
                                                                     @RequestParam(value = "public", required = false, defaultValue = "false") final boolean isPublic,
                                                                     @RequestPart("file") Mono<FilePart> filePart,
                                                                     final WebSession webSession) {
        log.info("Session id: %s, Received request to update catalog entity %s as file"
                .formatted(webSession.getId(), id));
        return filePart.flatMap(part -> catalogService.updateContentAttributeAsFile(id, name, part))
                .map(attributeValue -> new UpdateAttributeAsFileResponse(id, name, attributeValue))
                .map(ResponseEntity::ok);
    }

    /**
     * Set new values only for the specified keys attributes.
     *
     * @param id entity identifier
     * @param attributes entity attributes to be updated
     * @param webSession server-side web session representation
     * @return publisher witch emits an updated entity
     */
    @PostMapping("/{id}/updateAttributes")
    public Mono<ResponseEntity<KeepupResponseWrapper<Map<String, Serializable>>>> updateContentAttributes(@PathVariable("id") final Long id,
                                                                              final Map<String, Serializable> attributes,
                                                                              final WebSession webSession) {
        log.info("Session id: %s, Received request to update catalog entity %s attributes"
                .formatted(webSession.getId(), id));
        return catalogService.updateContentAttributes(id, attributes)
                .map(result -> ResponseEntity.ok(KeepupResponseWrapper.of(result)))
                .onErrorReturn(ResponseEntity.internalServerError().body(KeepupResponseWrapper.error("Failed to update attributes for entity %d".formatted(id))));
    }


    /**
     * Removes entity by id if is served by {@link CatalogServiceAbstract}
     * @param id         entity identifier
     * @param webSession server-side session data abstraction
     * @return publisher witch produces information about delete operation result
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<DeleteCatalogEntityRequestResponseWrapper>> delete(@PathVariable("id") final Long id,
                                                                                  final WebSession webSession) {
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

    private void tryLogResponse(final CatalogEntityListWrapper<CatalogEntity> response, final String sessionId) {
        log.info(SESSION_ID_WITH_RESPONSE.formatted(sessionId, response.toString()));
    }

    private static Mono<? extends CatalogEntityWrapper<CatalogEntity>> applyError(final Throwable throwable) {
        return CatalogEntityWrapper.error(throwable.getMessage());
    }

    @NotNull
    private Mono<CatalogEntityListWrapper<CatalogEntity>> getCatalogEntityListWrapperWithLayouts(final Set<String> layoutNames,
                                                                                                 final CatalogEntityListWrapper<CatalogEntity> wrapper) {
        if (layoutNames.isEmpty()) {
            return Mono.just(wrapper);
        }
        return layoutService.getByNames(layoutNames.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()))
                .collectList()
                .map(layouts -> {
                    wrapper.setLayouts(layouts);
                    return wrapper;
                });
    }
    // endregion
}
