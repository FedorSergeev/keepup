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
    /**
     * Some object wrapped in response
     */
    private T entity;

    /**
     * Constructs a new instance with the specified error and fail status
     *
     * @param error error description
     * @param <T>   type of object to be placed into response
     * @return      new response wrapper object
     */
    public static <T> KeepupResponseWrapper<T> error(final String error) {
        KeepupResponseWrapper<T> responseWrapper = new KeepupResponseWrapper<>();
        responseWrapper.setError(error);
        responseWrapper.setSuccess(false);
        return responseWrapper;
    }

    /**
     * Constructs a new successful response wrapper
     *
     * @param entity requested entity
     * @param <T>    type of object to be placed into response
     * @return       new wrapper object
     */
    public static <T> KeepupResponseWrapper<T> of(final T entity) {
        KeepupResponseWrapper<T> responseWrapper = new KeepupResponseWrapper<>();
        responseWrapper.setEntity(entity);
        responseWrapper.setSuccess(true);
        return responseWrapper;
    }

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
    public void setEntity(final T entity) {
        this.entity = entity;
    }
}
