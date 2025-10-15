package com.example.carebloom.services.moh;

import com.example.carebloom.dto.moh.AcceptMotherRequest;
import com.example.carebloom.dto.moh.UpdateHealthDetailsRequest;
import com.example.carebloom.models.HealthDetails;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.HealthDetailsRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.services.FcmMessagingService;
import com.example.carebloom.repositories.VisitRecordRepository;
import com.example.carebloom.models.VisitRecord;
import com.example.carebloom.dto.moh.CreateVitalRecordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class MoHMotherService {
    private static final Logger logger = LoggerFactory.getLogger(MoHMotherService.class);

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private HealthDetailsRepository healthDetailsRepository;
    
    @Autowired
    private FcmMessagingService fcmMessagingService;

    @Autowired
    private VisitRecordRepository visitRecordRepository;

    public void acceptMotherRegistration(AcceptMotherRequest request, String motherId) {
        Optional<Mother> motherOpt = motherRepository.findById(motherId);
        if (motherOpt.isEmpty()) {
            logger.error("Mother not found with ID: {}", motherId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found");
        }
        Mother mother = motherOpt.get();
        if (!"complete".equalsIgnoreCase(mother.getRegistrationStatus())) {
            logger.error("Mother {} registration status is not 'completed': {}", motherId, mother.getRegistrationStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mother registration status must be 'completed' to accept");
        }

        // Save health details
        AcceptMotherRequest.HealthDetailsRequest healthDetailsRequest = request.getHealthDetails();
        HealthDetails healthDetails = HealthDetails.builder()
                .motherId(motherId)
                .age(healthDetailsRequest.getAge())
                .bloodType(healthDetailsRequest.getBloodType())
                .allergies(healthDetailsRequest.getAllergies())
                .emergencyContactName(healthDetailsRequest.getEmergencyContactName())
                .emergencyContactPhone(healthDetailsRequest.getEmergencyContactPhone())
                .pregnancyType(healthDetailsRequest.getPregnancyType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        healthDetailsRepository.save(healthDetails);
        logger.info("Health details saved for mother {}", motherId);
        
        // Update mother status
        mother.setRegistrationStatus("accepted");
        mother.setUnitId(request.getUnitId());
        motherRepository.save(mother);
        logger.info("Mother {} registration accepted", motherId);
        
        // Send notification to the mother
        try {
            boolean notificationSent = fcmMessagingService.sendMotherAcceptanceNotification(motherId);
            if (notificationSent) {
                logger.info("Acceptance notification sent successfully to mother: {}", motherId);
            } else {
                logger.warn("Failed to send acceptance notification to mother: {} (no FCM token or notification failed)", motherId);
            }
        } catch (Exception e) {
            logger.error("Error sending acceptance notification to mother {}: {}", motherId, e.getMessage());
            // Don't throw exception - acceptance should still succeed even if notification fails
        }
    }

    public HealthDetails updateHealthDetails(String motherId, UpdateHealthDetailsRequest request) {
        HealthDetails healthDetails = healthDetailsRepository.findByMotherId(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Health details not found for mother"));

        healthDetails.setAge(request.getAge());
        healthDetails.setBloodType(request.getBloodType());
        healthDetails.setAllergies(request.getAllergies());
        healthDetails.setEmergencyContactName(request.getEmergencyContactName());
        healthDetails.setEmergencyContactPhone(request.getEmergencyContactPhone());
        healthDetails.setPregnancyType(request.getPregnancyType());
        healthDetails.setUpdatedAt(LocalDateTime.now());

        HealthDetails updatedHealthDetails = healthDetailsRepository.save(healthDetails);
        logger.info("Health details updated for mother {}", motherId);
        return updatedHealthDetails;
    }

    public VisitRecord createVitalRecord(String motherId, CreateVitalRecordRequest request) {
        VisitRecord visitRecord = VisitRecord.builder()
                .motherId(motherId)
                .visitType(VisitRecord.VisitType.OTHER)
                .visitDate(LocalDate.now())
                .gestationalWeek(request.getGestationalWeek())
                .weight(request.getWeight())
                .bloodPressure(request.getBloodPressure())
                .glucoseLevel(request.getGlucoseLevel())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        VisitRecord savedRecord = visitRecordRepository.save(visitRecord);
        logger.info("New vital record created for mother {}", motherId);
        return savedRecord;
    }

    public List<VisitRecord> getVitalRecords(String motherId) {
        return visitRecordRepository.findByMotherId(motherId);
    }
}
