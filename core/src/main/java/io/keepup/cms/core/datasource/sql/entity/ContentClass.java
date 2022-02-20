package io.keepup.cms.core.datasource.sql.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.*;

/**
 * Link between {@link NodeEntity} and Java classes representing business entity stored in database
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Entity
@org.springframework.data.relational.core.mapping.Table
@Table(name="entity_classes", indexes = {
        @Index(name = "IDX_CONTENT_CLASS_ID", columnList = "id"),
        @Index(name = "IDX_FILE_CONTENT_ID", columnList = "content_id")})
public class ContentClass {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_classes_seq_generator")
    @SequenceGenerator(name = "file_seq_generator", sequenceName = "content_classes_seq", allocationSize=1)
    private Long id;
    @Column(name = "content_id", nullable = false)
    private Long contentId;
    @Column(name = "class_name", nullable = false)
    private String className;

    /**
     * Creates a new {@link ContentClass} entity with specified {@link io.keepup.cms.core.persistence.Content} identifier
     * and name of Java type as String.
     *
     * @param contentId {@link io.keepup.cms.core.persistence.Content} record identifier
     * @param className name of the Java class stored by the {@link io.keepup.cms.core.persistence.Content} record
     */
    public ContentClass(Long contentId, String className) {
        this.contentId = contentId;
        this.className = className;
    }

    /**
     * Get entity ID.
     *
     * @return entity ID
     */
    public Long getId() {
        return id;
    }
    /**
     * Set entity ID.
     *
     * @param id entity ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Get entity {@link io.keepup.cms.core.persistence.Content} ID.
     *
     * @return ID of {@link io.keepup.cms.core.persistence.Content} record linked to current entity
     */
    public Long getContentId() {
        return contentId;
    }
    /**
     * Set entity {@link io.keepup.cms.core.persistence.Content} ID.
     *
     * @param contentId  ID of {@link io.keepup.cms.core.persistence.Content} record linked to current entity
     */
    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }
    /**
     * Get name of the Java class represented by {@link io.keepup.cms.core.persistence.Content} record.
     *
     * @return name of the Java class represented by {@link io.keepup.cms.core.persistence.Content} record
     */
    public String getClassName() {
        return className;
    }
    /**
     * Set name of the Java class represented by {@link io.keepup.cms.core.persistence.Content} record.
     *
     * @param className name of the Java class represented by {@link io.keepup.cms.core.persistence.Content} record
     */
    public void setClassName(String className) {
        this.className = className;
    }
}
