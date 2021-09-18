package io.keepup.cms.rest.controller;

import io.keepup.cms.core.service.EntityOperationServiceBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Basic implementation of Reactive REST controller responsible for work with objects managed by
 * extensions of {@link io.keepup.cms.core.service.EntityOperationServiceBase}
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
public abstract class AbstractRestController<T> {

    private final Log log = LogFactory.getLog(getClass());
    private final EntityOperationServiceBase<T> operationService;

    protected AbstractRestController(EntityOperationServiceBase<T> operationService) {
        this.operationService = operationService;
    }

    /**
     * Get all entities with the specified type and served by the specified operation service
     *
     * @return Mono signaling when the entities are ready or empty/error
     */
    @GetMapping
    public Mono<ResponseEntity<KeepupResponseListWrapper<T>>> getAll() {
        log.debug("[GET] request for all entities");
        return operationService.getAll()
                .collect(Collectors.toList())
                .map(this::getSuccessBody)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.empty())
                .doOnError(throwable -> log.error("Error while calling objects from %s"
                                           .formatted(operationService.getClass().getTypeName())))
                .onErrorResume(throwable -> Mono.just(ResponseEntity
                        .internalServerError()
                        .body(getErrorListWrapper(throwable.toString()))));
    }

    /**
     * Get an entity with the specified type and served by the specified operation service
     *
     * @return Mono signaling when the entity is ready or empty/error
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<KeepupResponseWrapper<T>>> get(@PathVariable("id") final Long id) {
        return operationService.get(id)
                .map(this::getSuccessBody)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.empty())
                .doOnError(throwable -> log.error("Error while calling object from %s"
                        .formatted(operationService.getClass().getTypeName())))
                .onErrorResume(throwable -> Mono.just(ResponseEntity
                        .internalServerError()
                        .body(getErrorWrapper(throwable.toString()))));
    }

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
}
