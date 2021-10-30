package io.keepup.cms.core.datasource.sql.entity;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Arrays;

import static java.util.Optional.ofNullable;

/**
 * Data access object for static files.
 * @author Fedor Sergeev
 */
@Entity
@org.springframework.data.relational.core.mapping.Table
@Table(name="files", indexes = {
        @Index(name = "IDX_FILE_ID", columnList = "id"),
        @Index(name = "IDX_FILE_CONTENT_ID", columnList = "contentid")})
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_seq_generator")
    @SequenceGenerator(name = "file_seq_generator", sequenceName = "file_seq",allocationSize=1)
    private Long id;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "contentId", nullable = false)
    private Long contentId;

    @Column(name = "content", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    public byte[] content;

    @Column(name = "creation_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDate creationTime;

    @Column(name = "modification_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDate modificationTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public byte[] getContent() {
        return Arrays.copyOf ( content, content.length);
    }

    public void setContent(byte[] content) {
        this.content = Arrays.copyOf ( content, content.length);
    }

    public LocalDate getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDate creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDate getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(LocalDate modificationTime) {
        this.modificationTime = modificationTime;
    }

    public FileEntity() {}

    public FileEntity(String fileName, String path, Long contentId, LocalDate creationTime, LocalDate modificationTime, byte[] content) {
        this.fileName = fileName;
        this.path = path;
        this.contentId = contentId;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
        this.content = ofNullable(content)
                .map(contentBytes -> Arrays.copyOf(contentBytes, contentBytes.length))
                .orElse(null);
    }
}

