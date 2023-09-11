package io.keepup.cms.core.datasource.resources;

/**
 * Response wrapper for get file from storage operation
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public class GetFileFromStoreResult extends GetFromStoreResultBase {

    /**
     * Fetched file
     */
    private StoredFileData file;
    /**
     * Get file to be stored.
     *
     * @return file to be stored
     */
    public StoredFileData getFile() {
        return file;
    }
    /**
     * Set file to be stored.
     *
     * @param file file to be stored
     */
    public void setFile(StoredFileData file) {
        this.file = file;
    }
    /**
     * Error result of get file operation.
     *
     * @param message additional information about operation
     * @return        operation result wrapper with error message
     */
    public static GetFileFromStoreResult error(String message) {
        return GetFromStoreResultBase.error(message, GetFileFromStoreResult.class);
    }
}
