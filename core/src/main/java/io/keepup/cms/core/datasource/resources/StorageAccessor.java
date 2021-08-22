package io.keepup.cms.core.datasource.resources;

import java.io.File;

/**
 * Interface to be implemented by storage accessors of different types
 */
public interface StorageAccessor<T> {
    /**
     * Saves file to the storage
     *
     * @param file         file to be saved
     * @param relativePath relative path to file
     * @return             operation result
     */
    TransferOperationResult<T> save(File file, String relativePath);

    /**
     * Get the lis of elements in the specified folder by it's type
     *
     * @param type          file extension
     * @param relativePaths directories to search for files
     * @return              wrapped search result or error message in negative case
     */
    GetTreeFromStoreResult getByType(String type, String... relativePaths);

    /**
     * Get file specified by name and path
     *
     * @param name         filename
     * @param relativePath root directory
     * @return             store directory fetch result
     */
    GetFileFromStoreResult getByName(String name, String relativePath);
}
