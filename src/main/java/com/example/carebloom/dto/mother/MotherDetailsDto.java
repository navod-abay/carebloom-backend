package com.example.carebloom.dto.mother;

import java.util.List;
import lombok.Data;

import com.example.carebloom.models.VisitRecord;

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
    private HealthRecordsDto healthDetails;
    private VisitRecord latestVitalRecord;
    private List<ChildRecordDto> childRecords;
    private List<WorkshopDto> workshops;
    
    @Data
    public static class HealthRecordsDto {
        private int age;
        private String bloodType;
        private String allergies;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String pregnancyType;
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
