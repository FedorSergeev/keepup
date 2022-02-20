package io.keepup.cms.rest.controller;

/**
 * Wraps web request response with additional information fields so both error and success will
 * have the same structure
 *
 * @param <T> entity type
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class KeepupResponseWrapper<T> extends AbstractResponseWrapper {
    private T entity;

    /**
     * Get the entity stored in result object.
     *
     * @return entity stored in result object
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Define entity stored in result object.
     *
     * @param entity entity stored in result object
     */
    public void setEntity(T entity) {
        this.entity = entity;
    }
}
