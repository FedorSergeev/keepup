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
    
    boolean isRoot();
}
