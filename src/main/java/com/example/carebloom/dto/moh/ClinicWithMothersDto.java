package com.example.carebloom.dto.moh;

import com.example.carebloom.models.Mother;
import com.example.carebloom.models.Clinic;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO for clinic with populated mother details
 */
@Data
public class ClinicWithMothersDto {
    private String id;
    private String userId;
    private String mohOfficeId;
    private String title;
    private String date;
    private String startTime;
    private String doctorName;
    private String location;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer maxCapacity;
    private String notes;
    
    // Keep original registeredMotherIds for backward compatibility
    private List<String> registeredMotherIds = new ArrayList<>();
    
    // Full mother details instead of just IDs
    private List<Mother> addedMothers = new ArrayList<>();
    
    public ClinicWithMothersDto() {}
    
    /**
     * Convert Clinic entity to ClinicWithMothersDto
     */
    public static ClinicWithMothersDto fromClinic(Clinic clinic) {
        ClinicWithMothersDto dto = new ClinicWithMothersDto();
        dto.setId(clinic.getId());
        dto.setUserId(clinic.getUserId());
        dto.setMohOfficeId(clinic.getMohOfficeId());
        dto.setTitle(clinic.getTitle());
        dto.setDate(clinic.getDate());
        dto.setStartTime(clinic.getStartTime());
        dto.setDoctorName(clinic.getDoctorName());
        dto.setLocation(clinic.getLocation());
        dto.setActive(clinic.isActive());
        dto.setCreatedAt(clinic.getCreatedAt());
        dto.setUpdatedAt(clinic.getUpdatedAt());
        dto.setMaxCapacity(clinic.getMaxCapacity());
        dto.setNotes(clinic.getNotes());
        dto.setRegisteredMotherIds(clinic.getRegisteredMotherIds());
        return dto;
    }
    
    /**
     * Convert Clinic entity to ClinicWithMothersDto with populated mothers
     */
    public static ClinicWithMothersDto fromClinicWithMothers(Clinic clinic, List<Mother> mothers) {
        ClinicWithMothersDto dto = fromClinic(clinic);
        dto.setAddedMothers(mothers != null ? mothers : new ArrayList<>());
        return dto;
    }
}
