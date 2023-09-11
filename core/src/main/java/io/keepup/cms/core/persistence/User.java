package io.keepup.cms.core.persistence;

import io.keepup.cms.core.datasource.sql.EntityUtils;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * KeepUP user object. Stores the main information about user.
 *
 * @author Fedor Sergeev
 * @since 1.0
 * @see UserDetails
 */
@NoArgsConstructor
public class User implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Primary identifier
     */
    private long id;
    /**
     * Collection of authorities granted by {@link org.springframework.security.core.Authentication} object
     */
    private Collection<GrantedAuthority> authorities;
    /**
     * password
     */
    private String password;
    /**
     * username
     */
    private String username;
    /**
     * Any additional info which is preferred to be stored within the user object
     */
    private String additionalInfo;
    /**
     * User's attributes distributed by names
     */
    private Map<String, Serializable> attributes;
    /**
     * Date when all granted authorities are expired for user
     */
    private LocalDate expirationDate;
    /**
     * Defined whether user is enabled in the system
     */
    private boolean enabled;

    /**
     * Get user ID.
     *
     * @return user's ID
     */
    public long getId() {
        return id;
    }

    /**
     * Set user ID.
     *
     * @param id user's ID
     */
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isExpirationDateAfterCurrent();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isExpirationDateAfterCurrent();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isExpirationDateAfterCurrent();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the authorities granted to an Authentication object.
     *
     * @param authorities list of authorities granted to an Authentication object.
     */
    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    /**
     * Set user's password.
     *
     * @param password user's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
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

    /**
     * Get user's attributes in pairs with their names.
     *
     * @return map of user's attribute names and values
     */
    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    /**
     * Set user's attributes in pairs with their names.
     *
     * @param attributes map of user's attribute names and values
     */
    public void setAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    /**
     * Enable or disable user.
     *
     * @param enabled true to enable and false to disable user
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
     * Set current user's account expiration date. Date after which the user will no longer have access to the system.
     *
     * @param expirationDate user's expiration date
     */
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    private boolean isExpirationDateAfterCurrent() {
        return getExpirationDate().isAfter(EntityUtils.convertToLocalDateViaInstant(new Date()));
    }
}
