package com.example.carebloom.dto.mother;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API response for notification operations")
public class NotificationResponse {
    
    @Schema(description = "Operation success status", example = "true")
    private Boolean success;
    
    @Schema(description = "Response message", example = "FCM token updated successfully")
    private String message;
}