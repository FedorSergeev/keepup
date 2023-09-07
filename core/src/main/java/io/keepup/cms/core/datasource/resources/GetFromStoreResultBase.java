package io.keepup.cms.core.datasource.resources;

import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.logging.LogFactory.getLog;

/**
 * Universal set of get from storage result wrapper fields
 */
public class GetFromStoreResultBase {

    /**
     * Success flag
     */
    private boolean success;

    /**
     * Additional response information
     */
    private String message;

    /**
     * Creating instances of current class is not allowed
     */
    protected GetFromStoreResultBase() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    /**
     * Get success flag.
     *
     * @return true if operation is successful.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Set success flag.
     *
     * @param success success flag, true when operation is successful.
     */
    public void setSuccess(final boolean success) {
        this.success = success;
    }

    /**
     * Get operation result message, can be null.
     *
     * @return message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message containing additional information about operation result.
     *
     * @param message text
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Create an error result of get object from storage operation.
     *
     * @param message error message
     * @param type    class of object to be found at storage
     * @param <T>     type of object to be found at storage
     * @return        get object operation result with error message
     */
    public static <T extends GetFromStoreResultBase> T error(final String message, final Class<T> type) {
        T result = null;
        final var staticLogger = getLog(GetFromStoreResultBase.class);
        try {
            result = type.getConstructor().newInstance();
            result.setSuccess(false);
            result.setMessage(message);
        } catch (NoSuchMethodException e) {
            if (staticLogger.isErrorEnabled()) {
                staticLogger.error("Type %s has no default constructors".formatted(type.getName()));
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            if (staticLogger.isErrorEnabled()) {
                staticLogger.error("Failed to instantiate type %s: %s".formatted(type.getName(), ex.toString()));
            }
        }
        return result;
    }
}
