package io.keepup.plugins.catalog.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Extension of Serializable abstraction with additional method for fetching the model view.
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public interface CatalogEntity extends Serializable {

    /**
     * @return primary entity identifier
     */
    Long getId();

    /**
     * Fetches the {@link Layout} name for view witch suits current object.
     *
     * @return name of layout entity
     */
    String getLayoutName();
}
