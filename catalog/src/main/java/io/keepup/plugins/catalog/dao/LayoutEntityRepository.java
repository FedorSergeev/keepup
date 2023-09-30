package io.keepup.plugins.catalog.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DAO for object layouts
 */
@Repository
public interface LayoutEntityRepository extends ReactiveCrudRepository<LayoutEntity, Long> {
    /**
     * Get entity by name. name is unique, corresponding constraint is added to database via liquibase scripts
     *
     * @param name name of Layout, should be unique
     * @return LayoutEntity or empty Mono if the result set is empty
     */
    @Query("SELECT id, name, html, breadcrumb_name, attributes FROM layouts AS layoutEntity WHERE layoutEntity.name = :name")
    Mono<LayoutEntity> findByName(@Param("name") final String name);

    /**
     * Get {@link LayoutEntity} entities by names
     *
     * @param names collection of Layout names
     * @return      publisher for many LayoutEntity objects or empty Publisher if the result set is empty
     */
    @Query("SELECT id, name, html, breadcrumb_name, attributes FROM layouts AS layoutEntity WHERE layoutEntity.name IN (:names)")
    Flux<LayoutEntity> findByNames(@Param("names") final Iterable<String> names);
}
