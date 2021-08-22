package io.keepup.cms.core.commons;

/**
 * Abstraction that helps to process some operation result
 */
public class ValidationResult {
    protected boolean success;
    protected String message;

    protected ValidationResult() {
    }

    /**
     * Builds a new object with specified parameters
     *
     * @param success flag describing result status
     * @param message detailed message
     * @return validation result object
     */
    public static ValidationResult build(boolean success, String message) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.success = success;
        validationResult.message = message;
        return validationResult;
    }

    public static ValidationResult success() {
        ValidationResult validationResult = new ValidationResult();
        validationResult.success = true;
        return validationResult;
    }

    public static ValidationResult error(String message) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.success = false;
        validationResult.message = message;
        return validationResult;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
