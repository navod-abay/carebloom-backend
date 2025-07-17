package com.example.carebloom.dto.moh;

import com.example.carebloom.models.Mother;
import lombok.Data;
import java.util.List;

/**
 * Response DTO for available mothers for clinic appointments
 */
@Data
public class AvailableMothersResponse {
    private String message;
    private int count;
    private List<Mother> mothers;
    
    public AvailableMothersResponse() {}
    
    public AvailableMothersResponse(String message, int count, List<Mother> mothers) {
        this.message = message;
        this.count = count;
        this.mothers = mothers;
    }
    
    /**
     * Static factory method for successful response
     */
    public static AvailableMothersResponse success(List<Mother> mothers) {
        return new AvailableMothersResponse(
            "Available mothers for clinic appointments",
            mothers.size(),
            mothers
        );
    }
    
    /**
     * Static factory method for empty response
     */
    public static AvailableMothersResponse empty() {
        return new AvailableMothersResponse(
            "No registered mothers found for this MoH office",
            0,
            List.of()
        );
    }
}
