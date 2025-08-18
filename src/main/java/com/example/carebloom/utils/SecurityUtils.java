package com.example.carebloom.utils;

import com.example.carebloom.config.CustomAuthenticationToken;
import com.example.carebloom.models.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    
    /**
     * Get the current authenticated user's Firebase UID
     */
    public static String getCurrentFirebaseUid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return (String) auth.getPrincipal();
        }
        return null;
    }
    
    /**
     * Get the current authenticated user's MongoDB ID
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserId();
        }
        return null;
    }
    
    /**
     * Get the current authenticated user's role
     */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserRole();
        }
        return null;
    }
    
    /**
     * Get the current user entity (generic)
     */
    public static Object getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserEntity();
        }
        return null;
    }
    
    /**
     * Type-safe methods for specific user types
     */
    public static Midwife getCurrentMidwife() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserEntity(Midwife.class);
        }
        return null;
    }
    
    public static Mother getCurrentMother() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserEntity(Mother.class);
        }
        return null;
    }
    
    public static PlatformAdmin getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserEntity(PlatformAdmin.class);
        }
        return null;
    }
    
    public static MoHOfficeUser getCurrentMohUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserEntity(MoHOfficeUser.class);
        }
        return null;
    }
    
    public static Vendor getCurrentVendor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof CustomAuthenticationToken) {
            return ((CustomAuthenticationToken) auth).getUserEntity(Vendor.class);
        }
        return null;
    }
}
