package io.keepup.plugins.catalog.model;

/**
 * Aggregates file link and entity ID as we don't know anything about exact object type.
 * @author Fedor Sergeev
 * @since 2.0.1
 */
public record CreateCatalogEntityWithFileAttributeResponse(long id,
                                                           String fileAttributeName,
                                                           String fileAttributeValue) {

    /**
     * Get ID
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * Get name of file attribute
     * @return file attribute name
     */
    public String getFileAttributeName() {
        return fileAttributeName;
    }

    /**
     * Get saved file attribute value
     * @return file attribute value
     */
    public String getFileAttributeValue() {
        return fileAttributeValue;
    }
}
