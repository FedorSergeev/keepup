package io.keepup.cms.core.datasource.access;

/**
 * Abstraction for working with user access rights to a record.
 *
 * @author Fedor Sergeev f.sergeev@trans-it.pro
 * @since 1.0
 */
public interface BasicPrivilege {
    /**
     * Flag showing if creating records with current node identifier as parent identifier is available.
     *
     * @return true if creating records with current node identifier as parent identifier is available
     */
    boolean canCreateChildren();

    /**
     * Flag showing if record read operation is available.
     *
     * @return true if record read operation is available
     */
    boolean canRead();
    /**
     * Flag showing if record write operation is available.
     *
     * @return true if record write operation is available
     */
    boolean canWrite();
    /**
     * Flag showing if record execution is available.
     *
     * @return true if record execution is available
     */
    boolean canExecute();
    /**
     * Set possibility to create current node children.
     *
     * @param create true if record children creation is available
     */
    void setCreateChildren(boolean create);
    /**
     * Set possibility to read current node.
     *
     * @param read possibility to read current node
     */
    void setRead(boolean read);
    /**
     * Set possibility to update current node.
     *
     * @param write possibility to update current node
     */
    void setWrite(boolean write);
    /**
     * Set possibility to execute current node.
     *
     * @param execute possibility to execute current node
     */
    void setExecute(boolean execute);
}
