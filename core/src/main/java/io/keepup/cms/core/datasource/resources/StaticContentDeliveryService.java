package io.keepup.cms.core.datasource.resources;

import io.keepup.cms.core.commons.ApplicationConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * Service responsible for static resources distribution, e.g. images and HTML files
 *
 * @since 1.8
 */
@Service
public class StaticContentDeliveryService implements IContentDeliveryService {

    private final Map<StorageType, StorageAccessor<String>> storageProcessors = new EnumMap<>(StorageType.class);

    private final int storageType;

    /**
     * Constructor with injection of beans managed by IoC container.
     *
     * @param applicationConfig application configuration component
     */
    public StaticContentDeliveryService(final ApplicationConfig applicationConfig) {
        storageType = applicationConfig.getStorageType();
        final var staticPath = applicationConfig.getStaticPath();
        storageProcessors.put(StorageType.FILESYSTEM, new FilesystemFileProcessor(staticPath, applicationConfig.getDocumentRoot(), staticPath));
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.valueOf(storageType);
    }

    @Override
    public TransferOperationResult<String> store(File file, String relativePath) {
        return ofNullable(storageProcessors.get(getStorageType()))
                .map(processor -> processor.save(file, new DefaultUriBuilderFactory().builder().build("/", relativePath).toString()))
                .orElse(new TransferOperationResult<String>().error(format("No files processor specified for type %s", getStorageType())));
    }

    /**
     * Get the list of files in the specified directory
     *
     * @param extension file extension, eg html
     * @param paths     paths to files directories
     *
     * @return          files in the directory
     */
    public GetTreeFromStoreResult getByType(String extension, String... paths) {
        if (paths == null) {
            return GetTreeFromStoreResult.error("Paths parameter is not specified");
        }
        return ofNullable(storageProcessors.get(StorageType.valueOf(storageType)))
                .map(processor -> processor.getByType(extension, paths))
                .orElse(GetTreeFromStoreResult.error(format("No processor found for storage type %s", StorageType.valueOf(storageType))));
    }

    /**
     * Get the file by name in the specified directory
     *
     * @param filename filename
     * @param path     path to files directory
     * @return         files in the directory
     */
    public GetFileFromStoreResult getFile(String filename, String path) {
        if (path == null) {
            return GetFileFromStoreResult.error("Path parameter is not specified");
        }
        return ofNullable(storageProcessors.get(StorageType.valueOf(storageType)))
                .map(processor -> processor.getByName(filename, path))
                .orElse(GetFileFromStoreResult.error(format("No processor found for storage type %s", StorageType.valueOf(storageType))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStorageAccessor(final StorageType storageType, final StorageAccessor<String> processor) {
        storageProcessors.put(storageType, processor);
    }
}
