package io.keepup.cms.core.persistence;

import io.keepup.cms.core.datasource.sql.EntityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class User implements UserDetails {

    private long id;
    private Collection<GrantedAuthority> authorities;
    private String password;
    private String username;
    private String additionalInfo;
    private Map<String, Serializable> attributes;
    private LocalDate expirationDate;
    private boolean enabled;

    public long getId() {
        return id;
    }

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

    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    private boolean isExpirationDateAfterCurrent() {
        return getExpirationDate().isAfter(EntityUtils.convertToLocalDateViaInstant(new Date()));
    }
}
