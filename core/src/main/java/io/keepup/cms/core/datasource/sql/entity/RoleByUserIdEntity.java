package io.keepup.cms.core.datasource.sql.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Database entity connecting multiple roles and users
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@Entity
@Table(name = "user_roles", indexes = {
        @Index(name = "IDX_USER_ROLES_ID", columnList = "id"),
        @Index(name = "IDX_USER_ROLES", columnList = "user_id")})
public class RoleByUserIdEntity implements Serializable {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
