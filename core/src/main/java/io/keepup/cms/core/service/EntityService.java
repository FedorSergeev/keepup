package io.keepup.cms.core.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>Basic CRUD operations interface.</p>
 *
 * @param <T> type of entities to be served
 * @author Fedor Sergeev
 * @since 1.8
 */
public interface EntityService<T> {
    /**
     * Save entity.
     *
     * @param entity  entity to be saved
     * @param ownerId entity owner ID
     * @return        Publisher emitting the saved entity
     */
    Mono<T> save(T entity, long ownerId);

    /**
     * Fetch an entity by primary ID.
     *
     * @param id entity ID
     * @return   Publisher emitting the entity specified by ID
     */
    Mono<T> get(Long id);

    /**
     * Find all entities served by current service.
     *
     * @return reactive stream publisher emitting all the entities served by this service
     */
    Flux<T> getAll();

    /**
     * Delete entity with specified ID.
     *
     * @param id entity ID
     * @return   reactive stream publisher emitting the {@link Void} object when delete operation is executed.
     */
    Mono<Void> delete(Long id);
}
