package io.keepup.plugins.catalog.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representing view rules to be displayed on the frontend
 */
public class Layout {

    /**
     * Common instance for {@code content()}. Used just for marking the layout, but object cannot be applied
     * for mapping fields to any views.
     */
    private static final Layout CONTENT = new Layout("contentLayout");

    private Long id;
    private String name;
    private String html;
    private String breadCrumbElementName;
    private List<LayoutApiAttribute> attributes;

    /**
     * Returns the default {@link Layout} object used to wrap catalog entities without concrete {@link CatalogEntity}
     * implementation.
     *
     * @return layout for wrapping {@link io.keepup.cms.core.persistence.Content} node
     */
    public static Layout content() {
        return CONTENT;
    }

    /**
     * New layout instance.
     */
    public Layout() {
        attributes = new ArrayList<>();
    }

    /**
     * Constructor used for static objects creation.
     *
     * @param name name of layout
     */
    private Layout(final String name) {
        this();
        setName(name);
    }

    /**
     * Get layout ID
     *
     * @return layout ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set layout ID
     *
     * @param id layout ID
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Get layout name
     *
     * @return name of layout
     */
    public String getName() {
        return name;
    }

    /**
     * Set layout name
     *
     * @param name name of layout
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get layout HTML template HTML representation
     *
     * @return layout HTML template HTML representation
     */
    public String getHtml() {
        return html;
    }

    /**
     * Set layout HTML template HTML representation
     *
     * @param html layout HTML template HTML representation
     */
    public void setHtml(final String html) {
        this.html = html;
    }

    /**
     * Get element attribute name to be mapped as a breadcrumb value
     *
     * @return breadcrumb name attribute key
     */
    public String getBreadCrumbElementName() {
        return breadCrumbElementName;
    }

    /**
     * Set element attribute name to be mapped as a breadcrumb value
     *
     * @param breadCrumbElementName breadcrumb name attribute key
     */
    public void setBreadCrumbElementName(final String breadCrumbElementName) {
        this.breadCrumbElementName = breadCrumbElementName;
    }

    /**
     * Get layout attributes associated with catalog entity
     *
     * @return layout attributes
     */
    public List<LayoutApiAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Set layout attributes associated with catalog entity
     *
     * @param attributes layout attributes
     */
    public void setAttributes(final List<LayoutApiAttribute> attributes) {
        this.attributes = attributes;
    }
}
