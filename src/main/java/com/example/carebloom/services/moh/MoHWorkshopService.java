package com.example.carebloom.services.moh;

import com.example.carebloom.models.Workshop;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.AddedMother;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.WorkshopRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.dto.CreateWorkshopRequest;
import com.example.carebloom.dto.CreateWorkshopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MoHWorkshopService {
    private static final Logger logger = LoggerFactory.getLogger(MoHWorkshopService.class);

    @Autowired
    private WorkshopRepository workshopRepository;

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    @Autowired
    private MotherRepository motherRepository;

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
    public CreateWorkshopResponse createWorkshop(CreateWorkshopRequest request, String userEmail) {
        try {
            String userId = getUserIdForUser(userEmail);
            
            if (userId == null) {
                throw new SecurityException("User not found or not authorized");
            }

            // Get MOH office ID from user
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(userEmail);
            if (mohUser == null || mohUser.getOfficeId() == null) {
                return new CreateWorkshopResponse(false, "Failed to determine MoH office for current user");
            }
            String mohOfficeId = mohUser.getOfficeId();

            Workshop workshop = new Workshop();
            workshop.setTitle(request.getTitle());
            workshop.setDate(request.getDate());
            workshop.setTime(request.getTime());
            workshop.setVenue(request.getVenue());
            workshop.setDescription(request.getDescription());
            workshop.setCategory(request.getCategory());
            workshop.setCapacity(request.getCapacity());
            workshop.setRegisteredMotherIds(request.getRegisteredMotherIds() != null ? new ArrayList<>(request.getRegisteredMotherIds()) : new ArrayList<>());
            workshop.setUnitIds(request.getUnitIds() != null ? new ArrayList<>(request.getUnitIds()) : new ArrayList<>());
            workshop.setUserId(userId);
            workshop.setMohOfficeId(mohOfficeId);
            workshop.setCreatedAt(LocalDateTime.now());
            workshop.setUpdatedAt(LocalDateTime.now());
            workshop.setActive(true);
            
            // Initialize addedMothers to empty list
            workshop.setAddedMothers(new ArrayList<>());
            
            // Populate addedMothers array with full mother objects (same as clinic)
            if (request.getRegisteredMotherIds() != null && !request.getRegisteredMotherIds().isEmpty()) {
                logger.info("Processing {} mother IDs for workshop", request.getRegisteredMotherIds().size());
                List<Mother> mothers = motherRepository.findAllById(request.getRegisteredMotherIds());
                logger.info("Found {} mothers in database", mothers.size());
                List<AddedMother> addedMothers = new ArrayList<>();
                
                for (Mother mother : mothers) {
                    logger.info("Checking mother: {} with mohOfficeId: {} against workshop mohOfficeId: {}", 
                        mother.getName(), mother.getMohOfficeId(), mohOfficeId);
                    // Only add mothers from the same MOH office
                    if (mohOfficeId != null && mohOfficeId.equals(mother.getMohOfficeId())) {
                        AddedMother am = new AddedMother();
                        am.setId(mother.getId());
                        am.setName(mother.getName());
                        am.setEmail(mother.getEmail());
                        am.setPhone(mother.getPhone());
                        am.setDueDate(mother.getDueDate());
                        am.setAge(25); // Default age
                        am.setRecordNumber(mother.getRecordNumber());
                        addedMothers.add(am);
                        logger.info("Added mother: {}", mother.getName());
                    } else {
                        logger.warn("Skipped mother {} - mohOfficeId mismatch", mother.getName());
                    }
                }
                
                workshop.setAddedMothers(addedMothers);
                workshop.setEnrolled(addedMothers.size());
                logger.info("Added {} mothers to workshop with record numbers populated", addedMothers.size());
            } else {
                logger.info("No mother IDs provided for workshop");
            }

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

    /**
     * Add mothers to a workshop
     */
    public Workshop addMothersToWorkshop(String workshopId, List<String> motherIds, String userEmail) {
        try {
            String userId = getUserIdForUser(userEmail);
            if (userId == null) {
                throw new SecurityException("User not found or not authorized");
            }

            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);
            if (workshopOpt.isEmpty() || !workshopOpt.get().getUserId().equals(userId)) {
                logger.warn("Workshop not found or access denied: {}", workshopId);
                return null;
            }

            Workshop workshop = workshopOpt.get();
            List<Mother> mothers = motherRepository.findAllById(motherIds);

            // Get the MOH office ID from the workshop
            String mohOfficeId = workshop.getMohOfficeId();

            // Filter mothers to only include those from the same MOH office
            List<Mother> validMothers = mothers.stream()
                    .filter(m -> mohOfficeId.equals(m.getMohOfficeId()))
                    .collect(Collectors.toList());

            if (workshop.getAddedMothers() == null) {
                workshop.setAddedMothers(new ArrayList<>());
            }

            for (Mother mother : validMothers) {
                // Check if mother is already added
                boolean alreadyAdded = workshop.getAddedMothers().stream()
                        .anyMatch(am -> am.getId().equals(mother.getId()));

                if (!alreadyAdded) {
                    AddedMother am = new AddedMother();
                    am.setId(mother.getId());
                    am.setName(mother.getName());
                    am.setEmail(mother.getEmail());
                    am.setPhone(mother.getPhone());
                    am.setDueDate(mother.getDueDate());
                    am.setAge(25); // Default age, could be calculated from DOB if available
                    am.setRecordNumber(mother.getRecordNumber());
                    workshop.getAddedMothers().add(am);
                    
                    // Update enrollment count
                    workshop.setEnrolled(workshop.getAddedMothers().size());
                }
            }

            workshop.setUpdatedAt(LocalDateTime.now());
            Workshop savedWorkshop = workshopRepository.save(workshop);
            logger.info("Added {} mothers to workshop: {}", validMothers.size(), workshopId);
            return savedWorkshop;

        } catch (Exception e) {
            logger.error("Error adding mothers to workshop", e);
            return null;
        }
    }

    /**
     * Remove a mother from a workshop
     */
    public Workshop removeMotherFromWorkshop(String workshopId, String motherId, String userEmail) {
        try {
            String userId = getUserIdForUser(userEmail);
            if (userId == null) {
                throw new SecurityException("User not found or not authorized");
            }

            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);
            if (workshopOpt.isEmpty() || !workshopOpt.get().getUserId().equals(userId)) {
                logger.warn("Workshop not found or access denied: {}", workshopId);
                return null;
            }

            Workshop workshop = workshopOpt.get();

            if (workshop.getAddedMothers() != null) {
                workshop.getAddedMothers().removeIf(am -> am.getId().equals(motherId));
                workshop.setEnrolled(workshop.getAddedMothers().size());
                workshop.setUpdatedAt(LocalDateTime.now());
                Workshop savedWorkshop = workshopRepository.save(workshop);
                logger.info("Removed mother {} from workshop: {}", motherId, workshopId);
                return savedWorkshop;
            }

            return workshop;

        } catch (Exception e) {
            logger.error("Error removing mother from workshop", e);
            return null;
        }
    }
}
