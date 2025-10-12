package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.Mother;
import com.example.carebloom.services.moh.MoHClinicService;
import com.example.carebloom.services.queue.QueueSSEService;
import com.example.carebloom.dto.CreateClinicRequest;
import com.example.carebloom.dto.CreateClinicResponse;
import com.example.carebloom.dto.UpdateClinicRequest;
import com.example.carebloom.dto.moh.AvailableMothersResponse;
import com.example.carebloom.dto.moh.ClinicWithMothersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;

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

    @Autowired
    private QueueSSEService queueSSEService;

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
     * Get queue status for a clinic (for frontend compatibility)
     */
    @GetMapping("/clinics/{clinicId}/queue/status")
    public ResponseEntity<?> getQueueStatus(@PathVariable String clinicId) {
        try {
            Object queueStatus = clinicService.getQueueStatus(clinicId); // Implement this in your service
            return ResponseEntity.ok(queueStatus);
        } catch (Exception e) {
            logger.error("Error getting queue status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get queue status: " + e.getMessage()));
        }
    }

    /**
     * SSE stream endpoint for queue updates (for frontend compatibility)
     */
    @GetMapping(value = "/clinics/{clinicId}/queue/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToQueueUpdates(@PathVariable String clinicId, @RequestParam(value = "token", required = false) String token, HttpServletResponse response) {
        logger.info("MoH client subscribing to queue updates for clinic: {}", clinicId);
        
        // Validate token if present
        if (token != null && !token.isEmpty()) {
            try {
                com.google.firebase.auth.FirebaseToken decodedToken = com.google.firebase.auth.FirebaseAuth.getInstance().verifyIdToken(token);
                logger.info("SSE token validated for UID: {}", decodedToken.getUid());
                // Return the SSE emitter
                return queueSSEService.subscribe(clinicId);
            } catch (Exception e) {
                logger.warn("Invalid SSE token for clinic {}: {}", clinicId, e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                try {
                    response.getWriter().write("{\"success\":false,\"error\":\"Invalid or expired token\"}");
                    response.getWriter().flush();
                } catch (java.io.IOException ioException) {
                    logger.error("Error writing error response", ioException);
                }
                return null;
            }
        } else {
            logger.warn("Missing SSE token for clinic {}", clinicId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write("{\"success\":false,\"error\":\"Missing token\"}");
                response.getWriter().flush();
            } catch (java.io.IOException ioException) {
                logger.error("Error writing error response", ioException);
            }
            return null;
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

    /**
     * Add mothers to a clinic for queue management
     */
    @PostMapping("/clinics/{id}/mothers")
    public ResponseEntity<?> addMothersToClinic(@PathVariable String id, @RequestBody List<String> motherIds) {
        try {
            Clinic updatedClinic = clinicService.addMothersToClinic(id, motherIds);
            if (updatedClinic != null) {
                return ResponseEntity.ok(Map.of("success", true, "data", updatedClinic));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Clinic not found or access denied"));
            }
        } catch (Exception e) {
            logger.error("Error adding mothers to clinic", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to add mothers to clinic: " + e.getMessage()));
        }
    }

    /**
     * Remove mother from a clinic
     */
    @DeleteMapping("/clinics/{id}/mothers/{motherId}")
    public ResponseEntity<?> removeMotherFromClinic(@PathVariable String id, @PathVariable String motherId) {
        try {
            Clinic updatedClinic = clinicService.removeMotherFromClinic(id, motherId);
            if (updatedClinic != null) {
                return ResponseEntity.ok(Map.of("success", true, "data", updatedClinic));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Clinic or mother not found"));
            }
        } catch (Exception e) {
            logger.error("Error removing mother from clinic", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to remove mother from clinic: " + e.getMessage()));
        }
    }

    // ===== Queue Management Endpoints =====
    
    /**
     * Start queue for a clinic
     */
    @PostMapping("/clinics/{clinicId}/queue/start")
    public ResponseEntity<?> startQueue(@PathVariable String clinicId) {
        try {
            Object response = clinicService.startQueue(clinicId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for starting queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for starting queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error starting queue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to start queue"));
        }
    }

    /**
     * Close queue for a clinic
     */
    @PostMapping("/clinics/{clinicId}/queue/close")
    public ResponseEntity<?> closeQueue(@PathVariable String clinicId, @RequestParam(defaultValue = "false") boolean force) {
        try {
            Map<String, Object> response = clinicService.closeQueue(clinicId, force);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for closing queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for closing queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error closing queue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to close queue"));
        }
    }

    /**
     * Add patient to queue
     */
    @PostMapping("/clinics/{clinicId}/queue/add")
    public ResponseEntity<?> addPatientToQueue(@PathVariable String clinicId, @RequestBody Object patient) {
        try {
            Object response = clinicService.addPatientToQueue(clinicId, patient);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error adding patient to queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding patient to queue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to add patient to queue"));
        }
    }

    /**
     * Update queue settings
     */
    @PutMapping("/clinics/{clinicId}/queue/settings")
    public ResponseEntity<?> updateQueueSettings(@PathVariable String clinicId, @RequestBody Object settings) {
        try {
            Object updatedSettings = clinicService.updateQueueSettings(clinicId, settings);
            return ResponseEntity.ok().body(Map.of("success", true, "data", updatedSettings));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for updating queue settings: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating queue settings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to update queue settings"));
        }
    }

    /**
     * Process next patient in queue
     */
    @PostMapping("/clinics/{clinicId}/queue/next")
    public ResponseEntity<?> processNextPatient(@PathVariable String clinicId) {
        try {
            Object response = clinicService.processNextPatient(clinicId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for processing next patient: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for processing next patient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing next patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to process next patient"));
        }
    }
    
}
