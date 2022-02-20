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
    /**
     * Get identifier of record owner {@link User}.
     *
     * @return owner identifier
     */
    Long getOwnerId();

    /**
     * Set identifier of record owner {@link User}.
     *
     * @param ownerId object owners identifier
     */
    void setOwnerId(Long ownerId);

    /**
     * Get access privileges for current record.
     *
     * @return record read, write, create children and execution privileges according to the group, role and identifier
     *         of user who requested them
     */
    ContentPrivileges getContentPrivileges();

    /**
     * Set access privileges for current record.
     *
     * @param contentPrivileges read, write, create children and execution privileges according to the group, role and
     *                          user identifier
     */
    void setContentPrivileges(ContentPrivileges contentPrivileges);

    /**
     * Set default content access {@link ContentPrivileges}
     */
    void setDefaultPrivileges();

    /**
     * Object type, e.g. name of class of entity persisted by {@link Content} storage.
     *
     * @return name of object type
     */
    String getEntityType();

    /**
     * Set object type.
     *
     * @param entityType entity that was converted to {@link Content} record. Can be null if there is
     *                   no need to wrap {@link Content} objects to some other types.
     */
    void setEntityType(String entityType);

    /**
     * Shows of record parent id is zero and if has no real parent records.
     *
     * @return true if record has no real parent records.
     */
    boolean isRoot();
}
