package com.example.carebloom.dto.mother;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to update notification preferences")
public class UpdateNotificationPreferencesRequest {
    
    @Schema(description = "Emergency alerts (cannot be disabled)", example = "true")
    @NotNull(message = "Emergency alerts preference is required")
    private Boolean emergencyAlerts;
    
    @Schema(description = "App updates and announcements", example = "false")
    @NotNull(message = "App updates preference is required")
    private Boolean appUpdates;
    
    @Schema(description = "MOH office notifications", example = "true")
    @NotNull(message = "MOH office notifications preference is required")
    private Boolean mohOfficeNotifications;
    
    @Schema(description = "Trimester-based health updates", example = "true")
    @NotNull(message = "Trimester updates preference is required")
    private Boolean trimesterUpdates;
}