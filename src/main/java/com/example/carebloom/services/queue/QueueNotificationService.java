package com.example.carebloom.services.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueueNotificationService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Notify about queue updates
     */
    public void notifyQueueUpdate(String clinicId, Object queueData) {
        try {
            String topic = "/topic/queue/" + clinicId;
            messagingTemplate.convertAndSend(topic, queueData);
            logger.info("Sent queue update notification to topic: {}", topic);
        } catch (Exception e) {
            logger.error("Error sending queue update notification for clinic: {}", clinicId, e);
        }
    }
    
    /**
     * Notify about user completion
     */
    public void notifyUserCompleted(String clinicId, String userName) {
        try {
            String topic = "/topic/queue/" + clinicId + "/completed";
            messagingTemplate.convertAndSend(topic, userName);
            logger.info("Sent user completed notification to topic: {}", topic);
        } catch (Exception e) {
            logger.error("Error sending user completed notification for clinic: {}", clinicId, e);
        }
    }
    
    /**
     * Notify about queue closure
     */
    public void notifyQueueClosed(String clinicId) {
        try {
            String topic = "/topic/queue/" + clinicId + "/closed";
            messagingTemplate.convertAndSend(topic, "Queue closed");
            logger.info("Sent queue closed notification to topic: {}", topic);
        } catch (Exception e) {
            logger.error("Error sending queue closed notification for clinic: {}", clinicId, e);
        }
    }
}
