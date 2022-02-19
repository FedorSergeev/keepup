package io.keepup.cms.core.persistence;

import java.io.Serializable;
import java.util.Map;

/**
 * Basic entity interface. Used for operations with KeepUP content DAO.
 *
 * @author Fedor Sergeev
 * @since 0.6.5
 */
public interface BasicEntity<T> extends Serializable {
    /**
     * Get entity primary identifier
     *
     * @return entity ID
     */
    Long getId();
    /**
     * Set entity primary identifier
     *
     * @param id entity ID
     */
    void setId(Long id);
    /**
     * Get entity parent record primary identifier
     *
     * @return entity parent record ID
     */
    Long getParentId();
    /**
     * Set entity parent record primary identifier
     *
     * @param id entity parent record ID
     */
    void setParentId(Long id);

    /**
     * Add new attribute to current node.
     *
     * @param key   attribute name
     * @param value attribute value
     * @return      attribute serialized according to entity type, e.g. {@link Serializable}
     */
    T addAttribute(String key, Object value);

    /**
     * Add new attributes to current node. Existing attributes won/t be replaced.
     *
     * @param objects collection of object names and values to be added to entity attributes
     */
    void addAttributes(Map<String, T> objects);

    /**
     * @param attrKey   attribute key
     * @param attrValue attribute value
     */
    void setAttribute(String attrKey, T attrValue);

    /**
     * Get entity attribute by key
     * @param attrKey attribute name
     * @return        attribute value serialized as entity attribute type, e.g. {@link Serializable}. If nothing
     *                found by specified key, null can be returned.
     */
    T getAttribute(String attrKey);

    /**
     * Delete attribute by key.
     *
     * @param key key of the attribute to be removed from entity.
     * @return    removed value serialized as entity attribute type, e.g. {@link Serializable}. If nothing
     *            found by specified key, null can be returned.
     */
    T removeAttribute(String key);

    /**
     * @return object attributes
     */
    Map<String, T> getAttributes();

    /**
     * Add new attributes to current node. Existing attributes will be replaced.
     *
     * @param attributes collection of object names and values to be added to entity attributes
     */
    void setAttributes(Map<String, T> attributes);

    /**
     * Check whether node contains attribute specified by name.
     *
     * @param attributeName attribute name
     * @return              true if record contains such attribute
     */
    boolean hasAttribute(String attributeName);

    /**
     * @return attribute interface parameter
     */
    Class<T> getType();
}
