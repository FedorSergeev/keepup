package io.keepup.cms.core.datasource.sql.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Arrays;

import static java.util.Optional.ofNullable;

/**
 * Data access object for static files.
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
@Entity
@org.springframework.data.relational.core.mapping.Table
@Table(name="files", indexes = {
        @Index(name = "IDX_FILE_ID", columnList = "id"),
        @Index(name = "IDX_FILE_CONTENT_ID", columnList = "contentid")})
public class FileEntity {
    /**
     * Primary identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_seq_generator")
    @SequenceGenerator(name = "file_seq_generator", sequenceName = "file_seq",allocationSize=1)
    private Long id;
    /**
     * Name of file
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;
    /**
     * Logical path to file
     */
    @Column(name = "path", nullable = false)
    private String path;
    /**
     * Identifier of {@link io.keepup.cms.core.persistence.Content} record linked to this file
     */
    @Column(name = "content_id", nullable = false)
    private Long contentId;
    /**
     * File content serialized as byte array
     */
    @Column(name = "content", nullable = false)
    @Lob
    public byte[] content;
    /**
     * File creation date
     */
    @Column(name = "creation_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDate creationTime;
    /**
     * File modification date
     */
    @Column(name = "modification_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDate modificationTime;

    /**
     * Get file primary identifier.
     *
     * @return file primary identifier.
     */
    public Long getId() {
        return id;
    }

    /**
     * Define file primary identifier.
     *
     * @param id file primary identifier.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the filename.
     *
     * @return name of file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Define the filename.
     *
     * @param fileName name of file
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get logical path to file.
     *
     * @return logical path to file
     */
    public String getPath() {
        return path;
    }

    /**
     * Define logical path to file.
     *
     * @param path logical path to file
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get identifier of {@link io.keepup.cms.core.persistence.Content} record linked to this file.
     *
     * @return identifier of {@link io.keepup.cms.core.persistence.Content} record linked to this file
     */
    public Long getContentId() {
        return contentId;
    }

    /**
     * Define identifier of {@link io.keepup.cms.core.persistence.Content} record linked to this file.
     *
     * @param contentId identifier of {@link io.keepup.cms.core.persistence.Content} record linked to this file
     */
    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    /**
     * Get file content serialized as byte array.
     *
     * @return file content serialized as byte array
     */
    public byte[] getContent() {
        return Arrays.copyOf ( content, content.length);
    }

    /**
     * Define file content serialized as byte array.
     *
     * @param content file content serialized as byte array
     */
    public void setContent(byte[] content) {
        this.content = Arrays.copyOf ( content, content.length);
    }

    /**
     * Get file creation time.
     *
     * @return file creation time
     */
    public LocalDate getCreationTime() {
        return creationTime;
    }

    /**
     * Define file creation time.
     *
     * @param creationTime file creation time
     */
    public void setCreationTime(LocalDate creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Get file modification time.
     *
     * @return file modification time
     */
    public LocalDate getModificationTime() {
        return modificationTime;
    }

    /**
     * Define file modification time.
     *
     * @param modificationTime file modification time
     */
    public void setModificationTime(LocalDate modificationTime) {
        this.modificationTime = modificationTime;
    }

    /**
     * Default constructor
     */
    public FileEntity() {}

    /**
     * Constructor with parameters.
     *
     * @param fileName         name of file
     * @param path             logical path to file
     * @param contentId        content ID
     * @param creationTime     file creation time
     * @param modificationTime file modification time
     * @param content          file content serialized as byte array
     */
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

