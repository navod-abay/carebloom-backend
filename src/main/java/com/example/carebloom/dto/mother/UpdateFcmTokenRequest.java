package com.example.carebloom.dto.mother;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request to update FCM token for push notifications")
public class UpdateFcmTokenRequest {
    
    @Schema(description = "Firebase Cloud Messaging token", 
            example = "fGHj7890abcdef1234567890...")
    @NotBlank(message = "FCM token is required")
    private String fcmToken;
    
    @Schema(description = "Device type", example = "android", allowableValues = {"android"})
    @Pattern(regexp = "android", message = "Only android devices are supported")
    private String deviceType = "android";
}