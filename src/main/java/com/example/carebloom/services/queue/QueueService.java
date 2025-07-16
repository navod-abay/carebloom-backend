package com.example.carebloom.services.queue;

import com.example.carebloom.dto.queue.*;
import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.ClinicQueue;
import com.example.carebloom.models.QueueEntry;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.ClinicRepository;
import com.example.carebloom.repositories.ClinicQueueRepository;
import com.example.carebloom.repositories.QueueEntryRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QueueService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);
    
    @Autowired
    private ClinicQueueRepository clinicQueueRepository;
    
    @Autowired
    private QueueEntryRepository queueEntryRepository;
    
    @Autowired
    private ClinicRepository clinicRepository;
    
    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;
    
    @Autowired
    private QueueNotificationService queueNotificationService;
    
    /**
     * Start a new queue for a clinic
     */
    @Transactional
    public QueueResponse startQueue(String clinicId, StartQueueRequest request) {
        try {
            // Verify clinic exists and user has access
            Clinic clinic = getClinicAndVerifyAccess(clinicId);
            
            // Check if queue already exists
            Optional<ClinicQueue> existingQueue = clinicQueueRepository.findByClinicId(clinicId);
            if (existingQueue.isPresent() && "active".equals(existingQueue.get().getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Queue already active for this clinic");
            }
            
            // Create new queue
            ClinicQueue queue = new ClinicQueue();
            queue.setClinicId(clinicId);
            queue.setMohOfficeId(clinic.getMohOfficeId());
            queue.setStatus("active");
            queue.setMaxCapacity(request.getMaxCapacity());
            queue.setAvgAppointmentTime(request.getAvgAppointmentTime());
            queue.setCompletedAppointments(0);
            queue.setCreatedAt(LocalDateTime.now());
            queue.setUpdatedAt(LocalDateTime.now());
            
            ClinicQueue savedQueue = clinicQueueRepository.save(queue);
            
            // Notify WebSocket clients
            queueNotificationService.notifyQueueUpdate(clinicId, savedQueue);
            
            logger.info("Started queue for clinic: {}", clinicId);
            return new QueueResponse(true, "Queue started successfully", savedQueue);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error starting queue for clinic: {}", clinicId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start queue");
        }
    }
    
    /**
     * Add users to queue
     */
    @Transactional
    public QueueResponse addUsersToQueue(String clinicId, AddUsersToQueueRequest request) {
        try {
            // Verify clinic and get queue
            ClinicQueue queue = getActiveQueue(clinicId);
            
            // Get current queue length
            long currentQueueLength = queueEntryRepository.countByQueueIdAndStatus(queue.getId(), "waiting");
            
            // Check capacity
            if (currentQueueLength + request.getUsers().size() > queue.getMaxCapacity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Queue capacity exceeded");
            }
            
            // Add users to queue
            int startPosition = (int) currentQueueLength + 1;
            
            for (int i = 0; i < request.getUsers().size(); i++) {
                AddUsersToQueueRequest.QueueUserRequest userRequest = request.getUsers().get(i);
                
                // Check if user already in queue
                Optional<QueueEntry> existingEntry = queueEntryRepository.findByQueueIdAndEmail(queue.getId(), userRequest.getEmail());
                if (existingEntry.isPresent()) {
                    continue; // Skip duplicate user
                }
                
                QueueEntry entry = new QueueEntry();
                entry.setQueueId(queue.getId());
                entry.setClinicId(clinicId);
                entry.setName(userRequest.getName());
                entry.setEmail(userRequest.getEmail());
                entry.setQueuePosition(startPosition + i);
                entry.setStatus("waiting");
                entry.setJoinedAt(LocalDateTime.now());
                entry.setEstimatedWaitTime(calculateWaitTime(startPosition + i - 1, queue.getAvgAppointmentTime()));
                
                queueEntryRepository.save(entry);
            }
            
            // Update queue timestamp
            queue.setUpdatedAt(LocalDateTime.now());
            clinicQueueRepository.save(queue);
            
            // Notify WebSocket clients about queue update
            QueueResponse statusResponse = getQueueStatus(clinicId);
            queueNotificationService.notifyQueueUpdate(clinicId, statusResponse.getData());
            
            logger.info("Added {} users to queue for clinic: {}", request.getUsers().size(), clinicId);
            return new QueueResponse(true, "Users added to queue successfully");
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding users to queue for clinic: {}", clinicId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add users to queue");
        }
    }
    
    /**
     * Complete current appointment
     */
    @Transactional
    public QueueResponse completeAppointment(String clinicId) {
        try {
            ClinicQueue queue = getActiveQueue(clinicId);
            
            // Find current user (position 1 with waiting or current status)
            Optional<QueueEntry> currentEntry = queueEntryRepository.findTopByQueueIdAndStatusOrderByQueuePosition(queue.getId(), "waiting");
            
            if (!currentEntry.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No users in queue to complete");
            }
            
            QueueEntry entry = currentEntry.get();
            entry.setStatus("completed");
            entry.setCompletedAt(LocalDateTime.now());
            queueEntryRepository.save(entry);
            
            // Update positions for remaining users
            List<QueueEntry> remainingEntries = queueEntryRepository.findByQueueIdAndStatus(queue.getId(), "waiting");
            for (QueueEntry remainingEntry : remainingEntries) {
                if (remainingEntry.getQueuePosition() > entry.getQueuePosition()) {
                    remainingEntry.setQueuePosition(remainingEntry.getQueuePosition() - 1);
                    remainingEntry.setEstimatedWaitTime(calculateWaitTime(remainingEntry.getQueuePosition() - 1, queue.getAvgAppointmentTime()));
                    queueEntryRepository.save(remainingEntry);
                }
            }
            
            // Update queue completed count
            queue.setCompletedAppointments(queue.getCompletedAppointments() + 1);
            queue.setUpdatedAt(LocalDateTime.now());
            clinicQueueRepository.save(queue);
            
            // Notify WebSocket clients
            queueNotificationService.notifyUserCompleted(clinicId, entry.getName());
            QueueResponse statusResponse = getQueueStatus(clinicId);
            queueNotificationService.notifyQueueUpdate(clinicId, statusResponse.getData());
            
            logger.info("Completed appointment for user: {} in clinic: {}", entry.getName(), clinicId);
            return new QueueResponse(true, "Appointment completed successfully");
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error completing appointment for clinic: {}", clinicId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to complete appointment");
        }
    }
    
    /**
     * Close queue
     */
    @Transactional
    public QueueResponse closeQueue(String clinicId) {
        try {
            ClinicQueue queue = getActiveQueue(clinicId);
            
            queue.setStatus("closed");
            queue.setClosedAt(LocalDateTime.now());
            queue.setUpdatedAt(LocalDateTime.now());
            clinicQueueRepository.save(queue);
            
            logger.info("Closed queue for clinic: {}", clinicId);
            return new QueueResponse(true, "Queue closed successfully");
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error closing queue for clinic: {}", clinicId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to close queue");
        }
    }
    
    /**
     * Get queue status
     */
    public QueueResponse getQueueStatus(String clinicId) {
        try {
            ClinicQueue queue = getActiveQueue(clinicId);
            
            // Get all queue entries
            List<QueueEntry> entries = queueEntryRepository.findByQueueIdOrderByQueuePosition(queue.getId());
            List<QueueEntry> waitingEntries = entries.stream()
                .filter(entry -> "waiting".equals(entry.getStatus()))
                .collect(Collectors.toList());
            
            // Build response
            QueueStatusDto statusDto = new QueueStatusDto();
            statusDto.setId(queue.getId());
            statusDto.setClinicId(queue.getClinicId());
            statusDto.setStatus(queue.getStatus());
            statusDto.setCompletedCount(queue.getCompletedAppointments());
            
            // Settings
            QueueStatusDto.QueueSettingsDto settings = new QueueStatusDto.QueueSettingsDto();
            settings.setMaxCapacity(queue.getMaxCapacity());
            settings.setAvgAppointmentTime(queue.getAvgAppointmentTime());
            statusDto.setSettings(settings);
            
            // Current user (first in waiting list)
            if (!waitingEntries.isEmpty()) {
                QueueEntry currentEntry = waitingEntries.get(0);
                statusDto.setCurrentUser(mapToQueueUserDto(currentEntry));
                
                // Waiting users (excluding current)
                List<QueueUserDto> waitingUsers = waitingEntries.stream()
                    .skip(1)
                    .map(this::mapToQueueUserDto)
                    .collect(Collectors.toList());
                statusDto.setWaitingUsers(waitingUsers);
            }
            
            // Statistics
            QueueStatusDto.QueueStatisticsDto statistics = new QueueStatusDto.QueueStatisticsDto();
            statistics.setQueueLength(waitingEntries.size());
            statistics.setTotalWaitTime(calculateTotalWaitTime(waitingEntries, queue.getAvgAppointmentTime()));
            statistics.setAvgWaitTime(waitingEntries.isEmpty() ? 0 : statistics.getTotalWaitTime() / waitingEntries.size());
            statusDto.setStatistics(statistics);
            
            return new QueueResponse(true, "Queue retrieved successfully", statusDto);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting queue status for clinic: {}", clinicId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get queue status");
        }
    }
    
    /**
     * Helper methods
     */
    private Clinic getClinicAndVerifyAccess(String clinicId) {
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
        if (!clinicOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found");
        }
        
        Clinic clinic = clinicOpt.get();
        String userMohOfficeId = getCurrentUserMohOfficeId();
        
        if (!clinic.getMohOfficeId().equals(userMohOfficeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to clinic");
        }
        
        return clinic;
    }
    
    private ClinicQueue getActiveQueue(String clinicId) {
        getClinicAndVerifyAccess(clinicId); // Verify access first
        
        Optional<ClinicQueue> queueOpt = clinicQueueRepository.findByClinicId(clinicId);
        if (!queueOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Queue not found for clinic");
        }
        
        ClinicQueue queue = queueOpt.get();
        if (!"active".equals(queue.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Queue is not active");
        }
        
        return queue;
    }
    
    private String getCurrentUserMohOfficeId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
            }
            
            String firebaseUid = authentication.getName();
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
            
            if (mohUser == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
            }
            
            return mohUser.getOfficeId();
        } catch (Exception e) {
            logger.error("Error getting current user's MoH office ID", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to authenticate user");
        }
    }
    
    private int calculateWaitTime(int position, int avgAppointmentTime) {
        return position * avgAppointmentTime;
    }
    
    private int calculateTotalWaitTime(List<QueueEntry> entries, int avgAppointmentTime) {
        return entries.stream()
            .mapToInt(entry -> (entry.getQueuePosition() - 1) * avgAppointmentTime)
            .sum();
    }
    
    private QueueUserDto mapToQueueUserDto(QueueEntry entry) {
        QueueUserDto dto = new QueueUserDto();
        dto.setName(entry.getName());
        dto.setEmail(entry.getEmail());
        dto.setWaitTime(entry.getEstimatedWaitTime());
        dto.setJoinedTime(entry.getJoinedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setQueuePosition(entry.getQueuePosition());
        dto.setStatus(entry.getStatus());
        return dto;
    }
}
