package io.keepup.cms.core.datasource.access;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Represents object privileges for creating, reading, writing and executing
 * Content objects during the session.
 *
 * @author Fedor Sergeev
 * @since  0.4
 * @see    io.keepup.cms.core.persistence.Content
 */

public class Privilege implements BasicPrivilege, Serializable {

    private static final String CREATE_KEY = "create";
    private static final String EXECUTE_KEY = "execute";
    private static final String WRITE_KEY = "write";
    private static final String READ_KEY = "read";

    /**
     * Defines whether {@link io.keepup.cms.core.persistence.Content} children can be read
     */
    @JsonProperty(READ_KEY)
    private boolean read;
    /**
     * Defines whether {@link io.keepup.cms.core.persistence.Content} children can be updated
     */
    @JsonProperty(WRITE_KEY)
    private boolean write;
    /**
     * Defines whether {@link io.keepup.cms.core.persistence.Content} children can be executed
     */
    @JsonProperty(EXECUTE_KEY)
    private boolean execute;
    /**
     * Defines whether {@link io.keepup.cms.core.persistence.Content} children can be created
     */
    @JsonProperty(CREATE_KEY)
    private boolean createChildren;
    
    @Override
    public boolean canCreateChildren() {
        return this.createChildren;
    }
    @Override
    public boolean canRead() {
        return this.read;
    }

    @Override
    public boolean canWrite() {
        return this.write;
    }

    @Override
    public boolean canExecute() {
        return this.execute;
    }

    @Override
    public void setCreateChildren(boolean create) {
        this.createChildren = create;
    }

    @Override
    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public void setWrite(boolean write) {
        this.write = write;
    }

    @Override
    public void setExecute(boolean execute) {
        this.execute = execute;
    }
    
    @Override
    public String toString() {
        return "read = " + this.read + ", write = " + this.write + ", execute = " + this.execute + ", children = " + this.createChildren;
    }

    @Override
    public int hashCode() {
        var hash = 5;
        hash = 97 * hash + (this.read ? 1 : 0);
        hash = 97 * hash + (this.write ? 1 : 0);
        hash = 97 * hash + (this.execute ? 1 : 0);
        hash = 97 * hash + (this.createChildren ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Privilege other = (Privilege) obj;
        if (this.read != other.read) {
            return false;
        }
        if (this.write != other.write) {
            return false;
        }
        if (this.execute != other.execute) {
            return false;
        }
        return this.createChildren == other.createChildren;
    }
}
