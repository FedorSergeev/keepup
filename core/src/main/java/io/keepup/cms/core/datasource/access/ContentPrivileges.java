package io.keepup.cms.core.datasource.access;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * {@link io.keepup.cms.core.persistence.Content} object privileges aggregator.
 *
 * @author Fedor Sergee
 * @since 0.4
 */

public class ContentPrivileges implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String OWNER = "owner";
    private static final String ROLE = "role";
    private static final String OTHERS = "others";

    /**
     * Owner privileges
     */
    @JsonProperty(OWNER)
    private Privilege ownerPrivileges;
    /**
     * Role privileges
     */
    @JsonProperty(ROLE)
    private Privilege rolePrivileges;
    /**
     * Privileges for anyone
     */
    @JsonProperty(OTHERS)
    private Privilege otherPrivileges;

    /**
     * Default constructor.
     */
    public ContentPrivileges() {
        super();
    }

    /**
     * Get {@link io.keepup.cms.core.persistence.Content} owner privileges
     *
     * @return owner's privileges for the object
     */
    public Privilege getOwnerPrivileges() {
        return ownerPrivileges;
    }

    /**
     * Define {@link io.keepup.cms.core.persistence.Content} owner privileges
     *
     * @param ownerPrivileges owner's privileges for the object
     */
    public void setOwnerPrivileges(Privilege ownerPrivileges) {
        this.ownerPrivileges = ownerPrivileges;
    }

    /**
     * Get privilege on an object for a user that has the same role as the owner of the object.
     *
     * @return privilege on an object for a user that has the same role as the owner of the object
     */
    public Privilege getRolePrivileges() {
        return rolePrivileges;
    }

    /**
     * Defines privilege on an object for a user that has the same role as the owner of the object.
     *
     * @param rolePrivileges privilege on an object for a user that has the same role as the owner of the object
     */
    public void setRolePrivileges(Privilege rolePrivileges) {
        this.rolePrivileges = rolePrivileges;
    }

    /**
     * Get {@link io.keepup.cms.core.persistence.Content} privileges on an object for anyone.
     *
     * @return privileges on an object for anyone.
     */
    public Privilege getOtherPrivileges() {
        return otherPrivileges;
    }

    /**
     * Define {@link io.keepup.cms.core.persistence.Content} privileges on an object for anyone.
     *
     * @param otherPrivileges privileges on an object for anyone.
     */
    public void setOtherPrivileges(Privilege otherPrivileges) {
        this.otherPrivileges = otherPrivileges;
    }

    @Override
    public String toString() {
        return "[ owner: " + ofNullable(ownerPrivileges).map(Privilege::toString)
                + ", role: " + ofNullable(rolePrivileges).map(Privilege::toString)
                + ", other: " + ofNullable(otherPrivileges).map(Privilege::toString)
                + "]";
    }

    @Override
    public int hashCode() {
        var hash = 5;
        hash = 89 * hash + Objects.hashCode(this.ownerPrivileges);
        hash = 89 * hash + Objects.hashCode(this.rolePrivileges);
        hash = 89 * hash + Objects.hashCode(this.otherPrivileges);
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
        final ContentPrivileges other = (ContentPrivileges) obj;
        if (!Objects.equals(this.ownerPrivileges, other.ownerPrivileges)) {
            return false;
        }
        if (!Objects.equals(this.rolePrivileges, other.rolePrivileges)) {
            return false;
        }
        return Objects.equals(this.otherPrivileges, other.otherPrivileges);
    }
}
