package io.keepup.cms.core.datasource.access;

/**
 *
 * @author Fedor Sergeev f.sergeev@trans-it.pro
 */
public interface BasicPrivilege {
    boolean canCreateChildren();
    boolean canRead();
    boolean canWrite();
    boolean canExecute();
    void setCreateChildren(boolean create);
    void setRead(boolean read);
    void setWrite(boolean write);
    void setExecute(boolean execute);
}
