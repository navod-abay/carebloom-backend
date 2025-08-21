package com.example.carebloom.controllers.midwife;

import com.example.carebloom.dto.midwife.FieldVisitCreateDTO;
import com.example.carebloom.dto.midwife.FieldVisitResponseDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderResponseDTO;
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

    @GetMapping("/field-visits/{id}")
    public ResponseEntity<FieldVisitResponseDTO> getFieldVisitById(@PathVariable String id) {
        try {
            logger.debug("Getting field visit details for ID: {}", id);

            FieldVisitResponseDTO fieldVisit = fieldVisitService.getFieldVisitById(id);
            
            logger.info("Retrieved field visit details for ID: {}", id);
            return ResponseEntity.ok(fieldVisit);

        } catch (org.springframework.web.server.ResponseStatusException e) {
            logger.error("Error getting field visit by ID {}: {}", id, e.getReason());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            logger.error("Unexpected error getting field visit by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/field-visits/{id}/calculate")
    public ResponseEntity<CalculateVisitOrderResponseDTO> calculateVisitOrder(
            @PathVariable String id,
            @RequestBody CalculateVisitOrderDTO request) {
        try {
            logger.debug("Calculating visit order for field visit ID: {}", id);

            CalculateVisitOrderResponseDTO response = fieldVisitService.calculateVisitOrder(id, request);
            
            if (response.isSuccess()) {
                logger.info("Visit order calculated successfully for field visit ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed to calculate visit order for field visit ID: {}, reason: {}", id, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error calculating visit order for field visit ID {}: {}", id, e.getMessage(), e);
            
            CalculateVisitOrderResponseDTO errorResponse = new CalculateVisitOrderResponseDTO();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error occurred while calculating visit order");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
