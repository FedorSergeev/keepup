package io.keepup.cms.core.datasource.resources;

import java.io.File;

/**
 * Maker interface for content delivery service.
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public interface IContentDeliveryService {
    /**
     * Get the specified storage type.
     *
     * @return specified storage type
     */
    StorageType getStorageType();

    /**
     * Perfotm the store operation for the file, puts it into the specified path.
     *
     * @param file         file to be saved
     * @param relativePath logical relative path to store the file
     * @return             operation result wrapper which defines success and describes error if operation faile
     */
    TransferOperationResult<String> store(File file, String relativePath);

    /**
     * Fetch files by type.
     *
     * @param extension file type
     * @param paths     paths to look for the file
     * @return          result wrapper
     * @see             GetTreeFromStoreResult
     */
    GetTreeFromStoreResult getByType(String extension, String... paths);

    /**
     * Get the single file from static files storage.
     *
     * @param filename name of file
     * @param path     logical path to file
     * @return         result wrapper
     * @see            GetTreeFromStoreResult
     */
    GetFileFromStoreResult getFile(String filename, String path);
}
