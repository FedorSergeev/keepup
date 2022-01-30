package io.keepup.plugins.catalog.service;

import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.service.EntityOperationServiceBase;
import io.keepup.cms.core.service.EntityService;
import io.keepup.plugins.catalog.model.CatalogEntity;
import io.keepup.plugins.catalog.model.CatalogEntityWrapper;
import io.keepup.plugins.catalog.model.Layout;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static io.keepup.plugins.catalog.model.CatalogEntityWrapper.success;

/**
 * Component responsible for work with entities and layout views
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Service
@ConditionalOnProperty(prefix = "keepup.plugins.catalog", name = "enabled", havingValue = "true")
public class CatalogService extends EntityOperationServiceBase<CatalogEntity> {

    private final LayoutService layoutService;

    public CatalogService(DataSourceFacade dataSourceFacade, LayoutService layoutService) {
        this.dataSourceFacade = dataSourceFacade;
        this.layoutService = layoutService;
    }

    /**
     * Looks for entities specified by id and parentId and also for {@link Layout} objects
     * linked to them.
     *
     * @param id             entities identifier or parent identifier
     * @param children       flag marking to look for entities by parent id
     * @return               publisher for sequence of catalog wrappers
     */
    public Flux<CatalogEntityWrapper<CatalogEntity>> getCatalogEntitiesWithLayouts(final Long id, boolean children) {
        if (id == null) {
            var errorMessage = "Can not find content by null id value";
            log.error(errorMessage);
            return CatalogEntityWrapper.error(errorMessage).flux();
        }
        Flux<CatalogEntity> entities;
        if (children) {
            log.debug("Looking for entity with id = %d and child nodes".formatted(id));
            entities = getWithChildren(id);

        } else {
            log.debug("Looking for entity with id = %d without child nodes");
            entities = get(id).flux();
        }
        return entities.flatMap(catalogEntity -> success(catalogEntity, layoutService.getByName(catalogEntity.getLayoutName())));
    }

    /**
     * Searches for the entity by it's identifier and also for it's child entities if they exist
     *
     * @param id entity identifier
     * @return publisher for the sequence of entities witch suit the identifier case
     */
    public Flux<CatalogEntity> getWithChildren(final Long id) {
        return dataSourceFacade.getContentByIdWithChildren(id)
                .filter(this::isCatalogEntity)
                .flatMap(this::convert);
    }

    /**
     * Fetch all entities with layouts
     *
     * @return all entities available for the user with their layout models
     */
    public Flux<CatalogEntityWrapper<CatalogEntity>> getAllWithLayouts() {
        return getAll()
                .flatMap(catalogEntity -> success(catalogEntity, layoutService.getByName(catalogEntity.getLayoutName())));
    }

    /**
     * Fetch a sequence of parents for the record specified by identifier.
     *
     * @param parentId  parent record identifier, can not be null
     * @param offset    number of parent records to get, will be set to {@link Long#MAX_VALUE} if null
     * @return          publisher for the parent records sequence
     */
    public Flux<CatalogEntity> getContentParents(Long parentId, Long offset) {
        if (parentId == null) {
            return Flux.empty();
        }
        if (offset == null) {
            offset = Long.MAX_VALUE;
        }
        return dataSourceFacade.getContentParents(parentId, offset)
                .filter(this::isCatalogEntity)
                .flatMap(this::convert);
    }

    private boolean isCatalogEntity(Content node)  {
        if (node.getEntityType() == null) {
            log.error("Record with id = %d is not managed by any implementation of %s"
                    .formatted(node.getId(), EntityService.class.getTypeName()));
            return false;
        }
        try {
            return CatalogEntity.class.isAssignableFrom(Class.forName(node.getEntityType()));
        } catch (ClassNotFoundException e) {
            log.error("Class %s not found, node with id = %d can not implement %s interface"
                    .formatted(node.getEntityType(), node.getId(), CatalogService.class.getTypeName()));
            return false;
        }
    }
}
