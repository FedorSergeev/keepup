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

    /**
     * Default constructor.
     */
    public FileWrapper() {
        exists = false;
    }

    /**
     * Get the primary identifier.
     *
     * @return primary identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Define the primary identifier.
     *
     * @param id primary identifier
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the name of file.
     *
     * @return name of file
     */
    public String getName() {
        return name;
    }

    /**
     * Define the name of file.
     *
     * @param name name of file
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the logical path to file.
     *
     * @return logical path to file
     */
    public String getPath() {
        return path;
    }

    /**
     * Define the logical path to file.
     *
     * @param path logical path to file
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get file creation date.
     *
     * @return file creation date
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * Define file creation date.
     *
     * @param creationDate file creation date
     */
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Get the date when file was modified last time.
     *
     * @return date when file was modified last time
     */
    public LocalDate getLastModified() {
        return lastModified;
    }

    /**
     * Define the date when file was modified last time.
     *
     * @param lastModified date when file was modified last time
     */
    public void setLastModified(LocalDate lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Get file content represented as an output stream of bytes.
     *
     * @return file content represented as an output stream of bytes
     */
    public OutputStream getContent() {
        return content;
    }

    /**
     * Define file content represented as an output stream of bytes.
     *
     * @param content file content represented as an output stream of bytes
     */
    public void setContent(OutputStream content) {
        this.content = content;
    }

    /**
     * Check whether file exists.
     *
     * @return true if file exists
     */
    public boolean isExists() {
        return exists;
    }

    /**
     * Define whether file exists.
     *
     * @param exists true if file exists
     */
    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
