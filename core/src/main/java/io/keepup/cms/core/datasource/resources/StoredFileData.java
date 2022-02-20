package io.keepup.cms.core.datasource.resources;

import java.io.File;

/**
 * Wrapper for file stored at some storage
 */
public class StoredFileData {
    /**
     * Stored file
     */
    private File file;
    /**
     * Path in the storage
     */
    private final String path;

    /**
     * Instantoates the wrapper and logical path to it.
     *
     * @param file stored file
     * @param path logical path to file
     */
    public StoredFileData(File file, String path) {
        this.file = file;
        this.path = path;
    }

    /**
     * Get stored file.
     *
     * @return stored file
     */
    public File getFile() {
        return file;
    }

    /**
     * Get path in the storage where the file is stored.
     *
     * @return logical path to current file
     */
    public String getPath() {
        return path;
    }
}
