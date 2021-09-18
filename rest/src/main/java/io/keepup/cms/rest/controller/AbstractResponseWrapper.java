package io.keepup.cms.rest.controller;

/**
 * Basic response wrapper for REST requests
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
public class AbstractResponseWrapper {
    private boolean success;
    private String error;

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
