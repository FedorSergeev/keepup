package io.keepup.plugins.catalog.model;

/**
 * Wrapper for entities implementing {@link CatalogEntity} interface and their {@link Layout}
 * objects linked by {@link CatalogEntity#getLayoutName()} value.
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class CatalogEntityBaseWrapper<T extends CatalogEntity> {
    private T entity;
    private Layout layout;

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }
}
