package com.example.carebloom.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {
    
    private final String userId;
    private final String userRole;
    private final Object userEntity;
    
    public CustomAuthenticationToken(Object principal, Object credentials, 
                                   Collection<? extends GrantedAuthority> authorities,
                                   String userId, String userRole, Object userEntity) {
        super(principal, credentials, authorities);
        this.userId = userId;
        this.userRole = userRole;
        this.userEntity = userEntity;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public Object getUserEntity() {
        return userEntity;
    }
    
    /**
     * Type-safe getter for specific user entity types
     */
    @SuppressWarnings("unchecked")
    public <T> T getUserEntity(Class<T> type) {
        if (userEntity != null && type.isInstance(userEntity)) {
            return (T) userEntity;
        }
        return null;
    }
}
