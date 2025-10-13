package com.example.carebloom.controllers.queue;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.carebloom.dto.queue.*;
import com.example.carebloom.models.QueueUser;
import com.example.carebloom.services.QueueService;
import com.example.carebloom.services.queue.QueueSSEService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/queue")
@CrossOrigin(origins = "*")
public class ClinicQueueController {
    private static final Logger logger = LoggerFactory.getLogger(ClinicQueueController.class);
    
    @Autowired
    private QueueService queueService;
    
    @Autowired
    private QueueSSEService queueSSEService;

    @PostMapping("/start")
    public ResponseEntity<?> startQueue(@PathVariable String clinicId) {
        try {
            QueueStatusResponse response = queueService.startQueue(clinicId);
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

    @PostMapping("/close")
    public ResponseEntity<?> closeQueue(@PathVariable String clinicId, @RequestParam(defaultValue = "false") boolean force) {
        try {
            Map<String, Object> response = queueService.closeQueue(clinicId, force);
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

    @GetMapping
    public ResponseEntity<?> getQueueStatus(@PathVariable String clinicId) {
        try {
            QueueStatusResponse response = queueService.getQueueStatus(clinicId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for getting queue status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting queue status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get queue status"));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addPatientToQueue(@PathVariable String clinicId, @RequestBody Map<String, Object> patient) {
        try {
            QueueStatusResponse response = queueService.addPatientToQueue(clinicId, patient);
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

    @PutMapping("/settings")
    public ResponseEntity<?> updateQueueSettings(@PathVariable String clinicId, @RequestBody QueueSettingsDto settings) {
        try {
            var updatedSettings = queueService.updateQueueSettings(clinicId, settings);
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

    @PostMapping("/next")
    public ResponseEntity<?> processNextPatient(@PathVariable String clinicId) {
        try {
            QueueStatusResponse response = queueService.processNextPatient(clinicId);
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

    @PutMapping("/patients/{patientId}/status")
    public ResponseEntity<?> updatePatientStatus(@PathVariable String clinicId, @PathVariable String patientId, 
                                                @RequestBody Map<String, Object> statusData) {
        try {
            QueueStatusResponse response = queueService.updatePatientStatus(clinicId, patientId, statusData);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for updating patient status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating patient status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to update patient status"));
        }
    }

    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<?> removePatientFromQueue(@PathVariable String clinicId, @PathVariable String patientId) {
        try {
            QueueStatusResponse response = queueService.removePatientFromQueue(clinicId, patientId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for removing patient from queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing patient from queue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to remove patient from queue"));
        }
    }

    @PostMapping("/reorder")
    public ResponseEntity<?> reorderQueue(@PathVariable String clinicId, @RequestBody Map<String, List<String>> reorderRequest) {
        try {
            List<String> patientIds = reorderRequest.get("patientIds");
            if (patientIds == null || patientIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Patient IDs are required"));
            }
            
            QueueStatusResponse response = queueService.reorderQueue(clinicId, patientIds);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for reordering queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for reordering queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error reordering queue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to reorder queue"));
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeCurrentPatient(@PathVariable String clinicId) {
        try {
            QueueStatusResponse response = queueService.completeCurrentPatient(clinicId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", response));
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for completing current patient: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for completing current patient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error completing current patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to complete current patient"));
        }
    }

    // GET /api/v1/clinics/{clinicId}/queue/patients - Get all patients in queue
    @GetMapping("/patients")
    public ResponseEntity<Map<String, Object>> getQueuePatients(@PathVariable String clinicId) {
        try {
            List<QueueUser> patients = queueService.getAllPatientsInQueue(clinicId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", patients));
        } catch (Exception e) {
            logger.error("Error getting queue patients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get queue patients"));
        }
    }

    // GET /api/v1/clinics/{clinicId}/queue/current - Get current patient
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentPatient(@PathVariable String clinicId) {
        try {
            QueueUser currentPatient = queueService.getCurrentPatient(clinicId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", currentPatient));
        } catch (Exception e) {
            logger.error("Error getting current patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get current patient"));
        }
    }

    // GET /api/v1/clinics/{clinicId}/queue/history - Get queue history
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getQueueHistory(@PathVariable String clinicId) {
        try {
            List<QueueUser> history = queueService.getQueueHistory(clinicId);
            return ResponseEntity.ok().body(Map.of("success", true, "data", history));
        } catch (Exception e) {
            logger.error("Error getting queue history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get queue history"));
        }
    }

    @GetMapping("/events")
    public SseEmitter subscribeToQueueUpdates(@PathVariable String clinicId, @RequestParam(value = "token", required = false) String token) {
        logger.info("Client subscribing to queue updates for clinic: {}", clinicId);
        // Validate token if present
        if (token != null && !token.isEmpty()) {
            try {
                com.google.firebase.auth.FirebaseToken decodedToken = com.google.firebase.auth.FirebaseAuth.getInstance().verifyIdToken(token);
                logger.info("SSE token validated for UID: {}", decodedToken.getUid());
            } catch (Exception e) {
                logger.warn("Invalid SSE token for clinic {}: {}", clinicId, e.getMessage());
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Invalid or expired token");
            }
        } else {
            logger.warn("Missing SSE token for clinic {}", clinicId);
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Missing token");
        }
        return queueSSEService.subscribe(clinicId);
    }
}
