package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.LocationRegistrationRequest;
import com.example.carebloom.dto.PersonalRegistrationRequest;
import com.example.carebloom.dto.RegistrationRequest;
import com.example.carebloom.models.UserProfile;
import com.example.carebloom.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/mothers/auth")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherAuthController {

    private static final Logger logger = LoggerFactory.getLogger(MotherAuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = authService.verifyIdToken(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerMother(
            @RequestHeader("Authorization") String idToken,
            @RequestBody RegistrationRequest request) {
        try {
            logger.info("Starting mother registration process for email: {}", request.getEmail());
            logger.debug("Registration request received with token length: {}", idToken.length());
            
            UserProfile profile = authService.registerMother(idToken, request.getEmail());
            
            logger.info("Mother registration successful for email: {}, profile ID: {}", 
                       request.getEmail(), profile.getId());
            
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            logger.error("Mother registration failed for email: {}, error: {}", 
                        request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/register/personal")
    public ResponseEntity<?> registerPersonal(
            @RequestHeader("Authorization") String idToken,
            @RequestBody PersonalRegistrationRequest request) {
        try {
            UserProfile profile = authService.updatePersonalInfo(idToken, request);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/register/location")
    public ResponseEntity<?> registerLocation(
            @RequestHeader("Authorization") String idToken,
            @RequestBody LocationRegistrationRequest request) {
        try {
            UserProfile profile = authService.updateLocation(idToken, request);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/skip-location")
    public ResponseEntity<?> skipLocation(
            @RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = authService.skipLocation(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.debug("No authentication context available");
                return ResponseEntity.ok().body(new Object()); // Return empty object
            }

            String firebaseUid = authentication.getName();
            if (firebaseUid == null || firebaseUid.isEmpty()) {
                logger.debug("No Firebase UID found in authentication context");
                return ResponseEntity.ok().body(new Object()); // Return empty object
            }

            UserProfile profile = authService.getProfileByFirebaseUid(firebaseUid);
            if (profile == null) {
                logger.debug("No profile found for Firebase UID: {}", firebaseUid);
                return ResponseEntity.ok().body(new Object()); // Return empty object
            }

            logger.info("Profile retrieved for Firebase UID: {}", firebaseUid);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            logger.error("Error retrieving profile: {}", e.getMessage(), e);
            return ResponseEntity.ok().body(new Object()); // Return empty object on error
        }
    }
}
