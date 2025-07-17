package com.example.carebloom.controllers.midwife;

import com.example.carebloom.dto.midwife.FieldVisitCreateDTO;
import com.example.carebloom.dto.midwife.FieldVisitResponseDTO;
import com.example.carebloom.services.midwife.FieldVisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "${app.cors.midwife-origin}")
@RequestMapping("/api/v1/midwife")
public class FieldVisitController {

    private static final Logger logger = LoggerFactory.getLogger(FieldVisitController.class);

    @Autowired
    private FieldVisitService fieldVisitService;

    @PostMapping("/field-visits")
    public ResponseEntity<?> createFieldVisit(@RequestBody FieldVisitCreateDTO createDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                logger.debug("Midwife is not authenticated or anonymous");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not signed in properly");
            }

            String firebaseUid = authentication.getName();
            logger.debug("Creating field visit for midwife with Firebase UID: {}", firebaseUid);

            FieldVisitResponseDTO fieldVisit = fieldVisitService.createFieldVisit(createDTO, firebaseUid);
            
            logger.info("Field visit created successfully for midwife: {}", firebaseUid);
            return ResponseEntity.status(HttpStatus.CREATED).body(fieldVisit);

        } catch (Exception e) {
            logger.error("Error creating field visit: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to create field visit: " + e.getMessage());
        }
    }

    @GetMapping("/field-visits")
    public ResponseEntity<?> getFieldVisits() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                logger.debug("Midwife is not authenticated or anonymous");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not signed in properly");
            }

            String firebaseUid = authentication.getName();
            logger.debug("Getting field visits for midwife with Firebase UID: {}", firebaseUid);

            List<FieldVisitResponseDTO> fieldVisits = fieldVisitService.getFieldVisitsByMidwife(firebaseUid);
            
            logger.info("Retrieved {} field visits for midwife: {}", fieldVisits.size(), firebaseUid);
            return ResponseEntity.ok(fieldVisits);

        } catch (Exception e) {
            logger.error("Error getting field visits: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to get field visits: " + e.getMessage());
        }
    }
}
