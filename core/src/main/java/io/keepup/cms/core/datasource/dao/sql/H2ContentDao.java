package io.keepup.cms.core.datasource.dao.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.persistence.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * Service for working with {@link io.keepup.cms.core.persistence.Content} records using H2 database
 * as data source. Can be used for demonstration properties, e.g. if you want to run the app without
 * any additional components like production database.
 *
 * Service works in 'h2' profile.
 *
 * @author Fedor Sergeev
 * @since  2.0.0
 */
@Service
@Primary
@Profile("h2")
public class H2ContentDao extends SqlContentDao {
    /**
     * {@link Content} DAO implementation for H2 as data source
     *
     * @param reactiveNodeEntityRepository          DAO for {@link io.keepup.cms.core.datasource.sql.entity.NodeEntity} objects
     * @param reactiveNodeAttributeEntityRepository DAO for {@link io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity} objects
     * @param objectMapper                          mapper component
     * @param manager                               cache manager
     * @param adapter                               cache adapter
     */
    public H2ContentDao(ReactiveNodeEntityRepository reactiveNodeEntityRepository, ReactiveNodeAttributeEntityRepository reactiveNodeAttributeEntityRepository, ObjectMapper objectMapper, CacheManager manager, CacheAdapter adapter) {
        super(reactiveNodeEntityRepository, reactiveNodeAttributeEntityRepository, objectMapper, manager, adapter);
    }

    /**
     * Less effective method to fetch record parents, but it works for H2 database. Fetches all {@link Content} records
     * until the root or the specified offset record is found. Yet this method does not put found records in cache.
     *
     * @param id     first parent record identifier, in case of null empty Flux will be returned
     * @param offset depth of search, in case of null will be set to {@link Long#MAX_VALUE}
     * @return       publisher for the sequence of records inheriting each other till the record with the specified
     *               parent id (excluding this record itself)
     */
    @Override
    public Flux<Content> getContentParents(@NotNull Long id, @Nullable Long offset) {
        Objects.requireNonNull(id, "Null parameter id was passed to getContentParents method");

        int capacity = offset == null || offset > Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : offset.intValue();

        return getNodeEntityRepository().findById(id)
                .flatMap(entity -> getNodeEntityRepository().findById(entity.getParentId()))
                .expand(nodeEntity -> getNodeEntityRepository().findById(nodeEntity.getParentId()), capacity)
                .take(capacity)
                .flatMap(getNodeEntityPublisherFunction());
    }
}
