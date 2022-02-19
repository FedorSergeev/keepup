package io.keepup.cms.core.datasource.sql.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * User entity. Represents entity for {@link io.keepup.cms.core.persistence.User} object which is stored
 * in the data source.
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "IDX_USER_ID", columnList = "id"),
        @Index(name = "IDX_USER_ROLE", columnList = "user_role")})
@org.springframework.data.relational.core.mapping.Table
public class UserEntity implements Serializable {

    /**
     * Primary identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_generator")
    @SequenceGenerator(name = "user_seq_generator", sequenceName = "user_seq", allocationSize = 1)
    private Long id;

    /**
     * Users name
     */
    @Column(name = "username")
    protected String username;

    /**
     * Password hash stored ini the database
     */
    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * Date when users credentials expire
     */
    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDate expirationDate;

    /**
     * Any information, e.g. JSON to map some specific users objects without additional requests
     * to user_attribute table
     */
    @Column(name = "additional_info")
    private String additionalInfo;

    /**
     * Get ID.
     *
     * @return user entity ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Define user ID.
     *
     * @param id user entity ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Define username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get hash of user's password.
     *
     * @return password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Define hash of user's password.
     *
     * @param passwordHash password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Get current user's account expiration date. Date after which the user will no longer have access to the system.
     *
     * @return user's expiration date
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Define current user's account expiration date. Date after which the user will no longer have access to the system.
     *
     * @param expirationDate user's expiration date
     */
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Get the object representing any additional information for user that for some reason could not be
     * stored in user's attributes. E.g. it can be some simple JSON string with user's meta data, which
     * is used often and there is no need to look for it in attributes stored in separate table.
     *
     * @return additional information about user stored as a string
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Set the object representing any additional information for user that for some reason could not be
     * stored in user's attributes. E.g. it can be some simple JSON string with user's meta data, which
     * is used often and there is no need to look for it in attributes stored in separate table.
     *
     * @param additionalInfo additional information about user stored as a string
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
