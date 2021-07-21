package io.keepup.cms.core.datasource.sql.entity;

import io.keepup.cms.core.persistence.Content;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Base {@link io.keepup.cms.core.persistence.Content} entity abstraction on SQL data source layer
 *
 * @author Fedor Sergeev
 */
@Entity
@Table(name="node", indexes = {
        @Index(name = "IDX_ID", columnList = "id"),
        @Index(name = "IDX_PARENT_ID", columnList = "parent_id"),
        @Index(name = "IDX_OWNER_ID", columnList = "owner_id")})
public class NodeEntity implements Serializable  {
    @Serial
    private static final long serialVersionUID = 24523L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq_generator")
    @SequenceGenerator(name = "content_seq_generator", sequenceName = "content_seq", allocationSize = 1)
    Long id;
    @Column(name = "parent_id", nullable = false)
    private Long parentId;
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(name = "owner_read_privilege", nullable = false)
    private boolean ownerReadPrivilege;
    @Column(name = "owner_write_privilege", nullable = false)
    private boolean ownerWritePrivilege;
    @Column(name = "owner_execute_privilege", nullable = false)
    private boolean ownerExecutePrivilege;
    @Column(name = "owner_create_children_privilege", nullable = false)
    private boolean ownerCreateChildrenPrivilege;
    @Column(name = "role_read_privilege", nullable = false)
    private boolean roleReadPrivilege;
    @Column(name = "role_write_privilege", nullable = false)
    private boolean roleWritePrivilege;
    @Column(name = "role_execute_privilege", nullable = false)
    private boolean roleExecutePrivilege;
    @Column(name = "role_create_children_privilege", nullable = false)
    private boolean roleCreateChildrenPrivilege;
    @Column(name = "other_read_privilege", nullable = false)
    private boolean otherReadPrivilege;
    @Column(name = "other_write_privilege", nullable = false)
    private boolean otherWritePrivilege;
    @Column(name = "other_execute_privilege", nullable = false)
    private boolean otherExecutePrivilege;
    @Column(name = "other_create_children_privilege", nullable = false)
    private boolean otherCreateChildrenPrivilege;

    @JoinColumn(name = "contentId")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<NodeAttributeEntity> attributes;

    public NodeEntity() {
        attributes = new ArrayList<>();
    }

    public NodeEntity(Content content, boolean noAttributes) {
        this(content);
        if (!noAttributes) {
            content.getAttributes().entrySet().forEach(this::accept);
        }
    }

    public NodeEntity(Content content) {
        if (content.getId() == null || content.getId() == 0) {
            this.id = -1L;
        } else {
            this.id = content.getId();
        }
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
        attributes = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwnerReadPrivilege() {
        return ownerReadPrivilege;
    }

    public void setOwnerReadPrivilege(boolean ownerReadPrivilege) {
        this.ownerReadPrivilege = ownerReadPrivilege;
    }

    public boolean isOwnerWritePrivilege() {
        return ownerWritePrivilege;
    }

    public void setOwnerWritePrivilege(boolean ownerWritePrivilege) {
        this.ownerWritePrivilege = ownerWritePrivilege;
    }

    public boolean isOwnerExecutePrivilege() {
        return ownerExecutePrivilege;
    }

    public void setOwnerExecutePrivilege(boolean ownerExecutePrivilege) {
        this.ownerExecutePrivilege = ownerExecutePrivilege;
    }

    public boolean isOwnerCreateChildrenPrivilege() {
        return ownerCreateChildrenPrivilege;
    }

    public void setOwnerCreateChildrenPrivilege(boolean ownerCreateChildrenPrivilege) {
        this.ownerCreateChildrenPrivilege = ownerCreateChildrenPrivilege;
    }

    public boolean isRoleReadPrivilege() {
        return roleReadPrivilege;
    }

    public void setRoleReadPrivilege(boolean roleReadPrivilege) {
        this.roleReadPrivilege = roleReadPrivilege;
    }

    public boolean isRoleWritePrivilege() {
        return roleWritePrivilege;
    }

    public void setRoleWritePrivilege(boolean roleWritePrivilege) {
        this.roleWritePrivilege = roleWritePrivilege;
    }

    public boolean isRoleExecutePrivilege() {
        return roleExecutePrivilege;
    }

    public void setRoleExecutePrivilege(boolean roleExecutePrivilege) {
        this.roleExecutePrivilege = roleExecutePrivilege;
    }

    public boolean isRoleCreateChildrenPrivilege() {
        return roleCreateChildrenPrivilege;
    }

    public void setRoleCreateChildrenPrivilege(boolean roleCreateChildrenPrivilege) {
        this.roleCreateChildrenPrivilege = roleCreateChildrenPrivilege;
    }

    public boolean isOtherReadPrivilege() {
        return otherReadPrivilege;
    }

    public void setOtherReadPrivilege(boolean otherReadPrivilege) {
        this.otherReadPrivilege = otherReadPrivilege;
    }

    public boolean isOtherWritePrivilege() {
        return otherWritePrivilege;
    }

    public void setOtherWritePrivilege(boolean otherWritePrivilege) {
        this.otherWritePrivilege = otherWritePrivilege;
    }

    public boolean isOtherExecutePrivilege() {
        return otherExecutePrivilege;
    }

    public void setOtherExecutePrivilege(boolean otherExecutePrivilege) {
        this.otherExecutePrivilege = otherExecutePrivilege;
    }

    public boolean isOtherCreateChildrenPrivilege() {
        return otherCreateChildrenPrivilege;
    }

    public void setOtherCreateChildrenPrivilege(boolean otherCreateChildrenPrivilege) {
        this.otherCreateChildrenPrivilege = otherCreateChildrenPrivilege;
    }

    public List<NodeAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<NodeAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    private void accept(Map.Entry<String, Serializable> entry) {
        attributes.add(new NodeAttributeEntity(id, entry.getKey(), entry.getValue()));
    }
}
