package com.example.carebloom.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.example.carebloom.repositories.PlatformAdminRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.VendorRepository;
import com.example.carebloom.models.PlatformAdmin;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class RoleAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleAuthenticationFilter.class);
    
    @Autowired
    private PlatformAdminRepository platformAdminRepository;
    
    @Autowired
    private MotherRepository motherRepository;
    
    @Autowired
    private MoHOfficeUserRepository moHOfficeUserRepository;
    
    @Autowired
    private MidwifeRepository midwifeRepository;
    
    @Autowired
    private VendorRepository vendorRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();
        
        // Only process if there's a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.replace("Bearer ", "");
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                String firebaseUid = decodedToken.getUid();
                
                logger.debug("Processing authentication for path: {}, UID: {}", path, firebaseUid);
                
                // Path-based repository check - only query the relevant repository based on the path
                if (path.startsWith("/api/v1/admin/")) {
                    authenticateAdmin(firebaseUid);
                } 
                else if (path.startsWith("/api/v1/mothers/")) {
                    authenticateMother(firebaseUid);
                } 
                else if (path.startsWith("/api/v1/midwife/")) {
                    authenticateMidwife(firebaseUid);
                } 
                else if (path.startsWith("/api/v1/vendor/")) {
                    authenticateVendor(firebaseUid);
                } 
                else if (path.startsWith("/api/v1/moh/")) {
                    authenticateMohUser(firebaseUid);
                }
            } catch (Exception e) {
                logger.error("Token verification failed for path: {}", path, e);
                // Don't set authentication - let Spring Security handle the rejection
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Authenticate a platform admin user
     */
    private void authenticateAdmin(String firebaseUid) {
        PlatformAdmin admin = platformAdminRepository.findByFirebaseUid(firebaseUid);
        if (admin != null) {
            setAuthentication(firebaseUid, "PLATFORM_MANAGER");
            logger.info("Authenticated admin: {}", firebaseUid);
        } else {
            logger.warn("User {} attempted to access admin resources but was not found", firebaseUid);
        }
    }
    
    /**
     * Authenticate a mother user
     */
    private void authenticateMother(String firebaseUid) {
        Mother mother = motherRepository.findByFirebaseUid(firebaseUid);
        if (mother != null) {
            setAuthentication(firebaseUid, "MOTHER");
            logger.info("Authenticated mother: {}", firebaseUid);
        } else {
            logger.warn("User {} attempted to access mother resources but was not found", firebaseUid);
        }
    }
    
    /**
     * Authenticate a midwife user
     */
    private void authenticateMidwife(String firebaseUid) {
        Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
        if (midwife != null) {
            setAuthentication(firebaseUid, "MIDWIFE");
            logger.info("Authenticated midwife: {}", firebaseUid);
        } else {
            logger.warn("User {} attempted to access midwife resources but was not found", firebaseUid);
        }
    }
    
    /**
     * Authenticate a vendor user
     */
    private void authenticateVendor(String firebaseUid) {
        Vendor vendor = vendorRepository.findByFirebaseUid(firebaseUid);
        if (vendor != null) {
            setAuthentication(firebaseUid, "VENDOR");
            logger.info("Authenticated vendor: {}", firebaseUid);
        } else {
            logger.warn("User {} attempted to access vendor resources but was not found", firebaseUid);
        }
    }
    
    /**
     * Authenticate a MOH Office user, determining their specific role based on the role field first, then accountType
     */
    private void authenticateMohUser(String firebaseUid) {
        MoHOfficeUser mohUser = moHOfficeUserRepository.findByFirebaseUid(firebaseUid);
        
        // Only authenticate active MoH users
        if (mohUser != null && "active".equals(mohUser.getState())) {

            String role = "admin".equals(mohUser.getAccountType()) ? "MOH_OFFICE_ADMIN" : "MOH_OFFICE_USER";
            
            setAuthentication(firebaseUid, role);
            logger.info("Authenticated MOH user: {} as role: {}", firebaseUid, role);
        } else {
            logger.warn("User {} attempted to access MOH resources but was not found or not active", firebaseUid);
        }
    }
    
    /**
     * Set authentication in the security context with the specified role
     */
    private void setAuthentication(String firebaseUid, String role) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role)
        );
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                firebaseUid, 
                null, 
                authorities
            );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}