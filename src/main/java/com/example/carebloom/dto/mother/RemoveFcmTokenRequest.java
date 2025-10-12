package com.example.carebloom.dto.mother;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to remove FCM token")
public class RemoveFcmTokenRequest {
    
    @Schema(description = "Firebase Cloud Messaging token to remove", 
            example = "fGHj7890abcdef1234567890...")
    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}