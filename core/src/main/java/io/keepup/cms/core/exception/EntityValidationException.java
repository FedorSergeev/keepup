package io.keepup.cms.core.exception;

import java.io.Serial;

/**
 * Standard exception witch is thrown in case of entities validation failures.
 *
 * @author Fedor Sergeev
 * @since  2.0
 */
public class EntityValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Constructs a new entity validation exception with the specified detail message. The cause is not initialized.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the getMessage() method.
     */
    public EntityValidationException(String message) {
        super(message);
    }
}
