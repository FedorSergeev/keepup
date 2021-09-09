package io.keepup.cms.rest.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps web request response with additional information fields so both error and success will
 * have the same structure
 *
 * @param <T> entity type
 * @author Fedor Sergeev
 * @since 2.0
 */
public class KeepupResponseListWrapper<T> extends AbstractResponseWrapper {
    private List<T> entities;
    private boolean success;
    private String error;

    public KeepupResponseListWrapper() {
        entities = new ArrayList<>();
    }

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
