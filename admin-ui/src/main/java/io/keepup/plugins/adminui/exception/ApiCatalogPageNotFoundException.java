package io.keepup.plugins.adminui.exception;

/**
 * Exception is raised when for some reason API catalog page content was not loaded from resources
 * and then was requested at runtime.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class ApiCatalogPageNotFoundException extends RuntimeException {
    /**
     * Constructor
     * @param message detailed message
     */
    public ApiCatalogPageNotFoundException(String message) {
        super(message);
    }
}
