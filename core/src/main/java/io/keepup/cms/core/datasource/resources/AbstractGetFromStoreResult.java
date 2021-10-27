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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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
