package com.example.carebloom.services.mothers;

import com.example.carebloom.dto.clinics.ClinicAppointmentDto;
import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.QueueUser;
import com.example.carebloom.repositories.ClinicRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.QueueUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MotherClinicService {

    private static final Logger logger = LoggerFactory.getLogger(MotherClinicService.class);

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private QueueUserRepository queueUserRepository;

    /**
     * Get all clinics where the mother is registered (in addedMothers list)
     */
    public List<ClinicAppointmentDto> getClinicAppointmentsForMother(String firebaseUid) {
        logger.info("Fetching clinic appointments for mother with Firebase UID: {}", firebaseUid);

        // Find the mother by Firebase UID
        Mother mother = motherRepository.findByFirebaseUid(firebaseUid);
        if (mother == null) {
            logger.warn("Mother not found with Firebase UID: {}", firebaseUid);
            return new ArrayList<>();
        }

        String motherId = mother.getId();
        String motherEmail = mother.getEmail();

        logger.info("Found mother: {} with ID: {}", mother.getName(), motherId);

        // Find all clinics where this mother is registered
        // Mother is registered if their ID or email is in the addedMothers list
        List<Clinic> allClinics = clinicRepository.findAll();
        List<ClinicAppointmentDto> clinicAppointments = new ArrayList<>();

        for (Clinic clinic : allClinics) {
            // Check if mother is in this clinic's addedMothers list
            boolean isRegistered = clinic.getAddedMothers().stream()
                    .anyMatch(addedMother -> 
                        addedMother.getId().equals(motherId) || 
                        addedMother.getEmail().equals(motherEmail));

            if (isRegistered) {
                ClinicAppointmentDto dto = mapToClinicAppointmentDto(clinic, motherId);
                clinicAppointments.add(dto);
                logger.info("Mother is registered for clinic: {}", clinic.getTitle());
            }
        }

        logger.info("Found {} clinic appointments for mother", clinicAppointments.size());
        return clinicAppointments;
    }

    /**
     * Get queue status for a specific clinic, including mother's position if in queue
     */
    public Map<String, Object> getQueueStatusForMother(String clinicId, String firebaseUid) {
        logger.info("Fetching queue status for clinic: {} for mother: {}", clinicId, firebaseUid);

        // Verify clinic exists
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (clinicOpt.isEmpty()) {
            throw new IllegalArgumentException("Clinic not found");
        }

        Clinic clinic = clinicOpt.get();

        // Find the mother
        Mother mother = motherRepository.findByFirebaseUid(firebaseUid);
        if (mother == null) {
            throw new IllegalArgumentException("Mother not found");
        }

        String motherId = mother.getId();
        String motherEmail = mother.getEmail();

        // Verify mother is registered for this clinic
        boolean isRegistered = clinic.getAddedMothers().stream()
                .anyMatch(addedMother -> 
                    addedMother.getId().equals(motherId) || 
                    addedMother.getEmail().equals(motherEmail));

        if (!isRegistered) {
            throw new IllegalArgumentException("You are not registered for this clinic");
        }

        // Get queue status
        boolean isQueueActive = "open".equals(clinic.getQueueStatus());
        
        // Find mother's position in queue (using motherId or email as identifier)
        Optional<QueueUser> motherInQueue = queueUserRepository.findByClinicIdAndStatus(clinicId, "in-progress")
                .filter(qu -> qu.getMotherId().equals(motherId) || qu.getMotherId().equals(motherEmail));
        
        if (motherInQueue.isEmpty()) {
            motherInQueue = queueUserRepository.findByClinicIdOrderByPosition(clinicId).stream()
                    .filter(qu -> "waiting".equals(qu.getStatus()))
                    .filter(qu -> qu.getMotherId().equals(motherId) || qu.getMotherId().equals(motherEmail))
                    .findFirst();
        }

        // Count total people in queue
        long totalInQueue = queueUserRepository.countByClinicIdAndStatus(clinicId, "waiting") +
                           queueUserRepository.countByClinicIdAndStatus(clinicId, "in-progress");

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("clinicId", clinicId);
        response.put("clinicTitle", clinic.getTitle());
        response.put("queueActive", isQueueActive);
        response.put("totalInQueue", totalInQueue);
        
        // Add current queue number (patient being served)
        Integer currentQueueNumber = getCurrentQueueNumber(clinicId);
        response.put("currentQueueNumber", currentQueueNumber);

        if (motherInQueue.isPresent()) {
            QueueUser queueUser = motherInQueue.get();
            response.put("inQueue", true);
            response.put("position", queueUser.getPosition());
            response.put("status", queueUser.getStatus());
            response.put("estimatedWaitTime", queueUser.getWaitTime() + " minutes");
            response.put("queueNumber", queueUser.getPosition());
        } else {
            response.put("inQueue", false);
            response.put("message", isQueueActive ? "You are not in the queue yet" : "Queue is not active");
        }

        return response;
    }

    /**
     * Map Clinic to ClinicAppointmentDto with queue information
     */
    private ClinicAppointmentDto mapToClinicAppointmentDto(Clinic clinic, String motherId) {
        ClinicAppointmentDto dto = new ClinicAppointmentDto();
        dto.setId(clinic.getId());
        dto.setDate(clinic.getDate());
        dto.setStartTime(clinic.getStartTime());
        dto.setTitle(clinic.getTitle());
        dto.setDoctorName(clinic.getDoctorName());
        dto.setLocation(clinic.getLocation());

        // Check queue status
        boolean queueActive = "open".equals(clinic.getQueueStatus());
        dto.setQueueActive(queueActive);

        if (queueActive) {
            // Find mother's queue position
            Optional<QueueUser> motherInQueue = queueUserRepository.findByMotherIdAndClinicId(motherId, clinic.getId());
            
            if (motherInQueue.isPresent()) {
                QueueUser queueUser = motherInQueue.get();
                dto.setQueueNumber(queueUser.getPosition());
                dto.setCurrentQueueNumber(getCurrentQueueNumber(clinic.getId()));
                dto.setEstimatedWaitTime(queueUser.getWaitTime() + " min");
            }
        }

        return dto;
    }

    /**
     * Get the current queue number (patient being served)
     */
    private Integer getCurrentQueueNumber(String clinicId) {
        Optional<QueueUser> currentPatient = queueUserRepository.findByClinicIdAndStatus(clinicId, "in-progress");
        return currentPatient.map(QueueUser::getPosition).orElse(null);
    }
}
