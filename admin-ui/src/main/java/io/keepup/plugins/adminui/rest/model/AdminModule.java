package io.keepup.plugins.adminui.rest.model;

/**
 * Meta information about admin panel module
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class AdminModule {
    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "{\"className\": \"%s\"}".formatted(className);
    }
}