package io.keepup.cms.core.datasource.resources;

import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.logging.LogFactory.getLog;

/**
 * Universal set of get from storage result wrapper fields
 */
public abstract class AbstractGetFromStoreResult {

    /**
     * Success flag
     */
    private boolean success;

    /**
     * Additional response information
     */
    private String message;

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
    public void setSuccess(boolean success) {
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
    public void setMessage(String message) {
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
    public static <T extends AbstractGetFromStoreResult> T error(String message, Class<T> type) {
        final var staticLogger = getLog("GetFromSoreResult");
        try {
            final var result = type.getConstructor().newInstance();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        } catch (NoSuchMethodException e) {
            staticLogger.error("Type %s has no default constructors".formatted(type.getName()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            staticLogger.error("Failed to instantiate type %s: %s".formatted(type.getName(), ex.toString()));
        }
        return null;
    }
}
