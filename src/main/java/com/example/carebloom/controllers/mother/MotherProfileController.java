package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.mother.PhotoUploadUrlRequest;
import com.example.carebloom.dto.mother.PhotoUploadUrlResponse;
import com.example.carebloom.dto.mother.PhotoConfirmRequest;
import com.example.carebloom.dto.mother.LocationUpdateRequest;
import com.example.carebloom.dto.mother.MotherProfileResponse;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.services.GoogleCloudStorageService;
import com.example.carebloom.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mothers")
@CrossOrigin(origins = "${app.cors.mother-origin}")
@Tag(name = "Mother Profile", description = "Mother profile and photo management APIs")
public class MotherProfileController {

    private static final Logger logger = LoggerFactory.getLogger(MotherProfileController.class);

    @Autowired
    private GoogleCloudStorageService gcsService;

    @Autowired
    private MotherRepository motherRepository;

    @Operation(summary = "Generate photo upload URL", 
               description = "Generate a signed URL for uploading profile photo to Google Cloud Storage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upload URL generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file name or unsupported file type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/profile/photo/upload-url")
    public ResponseEntity<?> generatePhotoUploadUrl(@Valid @RequestBody PhotoUploadUrlRequest request) {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            logger.info("Generating photo upload URL for mother: {}", currentMother.getId());

            // Generate signed URL
            URL uploadUrl = gcsService.generateUploadUrl(
                currentMother.getId(), 
                request.getFileName()
            );

            // Create response
            PhotoUploadUrlResponse response = PhotoUploadUrlResponse.builder()
                .uploadUrl(uploadUrl.toString())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

            logger.info("Successfully generated upload URL for mother: {}", 
                       currentMother.getId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generating upload URL: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to generate upload URL"));
        }
    }

    @Operation(summary = "Confirm photo upload", 
               description = "Confirm that photo has been uploaded and update mother's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photo upload confirmed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid photoId or file not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Upload record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/profile/photo/confirm")
    public ResponseEntity<?> confirmPhotoUpload(@Valid @RequestBody PhotoConfirmRequest request) {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            logger.info("Confirming photo upload for mother: {}", currentMother.getId());

            // Verify the file exists in GCS
            boolean fileExists = gcsService.fileExists(currentMother.getId(), request.getFileName());
            if (!fileExists) {
                logger.warn("File not found in GCS for mother: {}, fileName: {}", 
                           currentMother.getId(), request.getFileName());
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("File not found in storage"));
            }

            // Update mother's profile with photo information
            String publicUrl = gcsService.getPublicUrl(currentMother.getId(), request.getFileName());
            currentMother.setProfilePhotoUrl(publicUrl);
            currentMother.setProfilePhotoUploadedAt(LocalDateTime.now());
            currentMother.setRegistrationStatus("location_pending");

            Mother updatedMother = motherRepository.save(currentMother);

            logger.info("Successfully confirmed photo upload for mother: {}", currentMother.getId());

            return ResponseEntity.ok(updatedMother);

        } catch (Exception e) {
            logger.error("Error confirming photo upload: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to confirm photo upload"));
        }
    }

    @Operation(summary = "Delete profile photo", 
               description = "Delete the mother's profile photo from storage and update profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photo deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No profile photo to delete"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/profile/photo")
    public ResponseEntity<?> deleteProfilePhoto() {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            logger.info("Deleting profile photo for mother: {}", currentMother.getId());

            // Check if mother has a profile photo
            if (currentMother.getProfilePhotoUrl() == null || currentMother.getProfilePhotoUrl().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("No profile photo to delete"));
            }

            // Extract filename from URL to delete from GCS
            String fileName = extractFileNameFromUrl(currentMother.getProfilePhotoUrl());
            if (fileName != null) {
                boolean deleted = gcsService.deleteFile(currentMother.getId(), fileName);
                if (!deleted) {
                    logger.warn("File was not found in GCS, but will clear from profile anyway");
                }
            }

            // Clear photo information from mother's profile
            currentMother.setProfilePhotoUrl(null);
            currentMother.setProfilePhotoUploadedAt(null);

            Mother updatedMother = motherRepository.save(currentMother);

            logger.info("Successfully deleted photo for mother: {}", currentMother.getId());

            return ResponseEntity.ok(updatedMother);

        } catch (Exception e) {
            logger.error("Error deleting profile photo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to delete profile photo"));
        }
    }

    @Operation(summary = "Update location", 
               description = "Update the mother's location coordinates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid location data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/auth/profile/location")
    public ResponseEntity<?> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        try {
            Mother currentMother = SecurityUtils.getCurrentMother();
            if (currentMother == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            logger.info("Updating location for mother: {}", currentMother.getId());

            // Validate location data
            if (request.getLocation() == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Location data is required"));
            }

            LocationUpdateRequest.LocationCoordinates locationData = request.getLocation();
            
            // Validate coordinates
            if (locationData.getLatitude() == null || locationData.getLongitude() == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Latitude and longitude are required"));
            }

            // Basic validation for coordinate ranges
            if (locationData.getLatitude() < -90 || locationData.getLatitude() > 90) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid latitude value. Must be between -90 and 90"));
            }

            if (locationData.getLongitude() < -180 || locationData.getLongitude() > 180) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid longitude value. Must be between -180 and 180"));
            }

            // Update mother's location
            currentMother.setLatitude(locationData.getLatitude());
            currentMother.setLongitude(locationData.getLongitude());
            currentMother.setUpdatedAt(LocalDateTime.now());

            Mother updatedMother = motherRepository.save(currentMother);

            // Convert to response DTO
            MotherProfileResponse response = convertToProfileResponse(updatedMother, locationData);

            logger.info("Successfully updated location for mother: {}", currentMother.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating location: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update location"));
        }
    }

    /**
     * Helper method to convert Mother entity to MotherProfileResponse
     */
    private MotherProfileResponse convertToProfileResponse(Mother mother, LocationUpdateRequest.LocationCoordinates locationData) {
        MotherProfileResponse response = new MotherProfileResponse();
        response.setId(mother.getId());
        response.setEmail(mother.getEmail());
        response.setName(mother.getName());
        response.setPhone(mother.getPhone());
        response.setDueDate(mother.getDueDate());
        response.setAddress(mother.getAddress());
        response.setDistrict(mother.getDistrict());
        response.setMohOfficeId(mother.getMohOfficeId());
        response.setAreaMidwifeId(mother.getAreaMidwifeId());
        response.setRecordNumber(mother.getRecordNumber());
        response.setUnitId(mother.getUnitId());
        response.setProfilePhotoUrl(mother.getProfilePhotoUrl());
        response.setCreatedAt(mother.getCreatedAt());
        response.setUpdatedAt(mother.getUpdatedAt());
        
        // Set location coordinates from the request (including accuracy and timestamp)
        if (locationData != null) {
            MotherProfileResponse.LocationCoordinates location = new MotherProfileResponse.LocationCoordinates();
            location.setLatitude(locationData.getLatitude());
            location.setLongitude(locationData.getLongitude());
            location.setAccuracy(locationData.getAccuracy());
            location.setTimestamp(locationData.getTimestamp());
            response.setLocation(location);
        }
        
        return response;
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

    /**
     * Helper method to extract filename from GCS URL
     */
    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            // Extract filename from URL like: https://storage.googleapis.com/bucket/profiles/userId/filename.jpg
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        } catch (Exception e) {
            logger.warn("Could not extract filename from URL: {}", url);
            return null;
        }
    }
}
