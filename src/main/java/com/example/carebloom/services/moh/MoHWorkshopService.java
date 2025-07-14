package com.example.carebloom.services.moh;

import com.example.carebloom.models.Workshop;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.WorkshopRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.dto.CreateWorkshopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MoHWorkshopService {
    private static final Logger logger = LoggerFactory.getLogger(MoHWorkshopService.class);

    @Autowired
    private WorkshopRepository workshopRepository;

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    /**
     * Get all workshops for a specific user
     */
    public List<Workshop> getAllWorkshopsForUser(String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }
        return workshopRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Get active workshops for a specific user
     */
    public List<Workshop> getActiveWorkshopsForUser(String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }
        return workshopRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Get upcoming workshops for a specific user
     */
    public List<Workshop> getUpcomingWorkshopsForUser(String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }
        // For now, just return all active workshops since we're using string dates
        return workshopRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Get workshops by date for a specific user
     */
    public List<Workshop> getWorkshopsByDate(String date, String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }
        return workshopRepository.findByDateAndUserIdAndIsActiveTrue(date, userId);
    }

    /**
     * Get workshops by category for a specific user
     */
    public List<Workshop> getWorkshopsByCategory(String category, String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }
        return workshopRepository.findByCategoryAndUserIdAndIsActiveTrue(category, userId);
    }

    /**
     * Get workshops by venue for a specific user
     */
    public List<Workshop> getWorkshopsByVenue(String venue, String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }
        return workshopRepository.findByVenueAndUserIdAndIsActiveTrue(venue, userId);
    }

    /**
     * Get a workshop by ID, ensuring it belongs to the user
     */
    public Optional<Workshop> getWorkshopById(String id, String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }

        Optional<Workshop> workshopOpt = workshopRepository.findById(id);
        if (workshopOpt.isPresent() && workshopOpt.get().getUserId().equals(userId)) {
            return workshopOpt;
        }
        return Optional.empty();
    }

    /**
     * Create a new workshop for a specific user
     */
    public CreateWorkshopResponse createWorkshop(Workshop workshop, String userEmail) {
        try {
            String userId = getUserIdForUser(userEmail);
            
            if (userId == null) {
                throw new SecurityException("User not found or not authorized");
            }

            workshop.setUserId(userId);
            workshop.setCreatedAt(LocalDateTime.now());
            workshop.setUpdatedAt(LocalDateTime.now());
            workshop.setActive(true);

            Workshop savedWorkshop = workshopRepository.save(workshop);
            logger.info("Saved workshop: {}", savedWorkshop);
            return new CreateWorkshopResponse(true, "Workshop created successfully", savedWorkshop);
        } catch (SecurityException e) {
            logger.error("Security error creating workshop", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error creating workshop", e);
            return new CreateWorkshopResponse(false, "Failed to create workshop: " + e.getMessage());
        }
    }

    /**
     * Update a workshop, ensuring it belongs to the user
     */
    public Optional<Workshop> updateWorkshop(String id, Workshop workshop, String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }

        Optional<Workshop> existingWorkshopOpt = workshopRepository.findById(id);
        if (!existingWorkshopOpt.isPresent()) {
            return Optional.empty();
        }

        Workshop existingWorkshop = existingWorkshopOpt.get();
        // Verify that the workshop belongs to this user
        if (!existingWorkshop.getUserId().equals(userId)) {
            logger.warn("User attempted to update workshop from another user: {}", id);
            throw new SecurityException("Access denied: Workshop belongs to different user");
        }

        existingWorkshop.setTitle(workshop.getTitle());
        existingWorkshop.setDate(workshop.getDate());
        existingWorkshop.setTime(workshop.getTime());
        existingWorkshop.setVenue(workshop.getVenue());
        existingWorkshop.setDescription(workshop.getDescription());
        existingWorkshop.setCategory(workshop.getCategory());
        existingWorkshop.setUpdatedAt(LocalDateTime.now());

        Workshop savedWorkshop = workshopRepository.save(existingWorkshop);
        return Optional.of(savedWorkshop);
    }

    /**
     * Delete (hard delete) a workshop, ensuring it belongs to the user
     */
    public boolean deleteWorkshop(String id, String userEmail) {
        String userId = getUserIdForUser(userEmail);
        if (userId == null) {
            throw new SecurityException("User not found or not authorized");
        }

        Optional<Workshop> workshopOpt = workshopRepository.findById(id);
        if (!workshopOpt.isPresent()) {
            return false;
        }

        Workshop workshop = workshopOpt.get();
        // Verify that the workshop belongs to this user
        if (!workshop.getUserId().equals(userId)) {
            logger.warn("User attempted to delete workshop from another user: {}", id);
            throw new SecurityException("Access denied: Workshop belongs to different user");
        }

        // Hard delete - completely remove from database
        workshopRepository.delete(workshop);
        logger.info("Workshop with ID {} has been permanently deleted from database", id);
        return true;
    }

    /**
     * Helper method to get user ID for a specific user
     */
    private String getUserIdForUser(String userEmail) {
        try {
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(userEmail);
            if (mohUser == null) {
                logger.error("No MoH user found for email: {}", userEmail);
                return null;
            }
            return mohUser.getId(); // Assuming MoHOfficeUser has getId() method
        } catch (Exception e) {
            logger.error("Error getting user ID for user: {}", userEmail, e);
            return null;
        }
    }
}
