package com.example.carebloom.dto;

import com.example.carebloom.models.MOHOffice;
import lombok.Data;

/**
 * Data Transfer Object for MOH Office information.
 * This ensures we only send necessary data to clients and exclude sensitive information.
 */
@Data
public class MOHOfficeDto {
    private String id;
    private String divisionalSecretariat;
    private String district;
    private String address;
    private double latitude;
    private double longitude;
    private String officerInCharge;
    private String contactNumber;
    
    /**
     * Converts a MOHOffice entity to a MOHOfficeDto
     * 
     * @param office The MOHOffice entity to convert
     * @return A new MOHOfficeDto with the relevant fields
     */
    public static MOHOfficeDto fromEntity(MOHOffice office) {
        MOHOfficeDto dto = new MOHOfficeDto();
        dto.setId(office.getId());
        dto.setDivisionalSecretariat(office.getDivisionalSecretariat());
        dto.setDistrict(office.getDistrict());
        dto.setAddress(office.getAddress());
        
        if (office.getLocation() != null) {
            dto.setLatitude(office.getLocation().getLatitude());
            dto.setLongitude(office.getLocation().getLongitude());
        }
        
        dto.setOfficerInCharge(office.getOfficerInCharge());
        dto.setContactNumber(office.getContactNumber());
        
        return dto;
    }
}
