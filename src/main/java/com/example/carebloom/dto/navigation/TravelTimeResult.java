package com.example.carebloom.dto.navigation;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelTimeResult {
    private Duration duration;
    private Duration durationInTraffic;
    private double distanceMeters;
    private String status;
    private LocalDateTime calculatedAt;
    private boolean fromCache;
    
    // Helper methods
    public long getDurationSeconds() {
        return duration != null ? duration.getSeconds() : 0;
    }
    
    public long getDurationInTrafficSeconds() {
        return durationInTraffic != null ? durationInTraffic.getSeconds() : getDurationSeconds();
    }
    
    public boolean isValid() {
        return "OK".equals(status) && duration != null;
    }
}
