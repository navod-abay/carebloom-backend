package com.example.carebloom.dto.moh;

import lombok.Data;
import java.util.List;

@Data
public class DashboardResponse {
    private int totalMothersVisitedThisMonth;
    private int totalMedicalForumQuestions;
    private int overdueVaccinations;
    private int healthUpdatesThisMonth;
    private List<MotherVisitDto> upcomingVisits;
    private List<ForumQuestionDto> medicalForumQuestions;
    private List<MotherHealthSummaryDto> recentMotherHealthSummaries;
    private List<InfantVaccinationDto> infantVaccinationsDue;

    @Data
    public static class MotherVisitDto {
        private String id;
        private String motherName;
        private String motherId;
        private String address;
        private String scheduledDate;
        private String status; // 'Scheduled', 'Completed', 'Canceled'
    }

    @Data
    public static class ForumQuestionDto {
        private String id;
        private String author;
        private String question;
        private String date;
        private boolean isMedical;
        private boolean replied;
    }

    @Data
    public static class MotherHealthSummaryDto {
        private String motherId;
        private String motherName;
        private String pregnancyStage;
        private String lastCheckupDate;
        private String upcomingAppointmentDate;
    }

    @Data
    public static class InfantVaccinationDto {
        private String infantId;
        private String infantName;
        private String vaccineName;
        private String dueDate;
        private String status; // 'Due', 'Given', 'Overdue'
        private String givenDate;
    }
}