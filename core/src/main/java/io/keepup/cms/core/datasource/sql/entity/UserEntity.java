package io.keepup.cms.core.datasource.sql.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * User entity
 *
 * @author Fedor Sergeev
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "IDX_USER_ID", columnList = "id"),
        @Index(name = "IDX_USER_ROLE", columnList = "user_role")})
public class UserEntity implements Serializable {

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
     * to user_attributes table
     */
    @Column(name = "additional_info")
    private String additionalInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
