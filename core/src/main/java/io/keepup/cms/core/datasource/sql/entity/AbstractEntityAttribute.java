package io.keepup.cms.core.datasource.sql.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.io.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

/**
 * Not compatible with older KeepUP versions as some column names were changed due to
 * the uniform style ensuring
 */
@MappedSuperclass
public abstract class AbstractEntityAttribute implements Serializable {
    /**
     * Attribute key
     */
    @Column(name = "attribute_key", nullable = false)
    @Basic
    private String attributeKey;

    /**
     * Attribute value, by default it is a binary array witch can be serialized to other types at the
     * application side. Not the fastest way to serve content operations but it can be used as the universal approach
     * when storing some data. One can also build his own DAO repositories in the database if this practice is not
     * efficient for his cases.
     */
    @Column(name = "attribute_value")
    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] attributeValue;

    /**
     * Attribute Java type for serialization
     */
    @Column(name = "class", nullable = false)
    @Basic
    private String javaClass;

    /**
     * Defines date and time attribute was created
     */
    @Column(name = "creation_time", nullable = false)
    private LocalDate creationTime;

    /**
     * Defines the last time when object was modified
     */
    @Column(name = "modification_time", nullable = false)
    private LocalDate modificationTime;

    /**
     * Object mapper for entity serialization and deserializetion
     */
    protected static ObjectMapper mapper = new ObjectMapper();

    public String getAttributeKey() {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    public byte[] getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(byte[] attributeValue) {
        this.attributeValue = attributeValue != null
                ? Arrays.copyOf(attributeValue, attributeValue.length)
                : null;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
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

    public final byte[] toByteArray(Object o) {
        byte[] result;
        var bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            result = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            logError("Unable to convert attribute value to byte array: %s".formatted(ex.getMessage()));
            result = new byte[0];
        }
        return result;
    }

    protected void setDefaultValue() {
        attributeValue = new byte[0];
        javaClass = "java.lang.Byte[]";
    }

    /**
     * Return logger object
     *
     * @return logger implementation instance
     */
    protected Log getLog() {
        return null;
    }

    /**
     * Log error message if needed
     * @param message message to be logged at error level
     */
    private void logError(String message) {
        Optional.ofNullable(getLog()).ifPresent(log -> log.error(message));
    }
}
