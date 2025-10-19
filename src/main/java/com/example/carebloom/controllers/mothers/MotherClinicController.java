package com.example.carebloom.controllers.mothers;

import com.example.carebloom.dto.clinics.ClinicAppointmentDto;
import com.example.carebloom.services.mothers.MotherClinicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mothers/clinics")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherClinicController {

    private static final Logger logger = LoggerFactory.getLogger(MotherClinicController.class);

    @Autowired
    private MotherClinicService motherClinicService;

    /**
     * Get all clinics where the authenticated mother is registered
     */
    @GetMapping
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<?> getMyClinicAppointments(Authentication authentication) {
        try {
            String userId = authentication.getName(); // Firebase UID
            logger.info("Fetching clinic appointments for mother: {}", userId);
            
            List<ClinicAppointmentDto> clinics = motherClinicService.getClinicAppointmentsForMother(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", clinics,
                "count", clinics.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching clinic appointments for mother", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to fetch clinic appointments"));
        }
    }

    /**
     * Get queue status for a specific clinic (if mother is registered)
     */
    @GetMapping("/{clinicId}/queue")
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<?> getQueueStatus(
            @PathVariable String clinicId,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            logger.info("Fetching queue status for clinic: {} by mother: {}", clinicId, userId);
            
            Map<String, Object> queueStatus = motherClinicService.getQueueStatusForMother(clinicId, userId);
            
            return ResponseEntity.ok(queueStatus);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching queue status for clinic: " + clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to fetch queue status"));
        }
    }
}
