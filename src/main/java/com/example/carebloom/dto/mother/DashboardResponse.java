package com.example.carebloom.dto.mother;

import com.example.carebloom.dto.MOHOfficeDto;
import com.example.carebloom.dto.clinics.ClinicAppointmentDto;
import com.example.carebloom.dto.midwife.MidwifeBasicDTO;
import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse {
    private MotherProfile MotherProfile;
    private List<HealthTip> healthTips;
    private List<Workshop> workshops;
    private List<ClinicAppointmentDto> clinics;

    @Data
    public static class MotherProfile {
        private String id;
        private String email;
        private String name;
        private String dueDate;
        private String phone;
        private String address;
        private String district;
        private String recordNumber;
        private String registrationStatus; // 'initial' | 'location_pending' | 'photo_pending' | 'normal' | 'complete' | 'accepted'
        private String firebaseUid;
        private String profilePhotoUrl;
        private MOHOfficeDto mohOffice;
        private MidwifeBasicDTO areaMidwife;
    }

    @Data
    public static class HealthTip {
        private String id;
        private String title;
        private String description;
        private int trimester;
        private String imageUrl;
    }

    @Data
    public static class Workshop {
        private String id;
        private String title;
        private String description;
        private String date;
        private String location;
        private Integer capacity;
        private Integer enrolled;
        private String mohOfficeId;
    }
}
