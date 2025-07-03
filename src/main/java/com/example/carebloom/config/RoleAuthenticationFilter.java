package com.example.carebloom.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.example.carebloom.repositories.PlatformAdminRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.models.PlatformAdmin;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.MoHOfficeUser;
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
    private MoHOfficeUserRepository mohOfficeUserRepository;
    
    @Autowired
    private MoHOfficeUserRepository moHOfficeUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.replace("Bearer ", "");
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                String firebaseUid = decodedToken.getUid();
                
                // Determine user role based on which repository contains the user
                String role = determineUserRole(firebaseUid);
                logger.info("Authenticated user: {}, Role: {}", firebaseUid, role);
                
                if (role != null) {
                    // Create authentication with proper role
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)  // Spring Security expects "ROLE_" prefix
                    );
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            firebaseUid, 
                            null, 
                            authorities
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                
            } catch (Exception e) {
                logger.error("Token verification failed", e);
                // Don't set authentication - let Spring Security handle the rejection
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String determineUserRole(String firebaseUid) {
        // Check platform admin
        PlatformAdmin admin = platformAdminRepository.findByFirebaseUid(firebaseUid);
        if (admin != null) {
            return "PLATFORM_MANAGER";
        }
        
        // Check mother
        Mother mother = motherRepository.findByFirebaseUid(firebaseUid);
        if (mother != null) {
            return "MOTHER";
        }
        
        // Check MoH Office User
        MoHOfficeUser moHOfficeUser = moHOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (moHOfficeUser != null) {
            return "MOH_OFFICE_USER";
        }
        
        return null; // User not found in any role
    }
}