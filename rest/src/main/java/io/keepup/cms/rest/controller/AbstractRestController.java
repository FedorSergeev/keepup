package io.keepup.cms.rest.controller;

import io.keepup.cms.core.persistence.User;
import io.keepup.cms.core.service.EntityOperationServiceBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Basic implementation of Reactive REST controller responsible for work with objects managed by
 * extensions of {@link io.keepup.cms.core.service.EntityOperationServiceBase}
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
public abstract class AbstractRestController<T> {

    private static final String NULL = "NULL";
    private final Log log = LogFactory.getLog(getClass());
    protected final EntityOperationServiceBase<T> operationService;

    protected AbstractRestController(EntityOperationServiceBase<T> operationService) {
        this.operationService = operationService;
    }

    // region public API

    /**
     * Get all entities with the specified type and served by the specified operation service
     *
     * @return Mono signaling when the entities are ready or empty/error
     */
    @GetMapping
    public Mono<ResponseEntity<KeepupResponseListWrapper<T>>> getAll() {
        log.debug("Received GET request for all entities served by %s".formatted(ofNullable(operationService)
                .map(Object::getClass)
                .map(Class::getTypeName)
                .orElse(NULL)));
        return ofNullable(operationService).map(service -> service.getAll()
                .collect(Collectors.toList())
                .map(this::getSuccessBody)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.empty())
                .doOnError(throwable -> log.error("Error while calling objects from %s"
                                           .formatted(getTypeName())))
                .onErrorResume(throwable -> Mono.just(ResponseEntity
                        .internalServerError()
                        .body(getErrorListWrapper(throwable.toString())))))
                .orElse(Mono.just(ResponseEntity
                        .internalServerError()
                        .body(getErrorListWrapper("No operation service specified"))));
    }

    /**
     * Get an entity with the specified type and served by the specified operation service
     *
     * @return Mono signaling when the entity is ready or empty/error
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<KeepupResponseWrapper<T>>> get(@PathVariable("id") final Long id) {
        log.debug("Received GET request to read entity with id %d".formatted(id));
        return operationService.get(id)
                .map(this::getSuccessBody)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.empty())
                .doOnError(throwable -> log.error("Error while calling object from %s"
                        .formatted(getTypeName())))
                .onErrorResume(throwable -> Mono.just(ResponseEntity
                        .internalServerError()
                        .body(getErrorWrapper(throwable.toString()))));
    }

    /**
     * Creates a new entity or updates an exiting one if object with the specified id already exists.
     *
     * @param entity entity to create or update
     * @return wrapper with information about operation result
     */
    @PostMapping
    public Mono<ResponseEntity<KeepupResponseWrapper<T>>> save(@RequestBody final T entity) {
        log.debug("Received POST request to save entity %s".formatted(entity.toString()));
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(principal -> (io.keepup.cms.core.persistence.User)principal)
                .map(User::getId)
                .flatMap(userId -> operationService.save(entity, userId))
                .map(this::getSuccessBody)
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes entity specified by identifier
     *
     * @param id entity identifier
     * @return Mono signaling when operation is done
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") final Long id) {
        log.debug("Received DELETE request to remove entity with id %d".formatted(id));
        return operationService.delete(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // endregion

    private KeepupResponseListWrapper<T> getErrorListWrapper(String message) {
        var keepupResponseWrapper = new KeepupResponseListWrapper<T>();
        keepupResponseWrapper.setError(message);
        keepupResponseWrapper.setSuccess(false);
        return keepupResponseWrapper;
    }

    private KeepupResponseListWrapper<T> getSuccessBody(List<T> entities) {
        var keepupResponseWrapper = new KeepupResponseListWrapper<T>();
        keepupResponseWrapper.setSuccess(true);
        keepupResponseWrapper.getEntities().addAll(entities);
        return keepupResponseWrapper;
    }

    private KeepupResponseWrapper<T> getSuccessBody(T entity) {
        var keepupResponseWrapper = new KeepupResponseWrapper<T>();
        keepupResponseWrapper.setSuccess(true);
        keepupResponseWrapper.setEntity(entity);
        return keepupResponseWrapper;
    }

    private KeepupResponseWrapper<T> getErrorWrapper(String message) {
        var keepupResponseWrapper = new KeepupResponseWrapper<T>();
        keepupResponseWrapper.setError(message);
        keepupResponseWrapper.setSuccess(false);
        return keepupResponseWrapper;
    }

    @NotNull
    private String getTypeName() {
        return ofNullable(operationService)
                .map(Object::getClass)
                .map(Class::getTypeName)
                .orElse(NULL);
    }
}
