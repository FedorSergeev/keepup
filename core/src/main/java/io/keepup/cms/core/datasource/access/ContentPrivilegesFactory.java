package io.keepup.cms.core.datasource.access;

/**
 * Utility factory class
 *
 * @author Fedor Sergeev, f.sergeev@trans-it.pro
 */

public class ContentPrivilegesFactory {
    public static final ContentPrivileges STANDARD_PRIVILEGES;
    
    static {
        STANDARD_PRIVILEGES = new ContentPrivileges();
        
        STANDARD_PRIVILEGES.setOwnerPrivileges(new Privilege());
        STANDARD_PRIVILEGES.getOwnerPrivileges().setCreateChildren(true);
        STANDARD_PRIVILEGES.getOwnerPrivileges().setRead(true);
        STANDARD_PRIVILEGES.getOwnerPrivileges().setWrite(true);
        STANDARD_PRIVILEGES.getOwnerPrivileges().setExecute(true);
        
        STANDARD_PRIVILEGES.setRolePrivileges(new Privilege());
        STANDARD_PRIVILEGES.getRolePrivileges().setCreateChildren(true);
        STANDARD_PRIVILEGES.getRolePrivileges().setRead(true);
        STANDARD_PRIVILEGES.getRolePrivileges().setWrite(true);
        STANDARD_PRIVILEGES.getRolePrivileges().setExecute(true);
        
        STANDARD_PRIVILEGES.setOtherPrivileges(new Privilege());
        STANDARD_PRIVILEGES.getOtherPrivileges().setCreateChildren(true);
        STANDARD_PRIVILEGES.getOtherPrivileges().setRead(false);
        STANDARD_PRIVILEGES.getOtherPrivileges().setWrite(false);
        STANDARD_PRIVILEGES.getOtherPrivileges().setExecute(false);
    }

    /**
     * Alternative constructor.
     */
    private ContentPrivilegesFactory() {
        throw new IllegalStateException("Utility class");
    }
}
