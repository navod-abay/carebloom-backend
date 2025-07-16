package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Clinic;
import com.example.carebloom.services.moh.MoHClinicService;
import com.example.carebloom.services.queue.QueueService;
import com.example.carebloom.dto.CreateClinicResponse;
import com.example.carebloom.dto.queue.*;
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

    @Autowired
    private QueueService queueService;

    /**
     * Get all clinics for the current user's MoH office.
     * Uses Spring Security context to determine the user and MoH office.
     */
    @GetMapping("/clinics")
    public ResponseEntity<?> getAllClinicsByMohOffice() {
        try {
            List<Clinic> clinics = clinicService.getAllClinicsByMohOffice();
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
            Optional<Clinic> clinic = clinicService.getClinicById(id);
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
     * Create a new clinic for the current user's MoH office
     */
    @PostMapping("/clinics")
    public ResponseEntity<CreateClinicResponse> createClinic(@RequestBody Clinic clinic) {
        try {
            CreateClinicResponse response = clinicService.createClinic(clinic);
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
    public ResponseEntity<?> updateClinic(@PathVariable String id, @RequestBody Clinic clinic) {
        try {
            Clinic updatedClinic = clinicService.updateClinic(id, clinic);
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

    // ===== QUEUE MANAGEMENT ENDPOINTS =====

    /**
     * Start queue for a clinic
     */
    @PostMapping("/clinics/{clinicId}/queue/start")
    public ResponseEntity<?> startQueue(@PathVariable String clinicId, 
                                       @RequestBody StartQueueRequest request) {
        try {
            QueueResponse response = queueService.startQueue(clinicId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error starting queue for clinic: {}", clinicId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to start queue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add users to queue
     */
    @PostMapping("/clinics/{clinicId}/queue/add-users")
    public ResponseEntity<?> addUsersToQueue(@PathVariable String clinicId, 
                                            @RequestBody AddUsersToQueueRequest request) {
        try {
            QueueResponse response = queueService.addUsersToQueue(clinicId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding users to queue for clinic: {}", clinicId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add users to queue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Complete current appointment
     */
    @PostMapping("/clinics/{clinicId}/queue/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable String clinicId) {
        try {
            QueueResponse response = queueService.completeAppointment(clinicId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error completing appointment for clinic: {}", clinicId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to complete appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Close queue
     */
    @PostMapping("/clinics/{clinicId}/queue/close")
    public ResponseEntity<?> closeQueue(@PathVariable String clinicId) {
        try {
            QueueResponse response = queueService.closeQueue(clinicId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error closing queue for clinic: {}", clinicId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to close queue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get queue status
     */
    @GetMapping("/clinics/{clinicId}/queue")
    public ResponseEntity<?> getQueueStatus(@PathVariable String clinicId) {
        try {
            QueueResponse response = queueService.getQueueStatus(clinicId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting queue status for clinic: {}", clinicId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get queue status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get queue statistics
     */
    @GetMapping("/clinics/{clinicId}/queue/statistics")
    public ResponseEntity<?> getQueueStatistics(@PathVariable String clinicId) {
        try {
            QueueResponse response = queueService.getQueueStatus(clinicId);
            if (response.isSuccess() && response.getData() instanceof QueueStatusDto) {
                QueueStatusDto statusDto = (QueueStatusDto) response.getData();
                
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("completedAppointments", statusDto.getCompletedCount());
                statistics.put("currentQueueLength", statusDto.getStatistics().getQueueLength());
                statistics.put("averageWaitTime", statusDto.getStatistics().getAvgWaitTime());
                statistics.put("queueStatus", statusDto.getStatus());
                
                // Calculate estimated completion time
                int totalRemainingTime = statusDto.getStatistics().getQueueLength() * statusDto.getSettings().getAvgAppointmentTime();
                String estimatedCompletionTime = java.time.LocalDateTime.now()
                    .plusMinutes(totalRemainingTime)
                    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                statistics.put("estimatedCompletionTime", estimatedCompletionTime);
                
                Map<String, Object> statsResponse = new HashMap<>();
                statsResponse.put("success", true);
                statsResponse.put("data", statistics);
                
                return ResponseEntity.ok(statsResponse);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            logger.error("Error getting queue statistics for clinic: {}", clinicId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get queue statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
