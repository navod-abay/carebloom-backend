package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.mother.*;
import com.example.carebloom.models.Mother;
import com.example.carebloom.services.NotificationService;
import com.example.carebloom.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mothers/notifications")
@CrossOrigin(origins = "${app.cors.mother-origin}")
@Tag(name = "Mother Notifications", description = "Notification management APIs for mothers")
public class MotherNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(MotherNotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Update FCM token", 
               description = "Update the Firebase Cloud Messaging token for push notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "FCM token updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token format or missing fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(@Valid @RequestBody UpdateFcmTokenRequest request) {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            // Validate FCM token format
            if (!notificationService.isValidFcmToken(request.getFcmToken())) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid FCM token format"));
            }

            logger.info("Updating FCM token for mother: {}", currentMother.getId());

            NotificationResponse response = notificationService.updateFcmToken(
                currentMother.getId(), 
                request.getFcmToken()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating FCM token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update FCM token"));
        }
    }

    @Operation(summary = "Remove FCM token", 
               description = "Remove the Firebase Cloud Messaging token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "FCM token removed successfully"),
        @ApiResponse(responseCode = "400", description = "Missing fcmToken in request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Token not found for user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/fcm-token")
    public ResponseEntity<?> removeFcmToken(@Valid @RequestBody RemoveFcmTokenRequest request) {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            logger.info("Removing FCM token for mother: {}", currentMother.getId());

            NotificationResponse response = notificationService.removeFcmToken(
                currentMother.getId(), 
                request.getFcmToken()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("No FCM token found") || 
                e.getMessage().contains("does not match")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
            }
            logger.error("Error removing FCM token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to remove FCM token"));
        } catch (Exception e) {
            logger.error("Error removing FCM token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to remove FCM token"));
        }
    }

    @Operation(summary = "Get notification preferences", 
               description = "Retrieve the user's notification preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User preferences not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/preferences")
    public ResponseEntity<?> getNotificationPreferences() {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            logger.info("Getting notification preferences for mother: {}", currentMother.getId());

            NotificationPreferencesResponse preferences = notificationService.getNotificationPreferences(
                currentMother.getId()
            );

            return ResponseEntity.ok(preferences);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("User preferences not found"));
            }
            logger.error("Error getting notification preferences: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get notification preferences"));
        } catch (Exception e) {
            logger.error("Error getting notification preferences: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get notification preferences"));
        }
    }

    @Operation(summary = "Update notification preferences", 
               description = "Update the user's notification preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid preference values or missing fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "422", description = "Cannot disable critical notifications"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/preferences")
    public ResponseEntity<?> updateNotificationPreferences(@Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            logger.info("Updating notification preferences for mother: {}", currentMother.getId());

            NotificationResponse response = notificationService.updateNotificationPreferences(
                currentMother.getId(), 
                request
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating notification preferences: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update notification preferences"));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }
}