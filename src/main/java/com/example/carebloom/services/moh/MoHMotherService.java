package com.example.carebloom.services.moh;

import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.services.FcmMessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
public class MoHMotherService {
    private static final Logger logger = LoggerFactory.getLogger(MoHMotherService.class);

    @Autowired
    private MotherRepository motherRepository;
    
    @Autowired
    private FcmMessagingService fcmMessagingService;

    public void acceptMotherRegistration(String UnitId, String motherId) {
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
        
        // Update mother status
        mother.setRegistrationStatus("accepted");
        mother.setUnitId(UnitId); 
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
}
