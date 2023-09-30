package io.keepup.plugins.catalog.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Extension of Serializable abstraction with additional method for fetching the model view.
 *
 * Please do not forget to register your subtype in {@link com.fasterxml.jackson.databind.ObjectMapper},
 * as using fully-qualified Java class name as the type identifier can lead to CVE-2017-4995, CVE-2018-19362
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface CatalogEntity extends Serializable {

    /**
     * @return primary entity identifier
     */
    Long getId();

    /**
     * Get node parent ID
     * @return parent ID
     */
    Long getParentId();

    /**
     * Define entity parent node ID
     * @param parentId parent node ID for current entity
     */
    void setParentId(Long parentId);

    /**
     * Fetches the {@link Layout} name for view witch suits current object.
     *
     * @return name of layout entity
     */
    String getLayoutName();

    /**
     * Needed to define the layout template used by service layer to created an entity
     * @param layoutName name of layout template
     */
    void setLayoutName(String layoutName);
}
