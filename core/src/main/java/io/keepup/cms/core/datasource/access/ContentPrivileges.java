package io.keepup.cms.core.datasource.access;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @author Fedor Sergeev f.sergeev@hldn.ru
 * @since 0.4
 */

public class ContentPrivileges implements Serializable {
    private static final String OWNER = "owner";
    private static final String ROLE = "role";
    private static final String OTHERS = "others";

    @JsonProperty(OWNER)
    private Privilege ownerPrivileges;
    @JsonProperty(ROLE)
    private Privilege rolePrivileges;
    @JsonProperty(OTHERS)
    private Privilege otherPrivileges;

    public Privilege getOwnerPrivileges() {
        return ownerPrivileges;
    }

    public void setOwnerPrivileges(Privilege ownerPrivileges) {
        this.ownerPrivileges = ownerPrivileges;
    }

    public Privilege getRolePrivileges() {
        return rolePrivileges;
    }

    public void setRolePrivileges(Privilege rolePrivileges) {
        this.rolePrivileges = rolePrivileges;
    }

    public Privilege getOtherPrivileges() {
        return otherPrivileges;
    }

    public void setOtherPrivileges(Privilege otherPrivileges) {
        this.otherPrivileges = otherPrivileges;
    }
    
    public ContentPrivileges() {
        super();
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
