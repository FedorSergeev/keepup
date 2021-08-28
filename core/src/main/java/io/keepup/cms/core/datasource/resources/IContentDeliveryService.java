package io.keepup.cms.core.datasource.resources;

import java.io.File;

/**
 * Maker interface for content delivery service
 */
public interface IContentDeliveryService {
    StorageType getStorageType();
    TransferOperationResult<String> store(File file, String relativePath);
    GetTreeFromStoreResult getByType(String extension, String... paths);
    GetFileFromStoreResult getFile(String filename, String path);
}
