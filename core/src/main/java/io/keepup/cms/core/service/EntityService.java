package io.keepup.cms.core.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>Basic CRUD operations interface.</p>
 * @param <T> type of entities to be served
 */
public interface EntityService<T> {
    Mono<T> save(T entity, long ownerId);
    Mono<T> get(Long id);
    Flux<T> getAll();
    Mono<Void> delete(Long id);
}
