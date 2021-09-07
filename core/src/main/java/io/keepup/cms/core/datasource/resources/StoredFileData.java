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

    public StoredFileData(File file, String path) {
        this.file = file;
        this.path = path;
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }
}
