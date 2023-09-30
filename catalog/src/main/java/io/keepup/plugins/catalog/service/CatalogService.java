package io.keepup.plugins.catalog.service;

import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.datasource.resources.IContentDeliveryService;
import io.keepup.cms.core.datasource.resources.TransferOperationResult;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import io.keepup.cms.core.service.AbstractEntityOperationService;
import io.keepup.cms.core.service.EntityService;
import io.keepup.plugins.catalog.model.CatalogEntity;
import io.keepup.plugins.catalog.model.CatalogEntityWrapper;
import io.keepup.plugins.catalog.model.CreateCatalogEntityWithFileAttributeResponse;
import io.keepup.plugins.catalog.model.Layout;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import static io.keepup.plugins.catalog.model.CatalogEntityWrapper.success;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Component responsible for work with entities and layout views
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Service
@ConditionalOnProperty(prefix = "keepup.plugins.catalog", name = "enabled", havingValue = "true")
public class CatalogService extends AbstractEntityOperationService<CatalogEntity> {

    private final ApplicationConfig applicationConfig;
    private final LayoutService layoutService;
    private final IContentDeliveryService contentDeliveryService;


    /**
     * Constructor
     *
     * @param dataSourceFacade       data access object
     * @param layoutService          service for operations with view rules
     * @param contentDeliveryService service for delivering static files to static content storage
     * @param applicationConfig      base application configuration parameters
     */
    public CatalogService(final DataSourceFacade dataSourceFacade,
                          final LayoutService layoutService,
                          final IContentDeliveryService contentDeliveryService,
                          final ApplicationConfig applicationConfig) {
        this.dataSourceFacade = dataSourceFacade;
        this.layoutService = layoutService;
        this.contentDeliveryService = contentDeliveryService;
        this.applicationConfig = applicationConfig;

    }

    /**
     * Looks for entities specified by id and parentId and also for {@link Layout} objects
     * linked to them.
     *
     * @param id       entities identifier or parent identifier
     * @param children flag marking to look for entities by parent id
     * @return publisher for sequence of catalog wrappers
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
     * @param parentId parent record identifier, can not be null
     * @param offset   number of parent records to get, will be set to {@link Long#MAX_VALUE} if null
     * @return publisher for the parent records sequence
     */
    public Flux<CatalogEntity> getContentParents(final Long parentId, Long offset) {
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

    /**
     * Updates entity attribute as file (e.g. image or text file), process file binary content and
     * sets path to it as an attribute value.
     *
     * @param contentId     identifier of the entity to which the attribute update is applied
     * @param attributeName name of attribute to be updated
     * @param filePart      part that represents an uploaded file received in a multipart request
     * @return publisher that emits relative path to the saved file attribute
     */
    public Mono<String> updateContentAttributeAsFile(final long contentId,
                                                     final @NotNull String attributeName,
                                                     final @NotNull FilePart filePart) {
        return checkTempFile(filePart)
                .flatMap(file -> DataBufferUtils.join(filePart.content())
                        .map(dataBuffer -> dataBuffer.asByteBuffer().array())
                        .map(bytes -> writeFileToCache(file, bytes))
                        .flatMap(this::storeFileResult)
                        .flatMap(storeResult -> {
                            if (storeResult.isSuccess()) {
                                // нужно добавить относительный путь для доступа через веб
                                return dataSourceFacade.updateContentAttribute(contentId, attributeName, file.getName())
                                        .map(Object::toString);
                            } else {
                                return Mono.error(new IOException("Failed to save file: %s".formatted(storeResult.getMessage())));
                            }
                        }));
    }

    /**
     * Creates a new {@link CatalogEntity} with just one attribute with file type.
     *
     * @param parentId      parent record identifier
     * @param userId        owner ID
     * @param attributeName file attribute name
     * @param filePart      uploaded file received in a multipart request
     * @param layoutName    name of the display template by which the entity is created
     * @param type          corresponding Java class for object serialization
     * @return object containing created entity ID, file attribute name and relative path to saved file
     */
    public Mono<CreateCatalogEntityWithFileAttributeResponse> createEntityWithFileAttribute(final @NotNull Long parentId,
                                                                                            final @NonNull Long userId,
                                                                                            final @NotNull String attributeName,
                                                                                            final @NotNull FilePart filePart,
                                                                                            final @Nullable String layoutName,
                                                                                            final @Nullable String type) {
        return checkTempFile(filePart)
                .flatMap(file -> DataBufferUtils.join(filePart.content())
                        .map(dataBuffer -> dataBuffer.asByteBuffer().array())
                        .map(bytes -> writeFileToCache(file, bytes))
                        .flatMap(this::storeFileResult)
                        .flatMap(storeResult -> {
                            if (storeResult.isSuccess()) {
                                // нужно добавить относительный путь для доступа через веб
                                final var entity = new Node();
                                entity.setParentId(parentId);
                                entity.setOwnerId(userId);
                                entity.setAttribute(attributeName, storeResult.getPayload());
                                entity.setEntityType(type);
                                return layoutService.getByName(layoutName)
                                        .map(layout -> {
                                            layout.getAttributes()
                                                    .stream()
                                                    .filter(attr -> !entity.hasAttribute(attr.getKey()))
                                                    .forEach(attribute -> {
                                                        if (log.isDebugEnabled()) {
                                                            log.debug("Setting empty catalog attribute attribute with key = %s".formatted(attribute.getKey()));
                                                        }
                                                        entity.setAttribute(attribute.getKey(), null);
                                                    });
                                            return entity;
                                        })
                                        .flatMap(this::convert)
                                        .doOnNext(catalogEntity -> {
                                            catalogEntity.setLayoutName(layoutName);
                                            catalogEntity.setParentId(parentId);
                                        })
                                        .flatMap(ce -> save(ce, userId, parentId))
                                        .map(saved -> new CreateCatalogEntityWithFileAttributeResponse(saved.getId(), attributeName, storeResult.getPayload()));
                            } else {
                                return Mono.error(new IOException("Failed to save file: %s".formatted(storeResult.getMessage())));
                            }
                        }));
    }

    /**
     * Update {@link CatalogEntity} attributes.
     * @param id         record identifier
     * @param attributes list of attributes to be added or updated
     * @return result saved map of record attributes
     * @see CatalogEntity
     */
    public Mono<Map<String, Serializable>> updateContentAttributes(final long id, final @NotNull Map<String, Serializable> attributes) {
        return dataSourceFacade.updateContent(id, attributes);
    }

    @NotNull
    private File writeFileToCache(final File tempFile, final byte[] bytes) {
        try {
            Files.write(tempFile.toPath(), bytes);
        } catch (IOException e) {
            log.error("Error while writing file to cache: %s".formatted(e));
        }
        return tempFile;
    }

    private boolean isCatalogEntity(final Content node) {
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

    /**
     * Method will always return positive result, but the published {@link TransferOperationResult} can contain
     * errors that occurred while saving contentю
     *
     * @param file file to save
     * @return publisher that emits file transfer operation result
     */
    @NotNull
    private Mono<TransferOperationResult<String>> storeFileResult(final @NotNull File file) {
        final var storeResult = contentDeliveryService.store(file, EMPTY);
        try {
            Files.delete(file.getAbsoluteFile().toPath());
        } catch (IOException e) {
            log.warn("Failed to delete file %s".formatted(file.getAbsolutePath()));
        }
        return Mono.just(storeResult);
    }

    /**
     * Temporary solution that uses filesystem to store files received from the client.
     *
     * @param filePart uploaded file
     * @return publisher for file stored in the filesystem
     */
    @NotNull
    private Mono<File> checkTempFile(final @NotNull FilePart filePart) {

        final var cacheDirectory = new File(applicationConfig.getDump() + "uploadCache");
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdir();
        }
        final var tempFile = new File(cacheDirectory.getAbsolutePath() + "/" + UUID.randomUUID() + "-" + filePart.filename());
        if (tempFile.exists()) {
            try {
                Files.delete(tempFile.getAbsoluteFile().toPath());
            } catch (IOException e) {
                log.error("Failed to delete temp file %s before writing the new one: %s"
                        .formatted(tempFile.getAbsolutePath(), e.getMessage()));
                return Mono.empty();
            }
        }
        try {
            if (tempFile.createNewFile()) {
                log.info("New file %s has been created".formatted(tempFile.getAbsolutePath()));
            } else {
                log.error("Failed to create new file %s".formatted(tempFile.getAbsolutePath()));
            }

        } catch (IOException e) {
            return Mono.error(e);
        }
        return Mono.just(tempFile);
    }
}
