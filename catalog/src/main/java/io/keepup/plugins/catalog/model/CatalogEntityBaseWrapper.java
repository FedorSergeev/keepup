package io.keepup.plugins.catalog.model;

/**
 * Wrapper for entities implementing {@link CatalogEntity} interface and their {@link Layout}
 * objects linked by {@link CatalogEntity#getLayoutName()} value.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class CatalogEntityBaseWrapper<T extends CatalogEntity> {
    /**
     * Content abstraction
     */
    private T entity;
    /**
     * View rules
     */
    private Layout layout;

    /**
     * Get content
     *
     * @return content abstraction
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Set content
     *
     * @param entity content abstraction
     */
    public void setEntity(T entity) {
        this.entity = entity;
    }

    /**
     * Get view rules
     *
     * @return view rules
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Set view rules
     *
     * @param layout view rules
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }
}
