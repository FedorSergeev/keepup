package io.keepup.plugins.catalog.model;

import java.io.Serializable;

/**
 * Aggregates response data after entity attribute has been updated with file as value.
 */
public record UpdateAttributeAsFileResponse(long contentId,
                                            String attributeName,
                                            Serializable attributeValue) {

    /**
     * Get entity ID
     *
     * @return entity ID
     */
    public long getContentId() {
        return contentId;
    }

    /**
     * Get attribute name
     *
     * @return name of attribute
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Get attribute value
     *
     * @return attribute value
     */
    public Serializable getAttributeValue() {
        return attributeValue;
    }
}
