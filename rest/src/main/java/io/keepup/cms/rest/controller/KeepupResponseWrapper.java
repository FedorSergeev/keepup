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

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
