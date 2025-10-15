package com.example.carebloom.services;

import com.example.carebloom.dto.queue.*;
import com.example.carebloom.models.*;
import com.example.carebloom.repositories.*;
import com.example.carebloom.services.queue.QueueSSEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QueueService {
    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);
    
    @Autowired
    private ClinicRepository clinicRepository;
    
    @Autowired
    private QueueUserRepository queueUserRepository;
    
    @Autowired
    private MotherRepository motherRepository;
    
    @Autowired
    private QueueSSEService queueSSEService;

    /**
     * Initialize queue with clinic's registered mothers
     */
    public QueueStatusResponse startQueue(String clinicId) {
        logger.info("Starting queue for clinic: {}", clinicId);
        
        // Validate clinic exists and is active
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        
        if (!clinic.isActive()) {
            throw new IllegalStateException("Cannot start queue for inactive clinic");
        }
        
        // Check if queue is already active
        if ("open".equals(clinic.getQueueStatus())) {
            throw new IllegalStateException("Queue is already active for this clinic");
        }
        
        // Validate clinic date (should be today)
        LocalDate clinicDate = LocalDate.parse(clinic.getDate());
        LocalDate today = LocalDate.now();
        if (!clinicDate.equals(today)) {
            throw new IllegalStateException("Queue can only be started on the clinic date");
        }
        
        // No need to validate mothers - queue can start empty and mothers can be added manually
        logger.info("Starting empty queue for clinic {}. Mothers can be added manually through the queue interface.", clinicId);
        
        // Clear any existing queue data for this clinic
        queueUserRepository.deleteByClinicId(clinicId);
        
        // Initialize queue settings if not present
        if (clinic.getQueueSettings() == null) {
            QueueSettings defaultSettings = new QueueSettings();
            defaultSettings.setOpen(true);
            defaultSettings.setMaxCapacity(clinic.getMaxCapacity() != null ? clinic.getMaxCapacity() : 50);
            defaultSettings.setAvgAppointmentTime(15); // 15 minutes default
            defaultSettings.setClosingTime("17:00");
            defaultSettings.setAutoClose(true);
            clinic.setQueueSettings(defaultSettings);
        } else {
            clinic.getQueueSettings().setOpen(true);
        }
        
        // Start with empty queue - mothers will be added manually through the queue interface
        // This allows for more controlled queue management
        List<QueueUser> queueUsers = new ArrayList<>();
        
        // Don't automatically add mothers - they should be added manually through the queue modal
        logger.info("Queue started for clinic {} - ready to accept patients", clinicId);
        
        // Update clinic status
        clinic.setQueueStatus("open");
        clinicRepository.save(clinic);
        
        // Get queue status response
        QueueStatusResponse response = getQueueStatus(clinicId);
        
        // Broadcast update via SSE
        queueSSEService.broadcastQueueUpdate(clinicId, response);
        
        logger.info("Queue started successfully for clinic: {} with {} patients", clinicId, queueUsers.size());
        return response;
    }

    /**
     * Close the queue
     */
    public Map<String, Object> closeQueue(String clinicId, boolean forceClose) {
        logger.info("Closing queue for clinic: {}", clinicId);
        
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        
        if (!"open".equals(clinic.getQueueStatus())) {
            throw new IllegalStateException("Queue is not currently open");
        }
        
        // Check for waiting patients
        long waitingCount = queueUserRepository.countByClinicIdAndStatus(clinicId, "waiting");
        long inProgressCount = queueUserRepository.countByClinicIdAndStatus(clinicId, "in-progress");
        
        if ((waitingCount > 0 || inProgressCount > 0) && !forceClose) {
            Map<String, Object> confirmationResponse = new HashMap<>();
            confirmationResponse.put("requiresConfirmation", true);
            confirmationResponse.put("waitingPatients", waitingCount);
            confirmationResponse.put("inProgressPatients", inProgressCount);
            confirmationResponse.put("message", "There are still patients in the queue. Do you want to force close?");
            return confirmationResponse;
        }
        
        // Close the queue
        clinic.setQueueStatus("closed");
        if (clinic.getQueueSettings() != null) {
            clinic.getQueueSettings().setOpen(false);
        }
        clinicRepository.save(clinic);
        
        // Update remaining patients to no-show if force closing
        if (forceClose && (waitingCount > 0 || inProgressCount > 0)) {
            List<QueueUser> remainingPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "waiting");
            remainingPatients.addAll(queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "in-progress"));
            
            remainingPatients.forEach(patient -> patient.setStatus("no-show"));
            queueUserRepository.saveAll(remainingPatients);
        }
        
        QueueStatusResponse finalStatus = getQueueStatus(clinicId);
        queueSSEService.broadcastQueueUpdate(clinicId, finalStatus);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Queue closed successfully");
        response.put("finalStatus", finalStatus);
        
        logger.info("Queue closed for clinic: {}", clinicId);
        return response;
    }

    /**
     * Get current queue status
     */
    public QueueStatusResponse getQueueStatus(String clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        
        List<QueueUser> allPatients = queueUserRepository.findByClinicIdOrderByPosition(clinicId);
        
        QueueStatusResponse response = new QueueStatusResponse();
        response.setClinicId(clinicId);
        response.setActive("open".equals(clinic.getQueueStatus()));
        
        // Find current patient (in-progress)
        Optional<QueueUser> currentPatient = allPatients.stream()
            .filter(p -> "in-progress".equals(p.getStatus()))
            .findFirst();
        
        if (currentPatient.isPresent()) {
            QueueStatusResponse.QueuePatientDto currentDto = new QueueStatusResponse.QueuePatientDto();
            currentDto.setName(currentPatient.get().getName());
            currentDto.setEmail(currentPatient.get().getEmail());
            currentDto.setPosition(currentPatient.get().getPosition());
            currentDto.setStatus(currentPatient.get().getStatus());
            response.setCurrentPatient(currentDto);
        }
        
        // Get waiting patients
        List<QueueStatusResponse.QueuePatientDto> waitingQueue = allPatients.stream()
            .filter(p -> "waiting".equals(p.getStatus()))
            .map(this::convertToQueuePatientDto)
            .collect(Collectors.toList());
        response.setWaitingQueue(waitingQueue);
        
        // Calculate stats
        QueueStatusResponse.QueueStatsDto stats = new QueueStatusResponse.QueueStatsDto();
        stats.setTotalPatients((int) allPatients.size());
        stats.setCompleted((int) allPatients.stream().filter(p -> "completed".equals(p.getStatus())).count());
        stats.setWaiting((int) allPatients.stream().filter(p -> "waiting".equals(p.getStatus())).count());
        stats.setInProgress((int) allPatients.stream().filter(p -> "in-progress".equals(p.getStatus())).count());
        response.setStats(stats);
        
        return response;
    }

    /**
     * Process next patient in queue
     */
    public QueueStatusResponse processNextPatient(String clinicId) {
        logger.info("Processing next patient for clinic: {}", clinicId);
        
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        
        if (!"open".equals(clinic.getQueueStatus())) {
            throw new IllegalStateException("Queue is not currently open");
        }
        
        // Find current patient and mark as completed
        Optional<QueueUser> currentPatient = queueUserRepository.findFirstByClinicIdAndStatus(clinicId, "in-progress");
        if (currentPatient.isPresent()) {
            currentPatient.get().setStatus("completed");
            queueUserRepository.save(currentPatient.get());
        }
        
        // Find next waiting patient and mark as in-progress
        List<QueueUser> waitingPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "waiting");
        if (!waitingPatients.isEmpty()) {
            QueueUser nextPatient = waitingPatients.get(0); // First in waiting list
            nextPatient.setStatus("in-progress");
            nextPatient.setWaitTime(0);
            queueUserRepository.save(nextPatient);
            
            // Update wait times for remaining patients
            updateWaitTimes(clinicId);
        } else {
            // No more patients, consider auto-closing if enabled
            if (clinic.getQueueSettings() != null && clinic.getQueueSettings().isAutoClose()) {
                closeQueue(clinicId, false);
            }
        }
        
        QueueStatusResponse response = getQueueStatus(clinicId);
        queueSSEService.broadcastQueueUpdate(clinicId, response);
        
        return response;
    }

    /**
     * Add patient manually to queue
     */
    public QueueStatusResponse addPatientToQueue(String clinicId, QueueUserDto patientDto) {
        logger.info("Adding patient manually to queue for clinic: {}", clinicId);
        
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        
        if (!"open".equals(clinic.getQueueStatus())) {
            throw new IllegalStateException("Queue is not currently open");
        }
        
        // Check if patient already in queue
        if (queueUserRepository.findByMotherIdAndClinicId(patientDto.getMotherId(), clinicId).isPresent()) {
            throw new IllegalStateException("Patient is already in the queue");
        }
        
        // Check capacity
        long currentCount = queueUserRepository.countByClinicId(clinicId);
        if (clinic.getQueueSettings() != null && currentCount >= clinic.getQueueSettings().getMaxCapacity()) {
            throw new IllegalStateException("Queue has reached maximum capacity");
        }
        
        // Find next position
        int nextPosition = queueUserRepository.findByClinicIdOrderByPosition(clinicId).size() + 1;
        
        QueueUser newPatient = new QueueUser();
        newPatient.setName(patientDto.getName());
        newPatient.setEmail(patientDto.getEmail());
        newPatient.setMotherId(patientDto.getMotherId());
        newPatient.setClinicId(clinicId);
        newPatient.setPosition(nextPosition);
        newPatient.setStatus("waiting");
        newPatient.setJoinedTime(LocalDateTime.now().toString());
        newPatient.setNotes(patientDto.getNotes());
        
        // Calculate wait time
        int avgTime = clinic.getQueueSettings() != null ? clinic.getQueueSettings().getAvgAppointmentTime() : 15;
        newPatient.setWaitTime(calculateWaitTime(nextPosition - 1, avgTime));
        newPatient.setEstimatedTime(calculateEstimatedTime(clinic.getStartTime(), newPatient.getWaitTime()));
        
        queueUserRepository.save(newPatient);
        
        QueueStatusResponse response = getQueueStatus(clinicId);
        queueSSEService.broadcastQueueUpdate(clinicId, response);
        
        return response;
    }

    /**
     * Update patient status
     */
    public QueueStatusResponse updatePatientStatus(String clinicId, String patientId, String newStatus) {
        logger.info("Updating patient status for clinic: {}, patient: {}, status: {}", clinicId, patientId, newStatus);
        
        QueueUser patient = queueUserRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found in queue"));
        
        if (!patient.getClinicId().equals(clinicId)) {
            throw new IllegalArgumentException("Patient does not belong to this clinic");
        }
        
        String oldStatus = patient.getStatus();
        patient.setStatus(newStatus);
        queueUserRepository.save(patient);
        
        // If moving from in-progress to completed, process next patient
        if ("in-progress".equals(oldStatus) && "completed".equals(newStatus)) {
            return processNextPatient(clinicId);
        }
        
        QueueStatusResponse response = getQueueStatus(clinicId);
        queueSSEService.broadcastQueueUpdate(clinicId, response);
        
        return response;
    }

    /**
     * Remove patient from queue
     */
    public QueueStatusResponse removePatientFromQueue(String clinicId, String patientId) {
        logger.info("Removing patient from queue for clinic: {}, patient: {}", clinicId, patientId);
        
        QueueUser patient = queueUserRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found in queue"));
        
        if (!patient.getClinicId().equals(clinicId)) {
            throw new IllegalArgumentException("Patient does not belong to this clinic");
        }
        
        int removedPosition = patient.getPosition();
        String removedStatus = patient.getStatus();
        
        queueUserRepository.deleteById(patientId);
        
        // Reorder remaining patients
        List<QueueUser> remainingPatients = queueUserRepository.findByClinicIdOrderByPosition(clinicId);
        for (QueueUser remaining : remainingPatients) {
            if (remaining.getPosition() > removedPosition) {
                remaining.setPosition(remaining.getPosition() - 1);
            }
        }
        queueUserRepository.saveAll(remainingPatients);
        
        // If removed patient was in-progress, move next patient to in-progress
        if ("in-progress".equals(removedStatus)) {
            List<QueueUser> waitingPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "waiting");
            if (!waitingPatients.isEmpty()) {
                QueueUser nextPatient = waitingPatients.get(0);
                nextPatient.setStatus("in-progress");
                nextPatient.setWaitTime(0);
                queueUserRepository.save(nextPatient);
            }
        }
        
        updateWaitTimes(clinicId);
        
        QueueStatusResponse response = getQueueStatus(clinicId);
        queueSSEService.broadcastQueueUpdate(clinicId, response);
        
        return response;
    }

    /**
     * Update queue settings
     */
    public QueueSettings updateQueueSettings(String clinicId, QueueSettingsDto settingsDto) {
        logger.info("Updating queue settings for clinic: {}", clinicId);
        
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        
        QueueSettings settings = clinic.getQueueSettings();
        if (settings == null) {
            settings = new QueueSettings();
        }
        
        settings.setOpen(settingsDto.isOpen());
        settings.setMaxCapacity(settingsDto.getMaxCapacity());
        settings.setAvgAppointmentTime(settingsDto.getAvgAppointmentTime());
        settings.setClosingTime(settingsDto.getClosingTime());
        settings.setAutoClose(settingsDto.isAutoClose());
        
        clinic.setQueueSettings(settings);
        clinicRepository.save(clinic);
        
        // Recalculate wait times with new average time
        updateWaitTimes(clinicId);
        
        QueueStatusResponse response = getQueueStatus(clinicId);
        queueSSEService.broadcastQueueUpdate(clinicId, response);
        
        return settings;
    }

    /**
     * Reorder queue positions
     */
    public QueueStatusResponse reorderQueue(String clinicId, List<String> patientIds) {
        logger.info("Reordering queue for clinic: {}", clinicId);
        
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        
        if (!"open".equals(clinic.getQueueStatus())) {
            throw new IllegalStateException("Queue is not currently open");
        }
        
        List<QueueUser> patients = new ArrayList<>();
        for (String patientId : patientIds) {
            QueueUser patient = queueUserRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
            if (!patient.getClinicId().equals(clinicId)) {
                throw new IllegalArgumentException("Patient does not belong to this clinic: " + patientId);
            }
            patients.add(patient);
        }
        
        // Update positions
        for (int i = 0; i < patients.size(); i++) {
            patients.get(i).setPosition(i + 1);
        }
        
        queueUserRepository.saveAll(patients);
        updateWaitTimes(clinicId);
        
        QueueStatusResponse response = getQueueStatus(clinicId);
        queueSSEService.broadcastQueueUpdate(clinicId, response);
        
        return response;
    }

    // Helper methods
    private int calculateWaitTime(int position, int avgAppointmentTime) {
        return position * avgAppointmentTime;
    }

    private String calculateEstimatedTime(String startTime, int waitTimeMinutes) {
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime estimated = start.plusMinutes(waitTimeMinutes);
            return estimated.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            logger.warn("Error calculating estimated time", e);
            return null;
        }
    }

    private void updateWaitTimes(String clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId).orElse(null);
        if (clinic == null) return;
        
        int avgTime = clinic.getQueueSettings() != null ? clinic.getQueueSettings().getAvgAppointmentTime() : 15;
        List<QueueUser> waitingPatients = queueUserRepository.findByClinicIdAndStatusOrderByPosition(clinicId, "waiting");
        
        for (QueueUser patient : waitingPatients) {
            int waitTime = calculateWaitTime(patient.getPosition() - 1, avgTime);
            patient.setWaitTime(waitTime);
            patient.setEstimatedTime(calculateEstimatedTime(clinic.getStartTime(), waitTime));
        }
        
        queueUserRepository.saveAll(waitingPatients);
    }

    private QueueStatusResponse.QueuePatientDto convertToQueuePatientDto(QueueUser queueUser) {
        QueueStatusResponse.QueuePatientDto dto = new QueueStatusResponse.QueuePatientDto();
        dto.setName(queueUser.getName());
        dto.setEmail(queueUser.getEmail());
        dto.setPosition(queueUser.getPosition());
        dto.setStatus(queueUser.getStatus());
        dto.setEstimatedWaitTime(queueUser.getWaitTime());
        return dto;
    }
}
