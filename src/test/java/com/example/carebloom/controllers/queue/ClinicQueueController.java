package com.example.carebloom.controllers.queue;

import com.example.carebloom.services.QueueService;
import com.example.carebloom.services.queue.QueueSSEService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class ClinicQueueController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClinicQueueController.class);
    
    @Autowired
    private QueueService queueService;
    
    @Autowired
    private QueueSSEService queueSSEService;

    /**
     * Get queue status for a clinic
     */
    @GetMapping("/clinics/{clinicId}/queue/status")
    public ResponseEntity<?> getQueueStatus(@PathVariable String clinicId) {
        try {
            Object queueStatus = queueService.getQueueStatus(clinicId);
            return ResponseEntity.ok(Map.of("success", true, "data", queueStatus));
        } catch (Exception e) {
            logger.error("Error getting queue status for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get queue status: " + e.getMessage()));
        }
    }

    /**
     * Start queue for a clinic
     */
    @PostMapping("/clinics/{clinicId}/queue/start")
    public ResponseEntity<?> startQueue(@PathVariable String clinicId) {
        try {
            Object response = queueService.startQueue(clinicId);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for starting queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for starting queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error starting queue for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to start queue: " + e.getMessage()));
        }
    }

    /**
     * Close queue for a clinic
     */
    @PostMapping("/clinics/{clinicId}/queue/close")
    public ResponseEntity<?> closeQueue(@PathVariable String clinicId, @RequestParam(defaultValue = "false") boolean force) {
        try {
            Map<String, Object> response = queueService.closeQueue(clinicId, force);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for closing queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for closing queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error closing queue for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to close queue: " + e.getMessage()));
        }
    }

    /**
     * Add patient to queue
     */
    @PostMapping("/clinics/{clinicId}/queue/add")
    public ResponseEntity<?> addPatientToQueue(@PathVariable String clinicId, @RequestBody Map<String, Object> patient) {
        try {
            Object response = queueService.addPatientToQueue(clinicId, patient);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error adding patient to queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding patient to queue for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to add patient to queue: " + e.getMessage()));
        }
    }

    /**
     * Process next patient in queue
     */
    @PostMapping("/clinics/{clinicId}/queue/next")
    public ResponseEntity<?> processNextPatient(@PathVariable String clinicId) {
        try {
            Object response = queueService.processNextPatient(clinicId);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error processing next patient: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing next patient for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to process next patient: " + e.getMessage()));
        }
    }

    /**
     * Complete current patient
     */
    @PostMapping("/clinics/{clinicId}/queue/complete")
    public ResponseEntity<?> completeCurrentPatient(@PathVariable String clinicId) {
        try {
            Object response = queueService.completeCurrentPatient(clinicId);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error completing current patient: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error completing current patient for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to complete current patient: " + e.getMessage()));
        }
    }

    /**
     * Remove patient from queue
     */
    @DeleteMapping("/clinics/{clinicId}/queue/patients/{patientId}")
    public ResponseEntity<?> removePatient(@PathVariable String clinicId, @PathVariable String patientId) {
        try {
            Object response = queueService.removePatientFromQueue(clinicId, patientId);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error removing patient from queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing patient from queue for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to remove patient: " + e.getMessage()));
        }
    }

    /**
     * Update patient status
     */
    @PutMapping("/clinics/{clinicId}/queue/patients/{patientId}/status")
    public ResponseEntity<?> updatePatientStatus(@PathVariable String clinicId, @PathVariable String patientId, 
                                                 @RequestBody Map<String, Object> statusData) {
        try {
            Object response = queueService.updatePatientStatus(clinicId, patientId, statusData);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error updating patient status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating patient status for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to update patient status: " + e.getMessage()));
        }
    }

    /**
     * Get all patients in queue
     */
    @GetMapping("/clinics/{clinicId}/queue/patients")
    public ResponseEntity<?> getAllPatients(@PathVariable String clinicId) {
        try {
            Object response = queueService.getAllPatientsInQueue(clinicId);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            logger.error("Error getting all patients for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get patients: " + e.getMessage()));
        }
    }

    /**
     * Get current patient
     */
    @GetMapping("/clinics/{clinicId}/queue/current")
    public ResponseEntity<?> getCurrentPatient(@PathVariable String clinicId) {
        try {
            Object response = queueService.getCurrentPatient(clinicId);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            logger.error("Error getting current patient for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get current patient: " + e.getMessage()));
        }
    }

    /**
     * Get queue history
     */
    @GetMapping("/clinics/{clinicId}/queue/history")
    public ResponseEntity<?> getQueueHistory(@PathVariable String clinicId) {
        try {
            Object response = queueService.getQueueHistory(clinicId);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            logger.error("Error getting queue history for clinic: {}", clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get queue history: " + e.getMessage()));
        }
    }

    /**
     * SSE stream endpoint for queue updates
     */
    @GetMapping(value = "/clinics/{clinicId}/queue/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToQueueUpdates(@PathVariable String clinicId, 
                                              @RequestParam(value = "token", required = false) String token, 
                                              HttpServletResponse response) {
        logger.info("Client subscribing to queue updates for clinic: {}", clinicId);
        
        // Validate token if present
        if (token != null && !token.isEmpty()) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                logger.info("SSE token validated for UID: {}", decodedToken.getUid());
                // Return the SSE emitter
                return queueSSEService.subscribe(clinicId);
            } catch (Exception e) {
                logger.warn("Invalid SSE token for clinic {}: {}", clinicId, e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                try {
                    response.getWriter().write("{\"success\":false,\"error\":\"Invalid or expired token\"}");
                    response.getWriter().flush();
                } catch (IOException ioException) {
                    logger.error("Error writing error response", ioException);
                }
                return null;
            }
        } else {
            // Missing token
            logger.warn("Missing SSE token for clinic {}", clinicId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write("{\"success\":false,\"error\":\"Missing token\"}");
                response.getWriter().flush();
            } catch (IOException ioException) {
                logger.error("Error writing error response", ioException);
            }
            return null;
        }
    }
}
