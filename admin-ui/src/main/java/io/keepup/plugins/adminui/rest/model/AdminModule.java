package io.keepup.plugins.adminui.rest.model;

/**
 * Meta information about admin panel module
 *
 * @author Fedor Sergeev
 * @since  2.0.0
 */
public record AdminModule(String className) {

    @Override
    public String toString() {
        return "{\"className\": \"%s\"}".formatted(className);
    }
}