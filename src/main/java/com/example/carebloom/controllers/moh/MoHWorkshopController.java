package com.example.carebloom.controllers.moh;

import com.example.carebloom.dto.CreateWorkshopResponse;
import com.example.carebloom.models.Workshop;
import com.example.carebloom.services.moh.MoHWorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/moh")
public class MoHWorkshopController {

    @Autowired
    private MoHWorkshopService workshopService;

    /**
     * Create a new workshop
     */
    @PostMapping("/workshops")
    public ResponseEntity<?> createWorkshop(@RequestBody Workshop workshop, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            CreateWorkshopResponse response = workshopService.createWorkshop(workshop, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the workshop");
        }
    }

    /**
     * Get all workshops for the authenticated user
     */
    @GetMapping("/workshops")
    public ResponseEntity<?> getAllWorkshops(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<Workshop> workshops = workshopService.getAllWorkshopsForUser(userEmail);
            return ResponseEntity.ok(workshops);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving workshops");
        }
    }

    /**
     * Get a specific workshop by ID
     */
    @GetMapping("/workshops/{id}")
    public ResponseEntity<?> getWorkshopById(@PathVariable String id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Optional<Workshop> workshop = workshopService.getWorkshopById(id, userEmail);

            if (workshop.isPresent()) {
                return ResponseEntity.ok(workshop.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workshop not found or access denied");
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving the workshop");
        }
    }

    /**
     * Get workshops by date for the authenticated user
     */
    @GetMapping("/workshops/by-date")
    public ResponseEntity<?> getWorkshopsByDate(
            @RequestParam String date,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<Workshop> workshops = workshopService.getWorkshopsByDate(date, userEmail);
            return ResponseEntity.ok(workshops);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving workshops by date");
        }
    }

    /**
     * Get workshops by category for the authenticated user
     */
    @GetMapping("/workshops/by-category")
    public ResponseEntity<?> getWorkshopsByCategory(
            @RequestParam String category,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<Workshop> workshops = workshopService.getWorkshopsByCategory(category, userEmail);
            return ResponseEntity.ok(workshops);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving workshops by category");
        }
    }

    /**
     * Get workshops by venue for the authenticated user
     */
    @GetMapping("/workshops/by-venue")
    public ResponseEntity<?> getWorkshopsByVenue(
            @RequestParam String venue,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<Workshop> workshops = workshopService.getWorkshopsByVenue(venue, userEmail);
            return ResponseEntity.ok(workshops);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving workshops by venue");
        }
    }

    /**
     * Update a workshop
     */
    @PutMapping("/workshops/{id}")
    public ResponseEntity<?> updateWorkshop(
            @PathVariable String id,
            @RequestBody Workshop updatedWorkshop,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Optional<Workshop> workshop = workshopService.updateWorkshop(id, updatedWorkshop, userEmail);

            if (workshop.isPresent()) {
                return ResponseEntity.ok(workshop.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workshop not found or access denied");
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the workshop");
        }
    }

    /**
     * Delete a workshop (hard delete - physically removes from database)
     */
    @DeleteMapping("/workshops/{id}")
    public ResponseEntity<?> deleteWorkshop(@PathVariable String id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            boolean deleted = workshopService.deleteWorkshop(id, userEmail);

            if (deleted) {
                return ResponseEntity.ok("Workshop deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workshop not found or access denied");
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the workshop");
        }
    }

    /**
     * Get active workshops for the authenticated user
     */
    @GetMapping("/workshops/active")
    public ResponseEntity<?> getActiveWorkshops(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<Workshop> workshops = workshopService.getActiveWorkshopsForUser(userEmail);
            return ResponseEntity.ok(workshops);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving active workshops");
        }
    }

    /**
     * Get upcoming workshops for the authenticated user
     */
    @GetMapping("/workshops/upcoming")
    public ResponseEntity<?> getUpcomingWorkshops(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<Workshop> workshops = workshopService.getUpcomingWorkshopsForUser(userEmail);
            return ResponseEntity.ok(workshops);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving upcoming workshops");
        }
    }
}
