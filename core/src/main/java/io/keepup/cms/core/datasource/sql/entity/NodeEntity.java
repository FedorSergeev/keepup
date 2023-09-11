package io.keepup.cms.core.datasource.sql.entity;

import io.keepup.cms.core.persistence.Content;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;


/**
 * Base {@link io.keepup.cms.core.persistence.Content} entity abstraction on SQL data source layer
 *
 * @author Fedor Sergeev
 */
@Entity
@org.springframework.data.relational.core.mapping.Table
@Table(name="node_entity", indexes = {
        @Index(name = "IDX_ID", columnList = "id"),
        @Index(name = "IDX_PARENT_ID", columnList = "parent_id"),
        @Index(name = "IDX_OWNER_ID", columnList = "owner_id")})
public class NodeEntity implements Serializable  {
    @Serial
    private static final long serialVersionUID = 24523L;
    /**
     * Primary identifier
     */
    @Id
    Long id;
    /**
     * Primary identifier of parent node
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId;
    /**
     * {@link NodeEntity} owner's identifier
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    /**
     * Owner entity read access flag
     */
    @Column(name = "owner_read_privilege", nullable = false)
    private boolean ownerReadPrivilege;
    /**
     * Owner entity update access flag
     */
    @Column(name = "owner_write_privilege", nullable = false)
    private boolean ownerWritePrivilege;
    /**
     * Owner entity execute access flag
     */
    @Column(name = "owner_execute_privilege", nullable = false)
    private boolean ownerExecutePrivilege;
    /**
     * Owner possibility to create entities with current node ID as parent identifier access flag
     */
    @Column(name = "owner_create_children_privilege", nullable = false)
    private boolean ownerCreateChildrenPrivilege;
    /**
     * Specifies whether user with the same as owner role can read current entity
     */
    @Column(name = "role_read_privilege", nullable = false)
    private boolean roleReadPrivilege;
    /**
     * Specifies whether user with the same as owner role can update current entity
     */
    @Column(name = "role_write_privilege", nullable = false)
    private boolean roleWritePrivilege;
    /**
     * Specifies whether user with the same as owner role can execute current entity
     */
    @Column(name = "role_execute_privilege", nullable = false)
    private boolean roleExecutePrivilege;
    /**
     * Specifies whether user with the same as owner role can create child nodes with current node ID as parent identifier
     */
    @Column(name = "role_create_children_privilege", nullable = false)
    private boolean roleCreateChildrenPrivilege;
    /**
     * Specifies whether anyone can read current entity
     */
    @Column(name = "other_read_privilege", nullable = false)
    private boolean otherReadPrivilege;
    /**
     * Specifies whether anyone can update current entity
     */
    @Column(name = "other_write_privilege", nullable = false)
    private boolean otherWritePrivilege;
    /**
     * Specifies whether anyone can execute current entity
     */
    @Column(name = "other_execute_privilege", nullable = false)
    private boolean otherExecutePrivilege;
    /**
     * Specifies whether anyone can create children of current entity
     */
    @Column(name = "other_create_children_privilege", nullable = false)
    private boolean otherCreateChildrenPrivilege;
    /**
     * Entity Java class name
     */
    @Column(name = "entity_type")
    private String entityType;

    /**
     * Default constructor.
     */
    public NodeEntity() {
        super();
    }

    /**
     * Create Node from {@link Content} object.
     *
     * @param content {@link Content} implementation
     */
    public NodeEntity(final Content content) {
        this.id = content.getId() == null || content.getId() == 0
                ? null
                : content.getId();

        ownerId = content.getOwnerId();
        parentId = content.getParentId();
        ownerReadPrivilege = content.getContentPrivileges().getOwnerPrivileges().canRead();
        ownerWritePrivilege = content.getContentPrivileges().getOwnerPrivileges().canWrite();
        ownerExecutePrivilege = content.getContentPrivileges().getOwnerPrivileges().canExecute();
        ownerCreateChildrenPrivilege = content.getContentPrivileges().getOwnerPrivileges().canCreateChildren();
        roleReadPrivilege = content.getContentPrivileges().getRolePrivileges().canRead();
        roleWritePrivilege = content.getContentPrivileges().getRolePrivileges().canWrite();
        roleExecutePrivilege = content.getContentPrivileges().getRolePrivileges().canExecute();
        roleCreateChildrenPrivilege = content.getContentPrivileges().getRolePrivileges().canCreateChildren();
        otherReadPrivilege = content.getContentPrivileges().getOtherPrivileges().canRead();
        otherWritePrivilege = content.getContentPrivileges().getOtherPrivileges().canWrite();
        otherExecutePrivilege = content.getContentPrivileges().getOtherPrivileges().canExecute();
        otherCreateChildrenPrivilege = content.getContentPrivileges().getOtherPrivileges().canCreateChildren();
        setEntityType(content.getEntityType());
    }

    /**
     * Get primary identifier.
     *
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set primary identifier.
     *
     * @param id ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get parent record primary identifier, if the record is root itself, 0 will be returned.
     *
     * @return parent record ID
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * Ыet parent record primary identifier, if the record is root itself, 0 will be returned.
     *
     * @param parentId parent node identifier
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * Get node owner identifier.
     *
     * @return node owner ID
     */
    public Long getOwnerId() {
        return ownerId;
    }

    /**
     * Set node owner identifier.
     *
     * @param ownerId owner ID
     */
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Shows if node owner can read it.
     *
     * @return true if node can be read by it's owner
     */
    public boolean isOwnerReadPrivilege() {
        return ownerReadPrivilege;
    }

    /**
     * Allow the owner user to read node.
     *
     * @param ownerReadPrivilege true to allow the user to read node
     */
    public void setOwnerReadPrivilege(boolean ownerReadPrivilege) {
        this.ownerReadPrivilege = ownerReadPrivilege;
    }

    /**
     * Shows if node owner can write it.
     *
     * @return true if node can be updated by it's owner
     */
    public boolean isOwnerWritePrivilege() {
        return ownerWritePrivilege;
    }

    /**
     * Allow the owner user to write node.
     *
     * @param ownerWritePrivilege true to allow the user to read node
     */
    public void setOwnerWritePrivilege(boolean ownerWritePrivilege) {
        this.ownerWritePrivilege = ownerWritePrivilege;
    }

    /**
     * Checks if node owner can execute it.
     *
     * @return true if node can be executed by it's owner
     */
    public boolean isOwnerExecutePrivilege() {
        return ownerExecutePrivilege;
    }

    /**
     * Defines if node owner can execute it.
     *
     * @param ownerExecutePrivilege true if node can be executed by it's owner
     */
    public void setOwnerExecutePrivilege(boolean ownerExecutePrivilege) {
        this.ownerExecutePrivilege = ownerExecutePrivilege;
    }

    /**
     * Checks if node owner can create nodes with current node ID as parent identifier
     *
     * @return true if node owner can create nodes with current node ID as parent identifier
     */
    public boolean isOwnerCreateChildrenPrivilege() {
        return ownerCreateChildrenPrivilege;
    }

    /**
     * Shows if node owner can create children with current node ID as parent identifier.
     *
     * @param ownerCreateChildrenPrivilege true if owner user can create nodes with current node ID as parent identifier
     */
    public void setOwnerCreateChildrenPrivilege(boolean ownerCreateChildrenPrivilege) {
        this.ownerCreateChildrenPrivilege = ownerCreateChildrenPrivilege;
    }

    /**
     * Checks if user with the same as record owner role can read it.
     *
     * @return true if user with the same as record owner role can read it
     */
    public boolean isRoleReadPrivilege() {
        return roleReadPrivilege;
    }

    /**
     * Defines ability for user with the same as record owner role can read it.
     *
     * @param roleReadPrivilege true if user with the same as record owner role can read it
     */
    public void setRoleReadPrivilege(boolean roleReadPrivilege) {
        this.roleReadPrivilege = roleReadPrivilege;
    }

    /**
     * Checks if user with the same as record owner role can update it.
     *
     * @return true if user with the same as record owner role can update it
     */
    public boolean isRoleWritePrivilege() {
        return roleWritePrivilege;
    }

    /**
     * Defines ability for user with the same as record owner role can update it.
     *
     * @param roleWritePrivilege true if user with the same as record owner role can update it
     */
    public void setRoleWritePrivilege(boolean roleWritePrivilege) {
        this.roleWritePrivilege = roleWritePrivilege;
    }

    /**
     * Checks if user with the same as record owner role can execute it's attributes.
     *
     * @return true if user with the same as record owner role can execute it's attributes
     */
    public boolean isRoleExecutePrivilege() {
        return roleExecutePrivilege;
    }

    /**
     * Defines ability for user with the same as record owner role can execute it's attributes.
     *
     * @param roleExecutePrivilege  true if user with the same as record owner role can execute it's attributes
     */
    public void setRoleExecutePrivilege(boolean roleExecutePrivilege) {
        this.roleExecutePrivilege = roleExecutePrivilege;
    }

    /**
     * Checks if user with the same as record owner role can create records with current node ID as parent identifier.
     *
     * @return true if user with the same as record owner role can create records with current node ID as parent identifier.
     */
    public boolean isRoleCreateChildrenPrivilege() {
        return roleCreateChildrenPrivilege;
    }

    /**
     * Defines ability for user with the same as record owner role can create records with current node ID as parent identifier.
     *
     * @param roleCreateChildrenPrivilege true if user with the same as record owner role can create records with current node ID as parent identifier.
     */
    public void setRoleCreateChildrenPrivilege(boolean roleCreateChildrenPrivilege) {
        this.roleCreateChildrenPrivilege = roleCreateChildrenPrivilege;
    }

    /**
     * Checks if anyone can read record.
     *
     * @return true if anyone can read record
     */
    public boolean isOtherReadPrivilege() {
        return otherReadPrivilege;
    }

    /**
     * Define ability for anyone to read the record.
     *
     * @param otherReadPrivilege true if anyone can read current record
     */
    public void setOtherReadPrivilege(boolean otherReadPrivilege) {
        this.otherReadPrivilege = otherReadPrivilege;
    }

    /**
     * Checks ability for other users to update current record (e.g. not owner and not group member can change
     * node attributes).
     *
     * @return true if anyone can update the record
     */
    public boolean isOtherWritePrivilege() {
        return otherWritePrivilege;
    }

    /**
     * Define ability for other users to update current record (e.g. not owner and not group member can change
     * node attributes).
     *
     * @param otherWritePrivilege true if anyone can update the record
     */
    public void setOtherWritePrivilege(boolean otherWritePrivilege) {
        this.otherWritePrivilege = otherWritePrivilege;
    }

    /**
     * Checks ability for other users to execute attributes of current node.
     *
     * @return true if anyone can execute node attributes
     */
    public boolean isOtherExecutePrivilege() {
        return otherExecutePrivilege;
    }

    /**
     * Define ability for other users to execute attributes of current node.
     *
     * @param otherExecutePrivilege true if anyone can execute node attributes
     */
    public void setOtherExecutePrivilege(boolean otherExecutePrivilege) {
        this.otherExecutePrivilege = otherExecutePrivilege;
    }

    /**
     * Checks ability for other users to create current node children (e.g. not owner and not group member can create
     * node with current node ID as parent identifier).
     *
     * @return true if anyone can create nodes with current record ID as parent identifier
     */
    public boolean isOtherCreateChildrenPrivilege() {
        return otherCreateChildrenPrivilege;
    }

    /**
     * Define ability for other users to create current node children (e.g. not owner and not group member can create
     * node with current node ID as parent identifier).
     *
     * @param otherCreateChildrenPrivilege true if anyone can create nodes with current record ID as parent identifier
     */
    public void setOtherCreateChildrenPrivilege(boolean otherCreateChildrenPrivilege) {
        this.otherCreateChildrenPrivilege = otherCreateChildrenPrivilege;
    }

    /**
     * Get node entity type.
     *
     * @return node entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Define entity type for the node.
     *
     * @param entityType Java type of the node
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
