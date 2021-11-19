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

    public ContentClass(Long contentId, String className) {
        this.contentId = contentId;
        this.className = className;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
