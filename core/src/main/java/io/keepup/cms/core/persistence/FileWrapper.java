package io.keepup.cms.core.persistence;

import java.io.OutputStream;
import java.time.LocalDate;

/**
 * Aggregates file attributes without creating the file
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class FileWrapper {
    private long id;
    private String name;
    private String path;
    private LocalDate creationDate;
    private LocalDate lastModified;
    private OutputStream content;
    private boolean exists;

    public FileWrapper() {
        exists = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDate lastModified) {
        this.lastModified = lastModified;
    }

    public OutputStream getContent() {
        return content;
    }

    public void setContent(OutputStream content) {
        this.content = content;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
