package io.keepup.cms.core.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @param <T> node attribute type
 * @author Fedor Sergeev
 * @since 0.6.4
 */
public abstract class AbstractNode<T> implements BasicEntity<T> {
    /**
     * Node identifier
     */
    @JsonProperty("id")
    protected Long id;
    /**
     * Node parent id
     */
    @JsonProperty("parentId")
    protected Long parentId;
    /**
     * Node attributes
     */
    @JsonProperty("attributes")
    protected transient Map<String, T> attributes;

    /**
     * Adds a new attribute to Node.
     * <p>
     * You can specify an attribute name without
     * its type (eg description.html), it will be found even in case the parameter
     * type was specified in the data.
     *
     * @param attrKey name of the attribute.
     * @return attribute value.
     */
    @Override
    public T getAttribute(String attrKey) {
        Map<String, T> attributeNames = new HashMap<>();

        for (Map.Entry<String, T> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String resultKey;
            if (key.contains(".")) {
                resultKey = key.substring(0, key.indexOf('.'));
            } else {
                resultKey = key;
            }
            attributeNames.put(resultKey, attributes.get(key));
        }
        if (attributeNames.containsKey(attrKey)) {
            return attributeNames.get(attrKey);
        } else if (attributes.containsKey(attrKey)) {
            return attributes.get(attrKey);
        }
        return null;
    }

    @Override
    public void setAttribute(String attrKey, T attrValue) {
        if (attributes == null)
            attributes = new HashMap<>();
        attributes.put(attrKey, attrValue);
    }

    /**
     * Adds new set of attributes to the existing ones
     *
     * @param objects attributes {@link Map}
     */
    @Override
    public void addAttributes(Map<String, T> objects) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.putAll(objects);

    }

    /**
     * @return content record attributes
     */
    @Override
    public Map<String, T> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes new attributes
     */
    @Override
    public void setAttributes(Map<String, T> attributes) {
        Optional.ofNullable(attributes).ifPresent(attrs -> this.attributes = attrs);
    }

    /**
     * Adds a new pair of key and value to list of product attributes.
     *
     * @param key   attribute key
     * @param value attribute value
     * @return added attribute
     */
    @SuppressWarnings("unchecked")
    @Override
    public T addAttribute(String key, Object value) {

        T valueToSave = value != null && getType().isAssignableFrom(value.getClass())
                ? (T)value
                : convertToPersistentValue(value);
        return attributes.put(key, valueToSave);
    }

    /**
     * Removes attribute with the specified key.
     *
     * @param key attribute name
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    @Override
    public T removeAttribute(String key) {
        return attributes.remove(key);
    }

    @Override
    public Long getId() {
        return id;
    }


    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(Long id) {
        this.parentId = id;
    }

    /**
     * Checks whether current entity has attribute specified by key.
     * <p>
     * Method was designed to get rid of NULL checking.
     * </p>
     *
     * @param attrName attribute key.
     * @return true in case element contains the specified attribute.
     * @since 0.3
     */
    @Override
    public boolean hasAttribute(String attrName) {
        if (attrName == null) {
            return attributes.containsKey(null);
        }
        if (attributes.get(attrName) == null && attrName.contains(".")) {
            return hasAttribute(attrName.split("\\.")[0]);
        }
        return attributes.containsKey(attrName);
    }

    /**
     * Implement this method in case you need to convert attribute type to convenient persistent interface
     *
     * @param value object to be saved as an attribute
     * @return saved object
     */
    protected abstract T convertToPersistentValue(Object value);
}
