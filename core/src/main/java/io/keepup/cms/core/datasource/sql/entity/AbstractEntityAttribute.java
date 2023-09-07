package io.keepup.cms.core.datasource.sql.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;

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
    @Serial
    private static final long serialVersionUID = 1L;
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
    private byte[] attributeValue;

    /**
     * Attribute Java type for serialization
     */
    @Column(name = "java_class", nullable = false)
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

    /**
     * Get attribute name
     *
     * @return attribute name
     */
    public String getAttributeKey() {
        return attributeKey;
    }

    /**
     * Set attribute name
     *
     * @param attributeKey attribute key
     */
    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    /**
     * Get serialized to byte array value of the attribute
     *
     * @return serialized to byte array attribute value
     */
    public byte[] getAttributeValue() {
        return attributeValue;
    }

    /**
     * Set serialized to byte array value of the attribute
     *
     * @param attributeValue serialized to byte array attribute value
     */
    public void setAttributeValue(byte[] attributeValue) {
        this.attributeValue = attributeValue != null
                ? Arrays.copyOf(attributeValue, attributeValue.length)
                : null;
    }

    /**
     * Get the information about Java class of the object stored as byte array in attribute value.
     *
     * @return the full name of Java class including package name
     */
    public String getJavaClass() {
        return javaClass;
    }

    /**
     * Specify the name of Java class the attribute value should be deserialized to.
     *
     * @param javaClass Java type with package
     */
    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * Get time when the attribute was created.
     *
     * @return attribute creation time
     */
    public LocalDate getCreationTime() {
        return creationTime;
    }

    /**
     * Specify time when the attribute was created. Usually not needed for mappers.
     *
     * @param creationTime time of object creation
     */
    public void setCreationTime(LocalDate creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Get time when the attribute was last time nodified.
     *
     * @return attribute last modification time
     */
    public LocalDate getModificationTime() {
        return modificationTime;
    }

    /**
     * Specify time when the attribute was last time modified. Usually not needed for mappers.
     *
     * @param modificationTime time of object last modification
     */
    public void setModificationTime(LocalDate modificationTime) {
        this.modificationTime = modificationTime;
    }

    /**
     * Converts object to byte array for persisting attribute values in the same storage. In case of
     * IOException during serialization process no Throwable will be thrown and the an empty byte array will be
     * returned as operation result. Maybe catching exception and wrapping it as a specific system exception
     * is a better approach then checking the length of method result, but yet the process is design for user to
     * receive response for his request anyway.
     *
     * @param object object to be serialized to byte array
     * @return       object serialized to array of bytes
     */
    public final byte[] toByteArray(Object object) {
        byte[] result;
        var bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            result = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            logError("Unable to convert attribute value to byte array: %s".formatted(ex.getMessage()));
            result = new byte[0];
        }
        return result;
    }

    /**
     * Specifies the default value and Java type for node attribute value.
     */
    protected void setDefaultValue() {
        attributeValue = new byte[0];
        javaClass = "java.lang.Byte[]";
    }

    /**
     * Return logger object
     *
     * @return logger implementation instance
     */
    protected abstract Log getLog();

    /**
     * Log error message if needed
     * @param message message to be logged at error level
     */
    private void logError(String message) {
        Optional.ofNullable(getLog()).ifPresent(log -> log.error(message));
    }
}
