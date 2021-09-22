package io.keepup.plugins.catalog.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
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
     * @return LayoutEntity or
     */
    @Query("SELECT * FROM layouts AS layoutEntity WHERE layoutEntity.name = :name")
    Mono<LayoutEntity> findByName(@Param("name") final String name);
}
