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
 * @since 2.0
 */
@Entity
@Table(name="layouts", indexes = {@Index(name = "IDX_LAYOUT_ID", columnList = "id")})
public class LayoutEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 245237L;

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
     * JSON string with attributes
     */
    @Column(name = "attributes")
    private String attributes;

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

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }
}
