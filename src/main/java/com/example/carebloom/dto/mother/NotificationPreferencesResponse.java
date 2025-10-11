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
@Schema(description = "User's notification preferences")
public class NotificationPreferencesResponse {
    
    @Schema(description = "Emergency alerts (always enabled)", example = "true")
    private Boolean emergencyAlerts;
    
    @Schema(description = "App updates and announcements", example = "true")
    private Boolean appUpdates;
    
    @Schema(description = "MOH office notifications", example = "true")
    private Boolean mohOfficeNotifications;
    
    @Schema(description = "Trimester-based health updates", example = "true")
    private Boolean trimesterUpdates;
}