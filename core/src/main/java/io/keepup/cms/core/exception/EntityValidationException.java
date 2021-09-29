package io.keepup.cms.core.exception;

/**
 * Standard exception witch is thrown in case of entities validation failures.
 *
 * @author Fedor Sergeev
 * @since  2.0
 */
public class EntityValidationException extends RuntimeException {
    public EntityValidationException(String message) {
        super(message);
    }
}
