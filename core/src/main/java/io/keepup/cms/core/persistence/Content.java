package io.keepup.cms.core.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.keepup.cms.core.datasource.access.ContentPrivileges;

import java.io.Serializable;

/**
 * Basic object representation for all content records.
 *
 * @author Fedor Sergeev
 */
@JsonDeserialize(as = Node.class)
@JsonSerialize(as = Node.class)
public interface Content extends BasicEntity<Serializable> {
    Long getOwnerId();

    /**
     * @param ownerId object owners identifier
     */
    void setOwnerId(Long ownerId);

    ContentPrivileges getContentPrivileges();

    void setContentPrivileges(ContentPrivileges contentPrivileges);

    /**
     * Set default content access {@link ContentPrivileges}
     */
    void setDefaultPrivileges();

    /**
     * Object type, e.g. name of class of entity persisted by {@link Content} storage
     * @return name of object type
     */
    String getEntityType();

    /**
     * Set object type
     * @param entityType entity that was converted to {@link Content} record. Can be null if there is
     *                   no need to wrap {@link Content} objects to some other types.
     */
    void setEntityType(String entityType);
    
    boolean isRoot();
}
