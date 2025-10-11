package com.example.carebloom.services;

import com.google.firebase.messaging.*;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.MotherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FcmMessagingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FcmMessagingService.class);
    
    @Autowired
    private MotherRepository motherRepository;
    
    /**
     * Send notification to a specific mother by ID
     */
    public boolean sendNotificationToMother(String motherId, String title, String body, Map<String, String> data) {
        try {
            Mother mother = motherRepository.findById(motherId).orElse(null);
            if (mother == null) {
                logger.warn("Mother not found with ID: {}", motherId);
                return false;
            }
            
            if (mother.getFcmToken() == null || mother.getFcmToken().isEmpty()) {
                logger.warn("No FCM token found for mother: {}", motherId);
                return false;
            }
            
            return sendNotification(mother.getFcmToken(), title, body, data);
            
        } catch (Exception e) {
            logger.error("Error sending notification to mother {}: {}", motherId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Send notification to multiple mothers
     */
    public void sendNotificationToMultipleMothers(List<String> motherIds, String title, String body, Map<String, String> data) {
        List<String> fcmTokens = new ArrayList<>();
        
        for (String motherId : motherIds) {
            Mother mother = motherRepository.findById(motherId).orElse(null);
            if (mother != null && mother.getFcmToken() != null && !mother.getFcmToken().isEmpty()) {
                fcmTokens.add(mother.getFcmToken());
            }
        }
        
        if (!fcmTokens.isEmpty()) {
            sendMulticastNotification(fcmTokens, title, body, data);
        } else {
            logger.warn("No valid FCM tokens found for the provided mother IDs");
        }
    }
    
    /**
     * Send notification using FCM token directly
     */
    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            // Build the notification
            Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
            
            // Build the message
            Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .setAndroidConfig(AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                        .setIcon("ic_notification")
                        .setColor("#FF6B35") // CareBloom theme color
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .build())
                    .build());
            
            // Add custom data if provided
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            Message message = messageBuilder.build();
            
            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent notification to token {}: {}", 
                       fcmToken.substring(0, Math.min(10, fcmToken.length())) + "...", response);
            
            return true;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending FCM notification to token {}: {}", 
                        fcmToken.substring(0, Math.min(10, fcmToken.length())) + "...", e.getMessage());
            
            // Handle specific error cases
            if ("UNREGISTERED".equals(e.getErrorCode().name())) {
                logger.warn("FCM token is invalid or unregistered: {}", fcmToken);
                // You might want to remove this token from the database
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending notification: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send notification to multiple tokens (multicast)
     */
    public void sendMulticastNotification(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        try {
            // Build the notification
            Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
            
            // Build the multicast message
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
                .setNotification(notification)
                .setAndroidConfig(AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                        .setIcon("ic_notification")
                        .setColor("#FF6B35") // CareBloom theme color
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .build())
                    .build());
            
            // Add custom data if provided
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            MulticastMessage message = messageBuilder.build();
            
            // Send the message
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            
            logger.info("Successfully sent {} notifications, {} failed", 
                       response.getSuccessCount(), response.getFailureCount());
            
            // Log any failures
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        logger.error("Failed to send notification to token {}: {}", 
                                   fcmTokens.get(i).substring(0, Math.min(10, fcmTokens.get(i).length())) + "...",
                                   responses.get(i).getException().getMessage());
                    }
                }
            }
            
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending multicast FCM notification: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending multicast notification: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification when mother gets accepted by MOH office
     */
    public boolean sendMotherAcceptanceNotification(String motherId) {
        String title = "Registration Approved! 🎉";
        String body = "Your registration has been approved by the MOH office. Welcome to CareBloom!";
        
        Map<String, String> data = Map.of(
            "type", "mother_accepted",
            "action", "navigate_to_dashboard",
            "motherId", motherId
        );
        
        return sendNotificationToMother(motherId, title, body, data);
    }
    
    /**
     * Send notification for field visit appointment
     */
    public boolean sendFieldVisitNotification(String motherId, String visitDate, String visitTime) {
        String title = "Field Visit Scheduled 📅";
        String body = String.format("Your field visit is scheduled for %s at %s", visitDate, visitTime);
        
        Map<String, String> data = Map.of(
            "type", "field_visit_scheduled",
            "action", "navigate_to_appointments",
            "visitDate", visitDate,
            "visitTime", visitTime
        );
        
        return sendNotificationToMother(motherId, title, body, data);
    }
    
    /**
     * Send health tip notification
     */
    public boolean sendHealthTipNotification(String motherId, String tipTitle, String tipContent) {
        String title = "💡 Health Tip";
        String body = tipTitle;
        
        Map<String, String> data = Map.of(
            "type", "health_tip",
            "action", "navigate_to_health_tips",
            "tipTitle", tipTitle,
            "tipContent", tipContent
        );
        
        return sendNotificationToMother(motherId, title, body, data);
    }
    
    /**
     * Send emergency alert notification
     */
    public void sendEmergencyAlert(List<String> motherIds, String alertTitle, String alertMessage) {
        String title = "🚨 Emergency Alert";
        String body = alertTitle;
        
        Map<String, String> data = Map.of(
            "type", "emergency_alert",
            "action", "show_alert_dialog",
            "alertTitle", alertTitle,
            "alertMessage", alertMessage,
            "priority", "high"
        );
        
        sendNotificationToMultipleMothers(motherIds, title, body, data);
    }
}