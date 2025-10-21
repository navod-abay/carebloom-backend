package com.example.carebloom.services;

import com.example.carebloom.dto.queue.QueueUserDto;
import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.QueueUser;
import com.example.carebloom.repositories.ClinicRepository;
import com.example.carebloom.repositories.QueueUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@Transactional
public class NewQueueService {
    private static final Logger logger = LoggerFactory.getLogger(NewQueueService.class);
    
    @Autowired
    private ClinicRepository clinicRepository;
    
    @Autowired
    private QueueUserRepository queueUserRepository;

    /**
     * Start an empty queue for a clinic
     */
    public Map<String, Object> startQueue(String clinicId) {
        logger.info("Starting new empty queue for clinic: {}", clinicId);
        
        // Validate clinic exists
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (clinicOpt.isEmpty()) {
            throw new IllegalArgumentException("Clinic not found");
        }
        
        Clinic clinic = clinicOpt.get();
        
        // Check if queue is already active
        if ("open".equals(clinic.getQueueStatus())) {
            throw new IllegalStateException("Queue is already active for this clinic");
        }
        
        // Always clear any existing queue data (including old completed/no-show patients from previous sessions)
        // This ensures a clean start for each queue session
        queueUserRepository.deleteByClinicId(clinicId);
        logger.info("Cleared all existing queue data for clinic: {} (including old completed/no-show patients)", clinicId);
        
        // Reset completed count for the day
        clinic.setCompletedToday(0);
        clinic.setQueueStatus("open");
        clinicRepository.save(clinic);
        
        logger.info("Empty queue started successfully for clinic: {}", clinicId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Queue started successfully");
        response.put("clinicId", clinicId);
        response.put("isActive", true);
        response.put("currentPatient", null);
        response.put("waitingQueue", List.of());
        response.put("totalPatients", 0);
        response.put("completedToday", 0);
        
        return response;
    }

    /**
     * Get current queue status
     */
    public Map<String, Object> getQueueStatus(String clinicId) {
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (clinicOpt.isEmpty()) {
            throw new IllegalArgumentException("Clinic not found");
        }
        
        Clinic clinic = clinicOpt.get();
        boolean isActive = "open".equals(clinic.getQueueStatus());
        
        // Get all queue users for this clinic, ordered by position
        List<QueueUser> queueUsers = queueUserRepository.findByClinicIdOrderByPosition(clinicId);
        
        // Find current patient (status = "in-progress")
        QueueUser currentPatient = queueUsers.stream()
            .filter(user -> "in-progress".equals(user.getStatus()))
            .findFirst()
            .orElse(null);
        
        // Get waiting patients
        List<QueueUser> waitingPatients = queueUsers.stream()
            .filter(user -> "waiting".equals(user.getStatus()))
            .toList();
        
        Integer completedCount = clinic.getCompletedToday() != null ? clinic.getCompletedToday() : 0;
        logger.info("Queue status for clinic {}: {} patients in queue, {} completed today", 
                   clinicId, queueUsers.size(), completedCount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("clinicId", clinicId);
        response.put("isActive", isActive);
        response.put("currentPatient", currentPatient);
        response.put("waitingQueue", waitingPatients);
        response.put("totalPatients", queueUsers.size());
        response.put("completedToday", completedCount);
        
        return response;
    }

    /**
     * Add a patient to the queue
     */
    public Map<String, Object> addPatientToQueue(String clinicId, QueueUserDto patientDto) {
        logger.info("Adding patient to queue: {} for clinic: {}", patientDto.getName(), clinicId);
        
        // Validate clinic and queue status
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (clinicOpt.isEmpty()) {
            throw new IllegalArgumentException("Clinic not found");
        }
        
        Clinic clinic = clinicOpt.get();
        if (!"open".equals(clinic.getQueueStatus())) {
            throw new IllegalStateException("Queue is not currently open");
        }
        
        // Check if patient already in queue (only check for active patients: waiting or in-progress)
        Optional<QueueUser> existingUser = queueUserRepository.findByMotherIdAndClinicId(patientDto.getMotherId(), clinicId);
        if (existingUser.isPresent()) {
            QueueUser existing = existingUser.get();
            // Only prevent adding if patient is currently waiting or in-progress
            // Allow re-adding completed or no-show patients
            if ("waiting".equals(existing.getStatus()) || "in-progress".equals(existing.getStatus())) {
                throw new IllegalStateException("Patient is already in the queue");
            } else {
                // Patient was completed/no-show, remove old record and allow re-adding
                logger.info("Removing old {} patient {} to allow re-adding", existing.getStatus(), existing.getName());
                queueUserRepository.delete(existing);
            }
        }
        
        // Check if there's already a patient in-progress
        Optional<QueueUser> currentPatient = queueUserRepository.findByClinicIdAndStatus(clinicId, "in-progress");
        boolean hasCurrentPatient = currentPatient.isPresent();
        
        // Get current queue size to determine position
        long currentQueueSize = queueUserRepository.countByClinicId(clinicId);
        
        // Create new queue user
        QueueUser queueUser = new QueueUser();
        queueUser.setName(patientDto.getName());
        queueUser.setEmail(patientDto.getEmail());
        queueUser.setMotherId(patientDto.getMotherId());
        queueUser.setClinicId(clinicId);
        queueUser.setPosition((int) currentQueueSize + 1);
        
        // Set status: only set to "in-progress" if there's no current patient
        queueUser.setStatus(hasCurrentPatient ? "waiting" : "in-progress");
        queueUser.setJoinedTime(LocalDateTime.now().toString());
        queueUser.setWaitTime(currentQueueSize == 0 ? 0 : (int) (currentQueueSize * 15)); // 15 min per appointment
        queueUser.setNotes(patientDto.getNotes());
        
        // Save the queue user
        queueUser = queueUserRepository.save(queueUser);
        
        logger.info("Patient {} added to queue at position {} with status {} (hasCurrentPatient: {}, queueSize: {})", 
                   patientDto.getName(), queueUser.getPosition(), queueUser.getStatus(), hasCurrentPatient, currentQueueSize);
        
        return Map.of(
            "success", true,
            "message", "Patient added to queue successfully",
            "patient", queueUser
        );
    }

    /**
     * Close the queue
     */
    public Map<String, Object> closeQueue(String clinicId, boolean force) {
        logger.info("Closing queue for clinic: {}, force: {}", clinicId, force);
        
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (clinicOpt.isEmpty()) {
            throw new IllegalArgumentException("Clinic not found");
        }
        
        Clinic clinic = clinicOpt.get();
        
        if (!force) {
            // Check if there are patients still waiting
            long waitingCount = queueUserRepository.countByClinicIdAndStatus(clinicId, "waiting");
            if (waitingCount > 0) {
                throw new IllegalStateException("Cannot close queue with waiting patients. Use force=true to override.");
            }
        }
        
        // Update clinic status
        clinic.setQueueStatus("closed");
        clinicRepository.save(clinic);
        
        // Clean up any remaining completed and no-show patients when closing queue
        // (Note: completed patients are now deleted immediately in processNext, but clean up any remaining)
        List<QueueUser> completedPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "completed");
        List<QueueUser> noShowPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "no-show");
        
        if (!completedPatients.isEmpty() || !noShowPatients.isEmpty()) {
            logger.info("Removing {} completed and {} no-show patients from clinic {}", 
                       completedPatients.size(), noShowPatients.size(), clinicId);
            
            queueUserRepository.deleteAll(completedPatients);
            queueUserRepository.deleteAll(noShowPatients);
        } else {
            logger.info("No completed or no-show patients to clean up for clinic {}", clinicId);
        }
        
        // If force close, also remove waiting and in-progress patients
        if (force) {
            queueUserRepository.deleteByClinicId(clinicId);
            logger.info("Force close: Removed all remaining patients from clinic {}", clinicId);
        }
        
        logger.info("Queue closed successfully for clinic: {}", clinicId);
        
        return Map.of(
            "success", true,
            "message", "Queue closed successfully",
            "clinicId", clinicId
        );
    }

    /**
     * Process next patient (move current to completed, next waiting to in-progress)
     */
    public Map<String, Object> processNextPatient(String clinicId) {
        logger.info("Processing next patient for clinic: {}", clinicId);
        
        // Get the clinic to increment completed count
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (clinicOpt.isEmpty()) {
            throw new IllegalArgumentException("Clinic not found");
        }
        Clinic clinic = clinicOpt.get();
        
        // Get current patient
        Optional<QueueUser> currentPatientOpt = queueUserRepository.findByClinicIdAndStatus(clinicId, "in-progress");
        if (currentPatientOpt.isPresent()) {
            QueueUser currentPatient = currentPatientOpt.get();
            // Instead of marking as completed, directly remove the patient to prevent "already in queue" issues
            queueUserRepository.delete(currentPatient);
            logger.info("Completed and removed patient {} from queue", currentPatient.getName());
            
            // Increment completed count
            Integer currentCount = clinic.getCompletedToday() != null ? clinic.getCompletedToday() : 0;
            clinic.setCompletedToday(currentCount + 1);
            clinicRepository.save(clinic);
            logger.info("Incremented completed count to {} for clinic {}", currentCount + 1, clinicId);
        }
        
        // Get next waiting patient
        Optional<QueueUser> nextPatientOpt = queueUserRepository.findFirstByClinicIdAndStatusOrderByPosition(clinicId, "waiting");
        if (nextPatientOpt.isPresent()) {
            QueueUser nextPatient = nextPatientOpt.get();
            nextPatient.setStatus("in-progress");
            nextPatient.setWaitTime(0);
            queueUserRepository.save(nextPatient);
            logger.info("Started appointment for patient {}", nextPatient.getName());
        } else {
            // No more waiting patients - all appointments completed, auto-close queue
            logger.info("No more waiting patients for clinic {}, auto-closing queue", clinicId);
            closeQueue(clinicId, false); // Close without force since no waiting patients
        }
        
        return getQueueStatus(clinicId);
    }

    /**
     * Remove a specific patient from the queue
     */
    public Map<String, Object> removePatientFromQueue(String clinicId, String patientId) {
        logger.info("Removing patient {} from clinic {}", patientId, clinicId);
        
        // Find the patient
        Optional<QueueUser> patientOpt = queueUserRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            throw new IllegalArgumentException("Patient not found in queue");
        }
        
        QueueUser patient = patientOpt.get();
        
        // Verify patient belongs to this clinic
        if (!patient.getClinicId().equals(clinicId)) {
            throw new IllegalArgumentException("Patient does not belong to this clinic");
        }
        
        int removedPosition = patient.getPosition();
        String removedStatus = patient.getStatus();
        
        // Delete the patient
        queueUserRepository.delete(patient);
        logger.info("Removed patient {} (position: {}, status: {}) from clinic {}", 
                   patient.getName(), removedPosition, removedStatus, clinicId);
        
        // If we removed the current patient (in-progress), promote the next waiting patient
        if ("in-progress".equals(removedStatus)) {
            Optional<QueueUser> nextPatientOpt = queueUserRepository.findFirstByClinicIdAndStatusOrderByPosition(clinicId, "waiting");
            if (nextPatientOpt.isPresent()) {
                QueueUser nextPatient = nextPatientOpt.get();
                nextPatient.setStatus("in-progress");
                nextPatient.setWaitTime(0);
                queueUserRepository.save(nextPatient);
                logger.info("Promoted patient {} to in-progress after removal", nextPatient.getName());
            }
        } else {
            // Adjust positions for all patients after the removed one
            List<QueueUser> patientsAfter = queueUserRepository.findByClinicIdOrderByPosition(clinicId)
                .stream()
                .filter(p -> p.getPosition() > removedPosition)
                .toList();
            
            for (QueueUser p : patientsAfter) {
                p.setPosition(p.getPosition() - 1);
                queueUserRepository.save(p);
            }
            
            if (!patientsAfter.isEmpty()) {
                logger.info("Adjusted positions for {} patients after removal", patientsAfter.size());
            }
        }
        
        return Map.of(
            "success", true,
            "message", "Patient removed successfully",
            "removedPatient", patient.getName()
        );
    }
    
    /**
     * Reorder queue by updating patient positions
     */
    public Map<String, Object> reorderQueue(String clinicId, List<String> patientIds) {
        logger.info("Reordering queue for clinic {} with {} patients", clinicId, patientIds.size());
        
        // Validate clinic exists
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (clinicOpt.isEmpty()) {
            throw new IllegalArgumentException("Clinic not found");
        }
        
        // Get all patients in the queue
        List<QueueUser> allPatients = queueUserRepository.findByClinicIdOrderByPosition(clinicId);
        
        // Find current patient (in-progress) - they should always be first
        QueueUser currentPatient = allPatients.stream()
            .filter(p -> "in-progress".equals(p.getStatus()))
            .findFirst()
            .orElse(null);
        
        // Validate that all patient IDs exist and belong to this clinic
        for (String patientId : patientIds) {
            boolean found = allPatients.stream().anyMatch(p -> p.getId().equals(patientId));
            if (!found) {
                throw new IllegalArgumentException("Patient ID " + patientId + " not found in queue");
            }
        }
        
        // If there's a current patient, ensure they're first in the list
        if (currentPatient != null && !patientIds.isEmpty()) {
            if (!patientIds.get(0).equals(currentPatient.getId())) {
                throw new IllegalArgumentException("Current patient (in-progress) must remain first in queue");
            }
        }
        
        // Update positions based on the new order
        for (int i = 0; i < patientIds.size(); i++) {
            String patientId = patientIds.get(i);
            QueueUser patient = allPatients.stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst()
                .orElseThrow();
            
            patient.setPosition(i + 1);
            patient.setWaitTime(i * 15); // 15 min per appointment
            queueUserRepository.save(patient);
        }
        
        logger.info("Successfully reordered queue for clinic {}", clinicId);
        
        return Map.of(
            "success", true,
            "message", "Queue reordered successfully",
            "updatedCount", patientIds.size()
        );
    }

    /**
     * Clean up completed and no-show patients for a specific clinic
     */
    public Map<String, Object> cleanupCompletedPatients(String clinicId) {
        logger.info("Cleaning up completed patients for clinic: {}", clinicId);
        
        List<QueueUser> completedPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "completed");
        List<QueueUser> noShowPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "no-show");
        
        int totalRemoved = completedPatients.size() + noShowPatients.size();
        
        queueUserRepository.deleteAll(completedPatients);
        queueUserRepository.deleteAll(noShowPatients);
        
        logger.info("Removed {} completed and {} no-show patients from clinic {}", 
                   completedPatients.size(), noShowPatients.size(), clinicId);
        
        return Map.of(
            "success", true,
            "message", "Cleanup completed successfully",
            "completedPatientsRemoved", completedPatients.size(),
            "noShowPatientsRemoved", noShowPatients.size(),
            "totalPatientsRemoved", totalRemoved
        );
    }
}