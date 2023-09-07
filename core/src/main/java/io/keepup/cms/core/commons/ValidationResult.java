package io.keepup.cms.core.commons;

/**
 * Abstraction that helps to process some operation result.
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public class ValidationResult {
    /**
     * Success flag
     */
    protected boolean success;
    /**
     * Validation description
     */
    protected String message;

    /**
     * Builds a new object with specified parameters.
     *
     * @param success flag describing result status
     * @param message detailed message
     * @return validation result object
     */
    public static ValidationResult build(final boolean success, final String message) {
        final var validationResult = new ValidationResult();
        validationResult.success = success;
        validationResult.message = message;
        return validationResult;
    }

    /**
     * Creates successful validation result.
     * @return successful validation result
     */
    public static ValidationResult passed() {
        final var validationResult = new ValidationResult();
        validationResult.success = true;
        return validationResult;
    }

    /**
     * Creates unsuccessful validation result with description.
     *
     * @param message description of the reason why validation failed
     * @return        unsuccessful validation result with description
     */
    public static ValidationResult error(final String message) {
        final var validationResult = new ValidationResult();
        validationResult.success = false;
        validationResult.message = message;
        return validationResult;
    }

    /**
     * Checks if validation successful.
     *
     * @return true if validation succeeded
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the validation description message.
     * Used only for negative cases, when isSuccess() returns false.
     *
     * @return validation result detailed description
     */
    public String getMessage() {
        return message;
    }
}
