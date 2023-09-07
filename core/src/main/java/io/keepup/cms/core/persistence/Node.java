package io.keepup.cms.core.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keepup.cms.core.datasource.access.ContentPrivileges;
import io.keepup.cms.core.datasource.access.Privilege;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
/**
 * New representation of Content entity.
 *
 * @author Fedor Sergeev, f.sergeev@trans-it.pro
 * @since 1.2
 */
public class Node extends AbstractNode<Serializable> implements Content, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String OWNER_ID = "ownerId";
    private static final String PRIVILEGES_KEY = "privileges";
    private static final String ENTITY_TYPE = "entityType";
    /**
     * ID of entity owner
     */
    @JsonProperty(OWNER_ID)
    protected Long ownerId;
    /**
     * Entity privileges
     */
    @JsonProperty(PRIVILEGES_KEY)
    protected ContentPrivileges privileges;
    /**
     * Type of entity
     */
    @JsonProperty(ENTITY_TYPE)
    protected String entityType;

    /**
     * Constructor.
     *
     * @param id {@link Content} identifier
     */
    public Node(Long id) {
        this();
        this.id = id;
    }

    /**
     * Default constructor.
     */
    public Node() {
        super();
        attributes = new HashMap<>();
        privileges = new ContentPrivileges();
        privileges.setOwnerPrivileges(new Privilege());
        privileges.setRolePrivileges(new Privilege());
        privileges.setOtherPrivileges(new Privilege());
    }


    @Override
    public String toString() {
        return "id = "
                + getAttribute("id")
                + ", parentId = " + parentId
                + ", ownerId = " + ownerId
                + ", privileges = " + privileges.toString()
                + ",  attributes = "
                + getAttributes().toString();
    }

    @Override
    public int hashCode() {
        var hash = 7;
        if (ownerId != null) {
            hash = 29 * hash + ownerId.intValue();
        }
        if (privileges != null) {
            hash = 29 * hash + Objects.hashCode(privileges);
        }
        for (Map.Entry<String, Serializable> attribute : attributes.entrySet()) {
            hash += attribute.hashCode();
        }
        return hash;
    }

    /**
     * Checks whether the specified content entity has parents.
     * <p>
     * The most simple way is to check parentId, in case it equals zero there
     * are no parents for the entity because it is root. Otherwise it is to be
     * someone's child node.
     * <p>
     * Please mind that there can be a number of root entities in CMS.
     *
     * @return true if current {@link Content} record is the root of hierarchy
     * @since 0.3
     */
    @org.boon.json.annotations.JsonIgnore
    @JsonIgnore
    @Override
    public boolean isRoot() {
        return parentId != null && parentId == 0;
    }

    @Override
    public Long getOwnerId() {
        return ownerId;
    }

    @Override
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public ContentPrivileges getContentPrivileges() {
        return privileges;
    }

    /**
     * Nodes are being compared by privileges and attributes, as ownerId, id
     * and parentId can be changed after object was added to the data source.
     *
     * @param obj object for comparison
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node)obj;
        if (!Objects.equals(getId(), other.getId()))
            return false;
        if (attributes != other.getAttributes()) {
            List<String> keys = new ArrayList<>();
            for (String currentKey : attributes.keySet()) {
                keys.add(currentKey);
                if ((attributes.get(keys.get(keys.size() - 1)) == null && other.getAttributes().get(keys.get(keys.size() - 1)) != null)
                        || !attributes.get(keys.get(keys.size() - 1)).equals(other.getAttribute(keys.get(keys.size() - 1)))) {
                    return false;
                }
                if (!keys.contains(currentKey)) {
                    return false;
                }
            }
        }
        return Objects.equals(privileges, other.privileges);
    }

    /**
     * Sets default privileges for {@link Content} record
     */
    @Override
    public void setDefaultPrivileges() {
        var ownerPrivilege = new Privilege();
        ownerPrivilege.setCreateChildren(true);
        ownerPrivilege.setRead(true);
        ownerPrivilege.setWrite(true);
        ownerPrivilege.setExecute(true);

        var rolePrivilege = new Privilege();
        rolePrivilege.setCreateChildren(true);
        rolePrivilege.setRead(true);
        rolePrivilege.setWrite(true);
        rolePrivilege.setExecute(false);

        var othersPrivilege = new Privilege();
        othersPrivilege.setCreateChildren(false);
        othersPrivilege.setRead(true);
        othersPrivilege.setWrite(false);
        othersPrivilege.setExecute(false);

        var cp = new ContentPrivileges();
        cp.setOwnerPrivileges(ownerPrivilege);
        cp.setRolePrivileges(rolePrivilege);
        cp.setOtherPrivileges(othersPrivilege);

        privileges = cp;
    }

    /**
     * Sets privileges for content.
     *
     * @param contentPrivileges permissions object
     */
    @Override
    public void setContentPrivileges(ContentPrivileges contentPrivileges) {
        privileges = contentPrivileges;
    }

    @JsonIgnore
    @Override
    public Class<Serializable> getType() {
        return Serializable.class;
    }

    @Override
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @Override
    public String getEntityType() {
        return entityType;
    }

    /**
     * Unfortunately does not work with inner classes and classes without default constructor
     *
     * @param value object to be saved as an attribute
     * @return value as Serializable object
     */
    @Override
    protected Serializable convertToPersistentValue(Object value) {
        return value == null
                ? null
                : getSerializable(value);
    }

    private Serializable getSerializable(Object value) {
        var enhancer = new Enhancer();
        enhancer.setSuperclass(value.getClass());
        enhancer.setInterfaces(new Class[]{Serializable.class});
        enhancer.setCallback((MethodInterceptor)Node::intercept);
        return (Serializable)enhancer.create();
    }

    private static Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return proxy.invokeSuper(obj, args);
    }
}
