package io.keepup.cms.rest.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps web request response with additional information fields so both error and success will
 * have the same structure.
 *
 * @param <T> entity type
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class KeepupResponseListWrapper<T> extends AbstractResponseWrapper {
    private List<T> entities;

    /**
     * Default constructor. Empty list of entities is initialized.
     */
    public KeepupResponseListWrapper() {
        entities = new ArrayList<>();
    }

    /**
     * Get the list of entities.
     *
     * @return entities stored in response object
     */
    public List<T> getEntities() {
        return entities;
    }

    /**
     * Define the list of entities.
     *
     * @param entities entities stored in response object
     */
    public void setEntities(List<T> entities) {
        this.entities = entities;
    }
}
