package io.keepup.cms.core.datasource.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Response wrapper for get files hierarchy from storage operation
 */
public class GetTreeFromStoreResult extends AbstractGetFromStoreResult {

    /**
     * Files list
     */
    private List<StoredFileData> files;

    public List<StoredFileData> getFiles() {
        return Optional.ofNullable(files).orElse(Collections.emptyList());
    }

    public void setFiles(List<StoredFileData> files) {
        this.files = files;
    }

    public GetTreeFromStoreResult() {
        this.files = new ArrayList<>();
    }

    public static GetTreeFromStoreResult error(String message) {
        return AbstractGetFromStoreResult.error(message, GetTreeFromStoreResult.class);
    }
}
