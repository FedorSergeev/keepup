package io.keepup.cms.core.datasource.resources;

/**
 * Response wrapper for get file from storage operation
 */
public class GetFileFromStoreResult extends AbstractGetFromStoreResult {

    /**
     * Fetched file
     */
    private StoredFileData file;

    public StoredFileData getFile() {
        return file;
    }

    public void setFile(StoredFileData file) {
        this.file = file;
    }

    public static GetFileFromStoreResult error(String message) {
        return AbstractGetFromStoreResult.error(message, GetFileFromStoreResult.class);
    }
}
