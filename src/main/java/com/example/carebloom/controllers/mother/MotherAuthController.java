package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.LocationRegistrationRequest;
import com.example.carebloom.dto.PersonalRegistrationRequest;
import com.example.carebloom.dto.RegistrationRequest;
import com.example.carebloom.models.MotherProfile;
import com.example.carebloom.services.AuthService;
import com.example.carebloom.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MotherProfile> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            MotherProfile profile = authService.verifyIdToken(idToken);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<MotherProfile> registerMother(
            @RequestHeader("Authorization") String idToken,
            @RequestBody RegistrationRequest request) {
        try {
            logger.info("Starting mother registration process for email: {}", request.getEmail());
            logger.debug("Registration request received with token length: {}", idToken.length());
            
            MotherProfile profile = authService.registerMother(idToken, request.getEmail());
            
            logger.info("Mother registration successful for email: {}, profile ID: {}", 
                       request.getEmail(), profile.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid registration data for email: {}, error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Mother registration failed for email: {}, error: {}", 
                        request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/register/personal")
    public ResponseEntity<MotherProfile> registerPersonal(
            @RequestHeader("Authorization") String idToken,
            @RequestBody PersonalRegistrationRequest request) {
        try {
            MotherProfile profile = authService.updatePersonalInfo(idToken, request);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid personal registration data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Personal registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/register/location")
    public ResponseEntity<MotherProfile> registerLocation(
            @RequestHeader("Authorization") String idToken,
            @RequestBody LocationRegistrationRequest request) {
        try {
            // Validate required fields
            if (request.getDistrict() == null || request.getDistrict().trim().isEmpty()) {
                logger.error("District is required for location registration");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            if (request.getMohOfficeId() == null || request.getMohOfficeId().trim().isEmpty()) {
                logger.error("MOH Office ID is required for location registration");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            if (request.getAreaMidwifeId() == null || request.getAreaMidwifeId().trim().isEmpty()) {
                logger.error("Area Midwife ID is required for location registration");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            // Validate GPS coordinates if provided
            if (request.getLocation() != null) {
                LocationRegistrationRequest.LocationCoordinates location = request.getLocation();
                if (location.getLatitude() != null && location.getLongitude() != null) {
                    // Validate coordinate ranges
                    if (location.getLatitude() < -90 || location.getLatitude() > 90) {
                        logger.error("Invalid latitude: {}", location.getLatitude());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    if (location.getLongitude() < -180 || location.getLongitude() > 180) {
                        logger.error("Invalid longitude: {}", location.getLongitude());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    
                    logger.info("Location registration with GPS coordinates: lat={}, lng={}, accuracy={}m", 
                               location.getLatitude(), location.getLongitude(), location.getAccuracy());
                } else {
                    logger.info("Location registration without GPS coordinates");
                }
            }
            
            MotherProfile profile = authService.updateLocation(idToken, request);
            logger.info("Location registration successful for user: {}", profile.getId());
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid location registration data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Location registration failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/register/skip-location")
    public ResponseEntity<MotherProfile> skipLocation(
            @RequestHeader("Authorization") String idToken) {
        try {
            MotherProfile profile = authService.skipLocation(idToken);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Skip location failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<MotherProfile> getProfile() {
        try {
            // Get Firebase UID from security context
            String firebaseUid = SecurityUtils.getCurrentFirebaseUid();
            if (firebaseUid == null || "anonymousUser".equals(firebaseUid)) {
                logger.debug("No valid Firebase UID found: {}", firebaseUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Try to get user entity from security context first (no DB query needed)
            Object userEntity = SecurityUtils.getCurrentUserEntity();
            if (userEntity instanceof MotherProfile) {
                logger.info("Profile retrieved from security context for Firebase UID: {}", firebaseUid);
                return ResponseEntity.ok((MotherProfile) userEntity);
            }

            // Fallback to service lookup if not in security context
            MotherProfile profile = authService.getProfileByFirebaseUid(firebaseUid);
            if (profile == null) {
                logger.debug("No profile found for Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            logger.info("Profile retrieved from service for Firebase UID: {}", firebaseUid);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error retrieving profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
