package io.keepup.cms.core.persistence;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Fedor Sergeev
 * @since 0.6.5
 */
public interface BasicEntity<T> extends Serializable {
    Long getId();
    
    void setId(Long id);

    Long getParentId();
    
    void setParentId(Long id);
    
    T addAttribute(String key, Object value);

    void addAttributes(Map<String, T> objects);

    /**
     * @param attrKey   attribute key
     * @param attrValue attribute value
     */
    void setAttribute(String attrKey, T attrValue);
    
    T getAttribute(String attrKey);

    T removeAttribute(String key);

    /**
     * @return object attributes
     */
    Map<String, T> getAttributes();

    void setAttributes(Map<String, T> attributes);

    boolean hasAttribute(String attributeName);

    /**
     * @return attribute interface parameter
     */
    Class<T> getType();
}
