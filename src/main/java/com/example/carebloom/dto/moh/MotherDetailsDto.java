package com.example.carebloom.dto.moh;

import java.util.List;
import lombok.Data;

@Data
public class MotherDetailsDto {
    private String id;
    private String name;
    private String registrationStatus;
    private String firebaseUid;
    private String email;
    private String phone;
    private String dueDate;
    private String address;
    private String district;
    private String mohOfficeId;
    private String recordNumber;
    private HealthRecordsDto healthRecords;
    private List<ChildRecordDto> childRecords;
    private List<WorkshopDto> workshops;
    
    @Data
    public static class HealthRecordsDto {
        private Object age; // Can be number or string
        private String bloodType;
        private String medicalHistory;
        private String allergies;
        private String currentMedications;
        private String emergencyContact;
    }
    
    @Data
    public static class ChildRecordDto {
        private String id;
        private String name;
        private String dob;
        private String gender;
        private String birthWeight;
        private String birthLength;
        private List<String> vaccinations;
        private String healthNotes;
    }
    
    @Data
    public static class WorkshopDto {
        private String name;
        private String date;
    }
}
