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
     * Response success status
     *
     * @return true if response is successful
     */
    /**
     * Check success of the result.
     *
     * @return true if result is successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Define response success status
     *
     * @param success true is response if successful
     */
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
     * Define response error description
     *
     * @param error error description
     */
    public void setError(String error) {
        this.error = error;
    }
}
