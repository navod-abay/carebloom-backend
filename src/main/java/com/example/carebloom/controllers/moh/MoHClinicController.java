package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.Mother;
import com.example.carebloom.services.moh.MoHClinicService;
import com.example.carebloom.dto.CreateClinicRequest;
import com.example.carebloom.dto.CreateClinicResponse;
import com.example.carebloom.dto.UpdateClinicRequest;
import com.example.carebloom.dto.moh.AvailableMothersResponse;
import com.example.carebloom.dto.moh.ClinicWithMothersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MoHClinicController {
    private static final Logger logger = LoggerFactory.getLogger(MoHClinicController.class);

    @Autowired
    private MoHClinicService clinicService;

    /**
     * Get all clinics for the current user's MoH office.
     * Uses Spring Security context to determine the user and MoH office.
     */
    @GetMapping("/clinics")
    public ResponseEntity<?> getAllClinicsByMohOffice() {
        try {
            List<ClinicWithMothersDto> clinics = clinicService.getAllClinicsByMohOfficeWithMothers();
            return ResponseEntity.ok(clinics);
        } catch (Exception e) {
            logger.error("Error getting clinics", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve clinics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get clinics by date for the current user's MoH office
     */
    @GetMapping("/clinics/date/{date}")
    public ResponseEntity<?> getClinicsByDate(@PathVariable String date) {
        try {
            // For now, just get all clinics by date (not filtered by user)
            // You can modify this to filter by user if needed
            List<Clinic> clinics = clinicService.getClinicsByDate(date);
            return ResponseEntity.ok(clinics);
        } catch (Exception e) {
            logger.error("Error getting clinics by date", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve clinics by date: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get a clinic by ID, ensuring it belongs to the current user's MoH office
     */
    @GetMapping("/clinics/{id}")
    public ResponseEntity<?> getClinicById(@PathVariable String id) {
        try {
            Optional<ClinicWithMothersDto> clinic = clinicService.getClinicByIdWithMothers(id);
            if (clinic.isPresent()) {
                return ResponseEntity.ok(clinic.get());
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Clinic not found or you don't have access to it");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("Error getting clinic by ID", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve clinic: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get available mothers for clinic appointments
     * This endpoint fetches all registered mothers in the current user's MoH office
     * for use when creating new clinic appointments
     */
    @GetMapping("/clinics/available-mothers")
    public ResponseEntity<?> getAvailableMothersForClinic() {
        try {
            List<Mother> availableMothers = clinicService.getAvailableMothersForClinic();
            
            if (availableMothers.isEmpty()) {
                return ResponseEntity.ok(AvailableMothersResponse.empty());
            }
            
            return ResponseEntity.ok(AvailableMothersResponse.success(availableMothers));
        } catch (Exception e) {
            logger.error("Error fetching available mothers for clinic", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch available mothers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create a new clinic for the current user's MoH office
     */
    @PostMapping("/clinics")
    public ResponseEntity<CreateClinicResponse> createClinic(@RequestBody CreateClinicRequest request) {
        try {
            CreateClinicResponse response = clinicService.createClinic(request);
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Error creating clinic", e);
            CreateClinicResponse errorResponse = new CreateClinicResponse(false,
                    "Failed to create clinic: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update a clinic, ensuring it belongs to the current user's MoH office
     */
    @PutMapping("/clinics/{id}")
    public ResponseEntity<?> updateClinic(@PathVariable String id, @RequestBody UpdateClinicRequest request) {
        try {
            Clinic updatedClinic = clinicService.updateClinic(id, request);
            if (updatedClinic != null) {
                return ResponseEntity.ok(updatedClinic);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Clinic not found or you don't have access to update it");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("Error updating clinic", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update clinic: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete a clinic, ensuring it belongs to the current user's MoH office
     */
    @DeleteMapping("/clinics/{id}")
    public ResponseEntity<?> deleteClinic(@PathVariable String id) {
        try {
            boolean deleted = clinicService.deleteClinic(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Clinic not found or you don't have access to delete it");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("Error deleting clinic", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete clinic: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    
}
