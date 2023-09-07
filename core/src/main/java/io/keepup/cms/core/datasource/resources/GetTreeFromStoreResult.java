package io.keepup.cms.core.datasource.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Response wrapper for get files hierarchy from storage operation
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public class GetTreeFromStoreResult extends GetFromStoreResultBase {

    /**
     * Files list
     */
    private List<StoredFileData> files;

    /**
     * Get the files from operation result.
     *
     * @return files from operation result. If operation did not succeed, empty list will be returned.
     */
    public List<StoredFileData> getFiles() {
        return Optional.ofNullable(files).orElse(Collections.emptyList());
    }

    /**
     * Set the files from operation result.
     *
     * @param files from operation result
     */
    public void setFiles(List<StoredFileData> files) {
        this.files = files;
    }

    /**
     * New wrapper for getting the files tree from storage result.
     */
    public GetTreeFromStoreResult() {
        this.files = new ArrayList<>();
    }

    /**
     * Erroneous get files tree from static files content storage operation result.
     *
     * @param message error message
     * @return        operation result wrapper with error message
     */
    public static GetTreeFromStoreResult error(String message) {
        return GetFromStoreResultBase.error(message, GetTreeFromStoreResult.class);
    }
}
