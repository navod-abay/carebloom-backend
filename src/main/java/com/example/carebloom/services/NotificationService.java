package com.example.carebloom.services;

import com.example.carebloom.dto.mother.NotificationPreferencesResponse;
import com.example.carebloom.dto.mother.NotificationResponse;
import com.example.carebloom.dto.mother.UpdateNotificationPreferencesRequest;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.MotherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private MotherRepository motherRepository;
    
    /**
     * Update FCM token for a mother
     */
    public NotificationResponse updateFcmToken(String motherId, String fcmToken) {
        try {
            Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found"));
            
            mother.setFcmToken(fcmToken);
            mother.setFcmTokenUpdatedAt(LocalDateTime.now());
            
            motherRepository.save(mother);
            
            logger.info("FCM token updated for mother: {}", motherId);
            
            return NotificationResponse.builder()
                .success(true)
                .message("FCM token updated successfully")
                .build();
                
        } catch (Exception e) {
            logger.error("Error updating FCM token for mother {}: {}", motherId, e.getMessage());
            throw new RuntimeException("Failed to update FCM token", e);
        }
    }
    
    /**
     * Remove FCM token for a mother
     */
    public NotificationResponse removeFcmToken(String motherId, String fcmToken) {
        try {
            Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found"));
            
            // Check if the provided token matches the stored token
            if (mother.getFcmToken() == null) {
                throw new RuntimeException("No FCM token found for user");
            }
            
            if (!fcmToken.equals(mother.getFcmToken())) {
                throw new RuntimeException("FCM token does not match stored token");
            }
            
            mother.setFcmToken(null);
            mother.setFcmTokenUpdatedAt(null);
            
            motherRepository.save(mother);
            
            logger.info("FCM token removed for mother: {}", motherId);
            
            return NotificationResponse.builder()
                .success(true)
                .message("FCM token removed successfully")
                .build();
                
        } catch (Exception e) {
            logger.error("Error removing FCM token for mother {}: {}", motherId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get notification preferences for a mother
     */
    public NotificationPreferencesResponse getNotificationPreferences(String motherId) {
        try {
            Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found"));
            
            // Return defaults if preferences not set
            if (mother.getNotificationPreferences() == null) {
                return NotificationPreferencesResponse.builder()
                    .emergencyAlerts(true)
                    .appUpdates(true)
                    .mohOfficeNotifications(true)
                    .trimesterUpdates(true)
                    .build();
            }
            
            Mother.NotificationPreferences prefs = mother.getNotificationPreferences();
            return NotificationPreferencesResponse.builder()
                .emergencyAlerts(prefs.getEmergencyAlerts())
                .appUpdates(prefs.getAppUpdates())
                .mohOfficeNotifications(prefs.getMohOfficeNotifications())
                .trimesterUpdates(prefs.getTrimesterUpdates())
                .build();
                
        } catch (Exception e) {
            logger.error("Error getting notification preferences for mother {}: {}", motherId, e.getMessage());
            throw new RuntimeException("Failed to get notification preferences", e);
        }
    }
    
    /**
     * Update notification preferences for a mother
     */
    public NotificationResponse updateNotificationPreferences(String motherId, UpdateNotificationPreferencesRequest request) {
        try {
            // Validate that emergency alerts cannot be disabled
            if (!request.getEmergencyAlerts()) {
                throw new IllegalArgumentException("Emergency alerts cannot be disabled");
            }
            
            Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found"));
            
            // Initialize preferences if null
            if (mother.getNotificationPreferences() == null) {
                mother.setNotificationPreferences(new Mother.NotificationPreferences());
            }
            
            Mother.NotificationPreferences prefs = mother.getNotificationPreferences();
            prefs.setEmergencyAlerts(true); // Always true
            prefs.setAppUpdates(request.getAppUpdates());
            prefs.setMohOfficeNotifications(request.getMohOfficeNotifications());
            prefs.setTrimesterUpdates(request.getTrimesterUpdates());
            
            motherRepository.save(mother);
            
            logger.info("Notification preferences updated for mother: {}", motherId);
            
            return NotificationResponse.builder()
                .success(true)
                .message("Notification preferences updated successfully")
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid preference update for mother {}: {}", motherId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating notification preferences for mother {}: {}", motherId, e.getMessage());
            throw new RuntimeException("Failed to update notification preferences", e);
        }
    }
    
    /**
     * Validate FCM token format (basic validation)
     */
    public boolean isValidFcmToken(String fcmToken) {
        return fcmToken != null && 
               fcmToken.trim().length() > 20 && 
               fcmToken.matches("^[a-zA-Z0-9_-]+$");
    }
}