package com.example.carebloom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * High-level notification service for sending different types of notifications
 * This is a wrapper around FcmMessagingService for easier usage throughout the app
 */
@Service
public class NotificationDispatchService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatchService.class);
    
    @Autowired
    private FcmMessagingService fcmMessagingService;
    
    /**
     * Send mother acceptance notification
     * Called when MOH office accepts a mother's registration
     */
    public boolean sendMotherAcceptanceNotification(String motherId) {
        logger.info("Sending acceptance notification to mother: {}", motherId);
        return fcmMessagingService.sendMotherAcceptanceNotification(motherId);
    }
    
    /**
     * Send field visit appointment notification
     * Called when a field visit is scheduled
     */
    public boolean sendFieldVisitNotification(String motherId, String visitDate, String visitTime) {
        logger.info("Sending field visit notification to mother: {} for {}", motherId, visitDate);
        return fcmMessagingService.sendFieldVisitNotification(motherId, visitDate, visitTime);
    }
    
    /**
     * Send health tip notification
     * Called when sending educational content
     */
    public boolean sendHealthTip(String motherId, String tipTitle, String tipContent) {
        logger.info("Sending health tip to mother: {}", motherId);
        return fcmMessagingService.sendHealthTipNotification(motherId, tipTitle, tipContent);
    }
    
    /**
     * Send emergency alert to multiple mothers
     * Called for urgent announcements
     */
    public void sendEmergencyAlert(List<String> motherIds, String alertTitle, String alertMessage) {
        logger.info("Sending emergency alert to {} mothers", motherIds.size());
        fcmMessagingService.sendEmergencyAlert(motherIds, alertTitle, alertMessage);
    }
    
    /**
     * Send custom notification to a mother
     * For any other notification needs
     */
    public boolean sendCustomNotification(String motherId, String title, String body, Map<String, String> data) {
        logger.info("Sending custom notification to mother: {}", motherId);
        return fcmMessagingService.sendNotificationToMother(motherId, title, body, data);
    }
    
    /**
     * Send workshop notification
     * Called when a workshop is created or updated
     */
    public boolean sendWorkshopNotification(String motherId, String workshopTitle, String workshopDate, String workshopLocation) {
        String title = "📚 New Workshop Available";
        String body = String.format("%s on %s at %s", workshopTitle, workshopDate, workshopLocation);
        
        Map<String, String> data = Map.of(
            "type", "workshop_notification",
            "action", "navigate_to_workshops",
            "workshopTitle", workshopTitle,
            "workshopDate", workshopDate,
            "workshopLocation", workshopLocation
        );
        
        return fcmMessagingService.sendNotificationToMother(motherId, title, body, data);
    }
    
    /**
     * Send appointment reminder
     * Called before scheduled appointments
     */
    public boolean sendAppointmentReminder(String motherId, String appointmentType, String appointmentDate, String appointmentTime) {
        String title = "⏰ Appointment Reminder";
        String body = String.format("Your %s appointment is tomorrow at %s", appointmentType, appointmentTime);
        
        Map<String, String> data = Map.of(
            "type", "appointment_reminder",
            "action", "navigate_to_appointments",
            "appointmentType", appointmentType,
            "appointmentDate", appointmentDate,
            "appointmentTime", appointmentTime
        );
        
        return fcmMessagingService.sendNotificationToMother(motherId, title, body, data);
    }
}