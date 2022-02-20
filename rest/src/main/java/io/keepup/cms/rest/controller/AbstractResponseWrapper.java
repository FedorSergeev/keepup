package io.keepup.cms.rest.controller;

/**
 * Basic response wrapper for REST requests
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class AbstractResponseWrapper {
    /**
     * Success flag
     */
    protected boolean success;
    /**
     * Error message
     */
    protected String error;

    /**
     * Check success of the result.
     *
     * @return true if result is successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Define success of the result.
     *
     * @param success true if result is successful
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Get error message.
     *
     * @return error message
     */
    public String getError() {
        return error;
    }

    /**
     * Set error message.
     *
     * @param error  error message
     */
    public void setError(String error) {
        this.error = error;
    }
}
