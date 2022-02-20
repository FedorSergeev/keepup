package io.keepup.cms.core.datasource.sql.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Database entity connecting multiple roles and users
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@Entity
@org.springframework.data.relational.core.mapping.Table
@Table(name = "user_roles", indexes = {
        @Index(name = "IDX_USER_ROLES_ID", columnList = "id"),
        @Index(name = "IDX_USER_ROLES", columnList = "user_id")})
public class RoleByUserIdEntity implements Serializable {

    /**
     * Primary identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_roles_seq_generator")
    @SequenceGenerator(name = "user_role_seq_generator", sequenceName = "user_roles_seq", allocationSize = 1)
    private Long id;

    /**
     * User's identifier
     */
    @Column(name = "user_id")
    protected Long userId;
    /**
     * User's role
     */
    @Column(name = "role")
    private String role;

    /**
     * Get entity ID.
     *
     * @return user's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set entity ID.
     *
     * @param id entity ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get user's ID.
     *
     * @return user's ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Set user's ID.
     *
     * @param userId  user's ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Get user's role.
     *
     * @return user's role
     */
    public String getRole() {
        return role;
    }

    /**
     * Set user's ID.
     *
     * @param role  user's role
     */
    public void setRole(String role) {
        this.role = role;
    }
}
