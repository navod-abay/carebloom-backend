package com.example.carebloom.dto.midwife;

import lombok.Data;
import java.util.List;

@Data
public class FieldVisitResponseDTO {
    private String id;
    private String date;
    private String startTime;
    private String endTime;
    private String midwifeId;
    private List<MotherBasicInfo> mothers;
    private String status; // 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
    
    @Data
    public static class MotherBasicInfo {
        private String id;
        private String name;
    }
}
