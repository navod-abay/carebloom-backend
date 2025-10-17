package com.example.carebloom.controllers.moh;

import com.example.carebloom.dto.queue.QueueUserDto;
import com.example.carebloom.services.NewQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class NewMoHQueueController {
    private static final Logger logger = LoggerFactory.getLogger(NewMoHQueueController.class);

    @Autowired
    private NewQueueService newQueueService;

    /**
     * Get queue status for a clinic
     */
    @GetMapping("/clinics/{clinicId}/queue/status")
    public ResponseEntity<Map<String, Object>> getQueueStatus(@PathVariable String clinicId) {
        try {
            Map<String, Object> response = newQueueService.getQueueStatus(clinicId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for queue status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting queue status for clinic: " + clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to get queue status"));
        }
    }

    /**
     * Start queue for a clinic
     */
    @PostMapping("/clinics/{clinicId}/queue/start")
    public ResponseEntity<Map<String, Object>> startQueue(@PathVariable String clinicId) {
        try {
            Map<String, Object> response = newQueueService.startQueue(clinicId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for starting queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for starting queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error starting queue for clinic: " + clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to start queue"));
        }
    }

    /**
     * Add patient to queue
     */
    @PostMapping("/clinics/{clinicId}/queue/add")
    public ResponseEntity<Map<String, Object>> addPatientToQueue(@PathVariable String clinicId, @RequestBody QueueUserDto patient) {
        try {
            Map<String, Object> response = newQueueService.addPatientToQueue(clinicId, patient);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error adding patient to queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding patient to queue for clinic: " + clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to add patient to queue"));
        }
    }

    /**
     * Close queue for a clinic
     */
    @PostMapping("/clinics/{clinicId}/queue/close")
    public ResponseEntity<Map<String, Object>> closeQueue(@PathVariable String clinicId, @RequestParam(defaultValue = "false") boolean force) {
        try {
            Map<String, Object> response = newQueueService.closeQueue(clinicId, force);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for closing queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for closing queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error closing queue for clinic: " + clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to close queue"));
        }
    }

    /**
     * Process next patient in queue
     */
    @PostMapping("/clinics/{clinicId}/queue/next")
    public ResponseEntity<Map<String, Object>> processNextPatient(@PathVariable String clinicId) {
        try {
            Map<String, Object> response = newQueueService.processNextPatient(clinicId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for processing next patient: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing next patient for clinic: " + clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to process next patient"));
        }
    }

    /**
     * Remove a patient from the queue
     */
    @DeleteMapping("/clinics/{clinicId}/queue/patients/{patientId}")
    public ResponseEntity<Map<String, Object>> removePatient(@PathVariable String clinicId, @PathVariable String patientId) {
        try {
            Map<String, Object> response = newQueueService.removePatientFromQueue(clinicId, patientId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error removing patient from queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing patient {} from clinic: {}", patientId, clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to remove patient from queue"));
        }
    }

    /**
     * Reorder the queue
     */
    @PutMapping("/clinics/{clinicId}/queue/reorder")
    public ResponseEntity<Map<String, Object>> reorderQueue(@PathVariable String clinicId, @RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<String> patientIds = (List<String>) requestBody.get("patientIds");
            
            if (patientIds == null || patientIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Patient IDs are required"));
            }
            
            Map<String, Object> response = newQueueService.reorderQueue(clinicId, patientIds);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error reordering queue: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error reordering queue for clinic: " + clinicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to reorder queue"));
        }
    }
}