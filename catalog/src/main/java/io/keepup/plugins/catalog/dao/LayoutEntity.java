package io.keepup.plugins.catalog.dao;

import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;

/**
 * Persistent entity for viewing the entities
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Entity
@org.springframework.data.relational.core.mapping.Table
@Table(name="layouts", indexes = {@Index(name = "IDX_LAYOUT_ID", columnList = "id")})
public class LayoutEntity implements Serializable {
    /**
     * This field is a part of the serialization mechanism defined by the Java Object Serialization Specification
     */
    @Serial
    private static final long serialVersionUID = 245237L;

    /**
     * Layout identifier
     */
    @Id
    private Long id;

    /**
     * Name of layout
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * HTML template for
     */
    @Column(name = "html", nullable = false)
    private String html;

    /**
     * Name of the bread crumb element for this layout
     */
    @Column(name = "breadcrumb_name")
    private String breadcrumbName;

    /**
     * JSON string with attributes
     */
    @Column(name = "attributes")
    private String attributes;

    /**
     * Get ID
     *
     * @return entity identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Get ID
     *
     * @param id entity identifier
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
     * Set name for the layout
     *
     * @param name layout name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get layout HTML template
     *
     * @return HTML template
     */
    public String getHtml() {
        return html;
    }

    /**
     * Set layout HTML template
     *
     * @param html HTML template
     */
    public void setHtml(final String html) {
        this.html = html;
    }

    /**
     * Get the key of element attribute to be placed as the breadcrumb
     *
     * @return breadcrumb element name
     */
    public String getBreadcrumbName() {
        return breadcrumbName;
    }

    /**
     * Set the key of element attribute to be placed as the breadcrumb
     *
     * @param breadcrumbName breadcrumb element name
     */
    public void setBreadcrumbName(final String breadcrumbName) {
        this.breadcrumbName = breadcrumbName;
    }

    /**
     * Get element attributes processed by current layout template as JSON string
     *
     * @return JSON representation of content element attributes
     */
    public String getAttributes() {
        return attributes;
    }

    /**
     * Set element attributes processed by current layout template as JSON string
     *
     * @param attributes JSON representation of content element attributes
     */
    public void setAttributes(final String attributes) {
        this.attributes = attributes;
    }
}
