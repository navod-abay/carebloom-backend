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
                
                // Path-based role determination - check only the relevant repository
                if (path.startsWith("/api/v1/admin/")) {
                    authenticateForRole(firebaseUid, "PLATFORM_MANAGER", platformAdminRepository);
                } 
                else if (path.startsWith("/api/v1/mother/")) {
                    authenticateForRole(firebaseUid, "MOTHER", motherRepository);
                } 
                else if (path.startsWith("/api/v1/midwife/")) {
                    authenticateForRole(firebaseUid, "MIDWIFE", midwifeRepository);
                } 
                else if (path.startsWith("/api/v1/vendor/")) {
                    authenticateForRole(firebaseUid, "VENDOR", vendorRepository);
                } 
                else if (path.startsWith("/api/v1/moh/")) {
                    authenticateForRole(firebaseUid, "MOH_OFFICE_USER", moHOfficeUserRepository);
                }
            } catch (Exception e) {
                logger.error("Token verification failed for path: {}", path, e);
                // Don't set authentication - let Spring Security handle the rejection
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Authenticate user for a specific role by checking only the relevant repository
     */
    private void authenticateForRole(String firebaseUid, String role, Object repository) {
        boolean userExists = false;
        
        // Check the appropriate repository based on type
        if (repository instanceof PlatformAdminRepository) {
            PlatformAdmin admin = ((PlatformAdminRepository) repository).findByFirebaseUid(firebaseUid);
            userExists = (admin != null);
        } 
        else if (repository instanceof MotherRepository) {
            Mother mother = ((MotherRepository) repository).findByFirebaseUid(firebaseUid);
            userExists = (mother != null);
        } 
        else if (repository instanceof MidwifeRepository) {
            Midwife midwife = ((MidwifeRepository) repository).findByFirebaseUid(firebaseUid);
            userExists = (midwife != null);
        } 
        else if (repository instanceof VendorRepository) {
            Vendor vendor = ((VendorRepository) repository).findByFirebaseUid(firebaseUid);
            userExists = (vendor != null);
        } 
        else if (repository instanceof MoHOfficeUserRepository) {
            MoHOfficeUser moHUser = ((MoHOfficeUserRepository) repository).findByFirebaseUid(firebaseUid);
            userExists = (moHUser != null && "active".equals(moHUser.getState()));
        }
        
        // If user exists in the repository, authenticate with the appropriate role
        if (userExists) {
            logger.info("Authenticated user: {} as role: {}", firebaseUid, role);
            
            // Create authentication with proper role
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
        } else {
            logger.warn("User {} attempted to access {} resources but was not found", 
                      firebaseUid, role);
        }
    }
}