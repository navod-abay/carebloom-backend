package com.example.carebloom.controllers.moh;

import com.example.carebloom.services.NewQueueService;
import com.example.carebloom.repositories.QueueUserRepository;
import com.example.carebloom.models.QueueUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/debug/queue")
@CrossOrigin(origins = {"${app.cors.moh-origin}", "http://localhost:5174", "http://localhost:5175"})
public class QueueCleanupController {
    
    private static final Logger logger = LoggerFactory.getLogger(QueueCleanupController.class);
    
    @Autowired
    private QueueUserRepository queueUserRepository;
    
    @Autowired
    private NewQueueService newQueueService;
    
    /**
     * Get all queue users in database for debugging
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllQueueUsers() {
        try {
            List<QueueUser> allQueueUsers = queueUserRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("total", allQueueUsers.size());
            response.put("queueUsers", allQueueUsers);
            
            logger.info("Found {} queue users in database", allQueueUsers.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving queue users: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Clean up all queue users for a specific clinic
     */
    @DeleteMapping("/clinic/{clinicId}")
    public ResponseEntity<Map<String, Object>> cleanupClinicQueue(@PathVariable String clinicId) {
        try {
            // Get count before deletion
            long countBeforeDeletion = queueUserRepository.countByClinicId(clinicId);
            queueUserRepository.deleteByClinicId(clinicId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", countBeforeDeletion);
            response.put("message", "Queue cleanup completed for clinic: " + clinicId);
            
            logger.info("Cleaned up {} queue users for clinic {}", countBeforeDeletion, clinicId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cleaning up queue for clinic {}: {}", clinicId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Clean up ALL queue users in database
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> cleanupAllQueues() {
        try {
            long totalCount = queueUserRepository.count();
            queueUserRepository.deleteAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", totalCount);
            response.put("message", "All queue users cleaned up");
            
            logger.warn("CLEANED UP ALL {} QUEUE USERS FROM DATABASE", totalCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cleaning up all queue users: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get queue status for a specific clinic (debugging)
     */
    @GetMapping("/status/{clinicId}")
    public ResponseEntity<Map<String, Object>> getQueueStatusDebug(@PathVariable String clinicId) {
        try {
            Map<String, Object> queueStatus = newQueueService.getQueueStatus(clinicId);
            List<QueueUser> queueUsers = queueUserRepository.findByClinicIdOrderByPosition(clinicId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("queueStatus", queueStatus);
            response.put("rawQueueUsers", queueUsers);
            response.put("totalUsers", queueUsers.size());
            
            logger.info("Queue status for clinic {}: {} users found", clinicId, queueUsers.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting queue status for clinic {}: {}", clinicId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Clean up completed and no-show patients for a specific clinic
     */
    @DeleteMapping("/completed/{clinicId}")
    public ResponseEntity<Map<String, Object>> cleanupCompletedPatients(@PathVariable String clinicId) {
        try {
            logger.info("Manual cleanup of completed patients for clinic: {}", clinicId);
            Map<String, Object> result = newQueueService.cleanupCompletedPatients(clinicId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error cleaning up completed patients for clinic: {}", clinicId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
