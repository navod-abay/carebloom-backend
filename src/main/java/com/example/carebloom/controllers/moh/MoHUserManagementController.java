package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.services.admin.MoHOfficeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * Controller for managing MOH Office Users by MOH Office Admins
 * All endpoints in this controller require MOH_OFFICE_ADMIN role
 */
@RestController
@RequestMapping("/api/v1/moh/users")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MoHUserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(MoHUserManagementController.class);
    
    @Autowired
    private MoHOfficeUserService mohOfficeUserService;
    
    /**
     * Create a new MOH Office User (normal role) in pending state
     * Only MOH Office Admins can create new users
     */
    @PostMapping
    @PreAuthorize("hasRole('MOH_OFFICE_ADMIN')")
    public ResponseEntity<MoHOfficeUser> createUser(
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {
        
        String email = requestBody.get("email");
        
        // Get the admin who is creating this user
        String createdBy = authentication.getName(); // This will be the Firebase UID
        
        logger.info("MOH Office Admin {} creating new user with email {}", createdBy, email);
        
        MoHOfficeUser createdUser = mohOfficeUserService.createMohUser(email, createdBy);
        return ResponseEntity.ok(createdUser);
    }
}
