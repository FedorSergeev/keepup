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
     * new layout instance.
     */
    public Layout() {
        attributes = new ArrayList<>();
    }

    /**
     * Constructor used for static objects creation.
     *
     * @param name name of layout
     */
    private Layout(String name) {
        this();
        setName(name);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getBreadCrumbElementName() {
        return breadCrumbElementName;
    }

    public void setBreadCrumbElementName(String breadCrumbElementName) {
        this.breadCrumbElementName = breadCrumbElementName;
    }

    public List<LayoutApiAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<LayoutApiAttribute> attributes) {
        this.attributes = attributes;
    }
}
