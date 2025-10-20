package com.example.carebloom.services.moh;

import com.example.carebloom.dto.moh.DashboardResponse;
import com.example.carebloom.models.*;
import com.example.carebloom.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoHDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(MoHDashboardService.class);

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private FieldVisitRepository fieldVisitRepository;

    @Autowired
    private ForumThreadRepository forumThreadRepository;

    @Autowired
    private ChildRepository childRepository;

    /**
     * Get dashboard data for MOH office user
     */
    public DashboardResponse getDashboardData(String firebaseUid) {
        // Get current MOH user
        MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (mohUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MOH user not found");
        }

        logger.info("Fetching dashboard data for MOH office: {}", mohUser.getOfficeId());

        DashboardResponse dashboard = new DashboardResponse();

        // Calculate date ranges
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        // Get mothers under this MOH office
        List<Mother> mohMothers = motherRepository.findByMohOfficeId(mohUser.getOfficeId());
        List<String> motherIds = mohMothers.stream().map(Mother::getId).collect(Collectors.toList());

        // 1. Total mothers visited this month
        dashboard.setTotalMothersVisitedThisMonth(calculateMothersVisitedThisMonth(motherIds, startOfMonth, endOfMonth));

        // 2. Total medical forum questions
        dashboard.setTotalMedicalForumQuestions(calculateMedicalForumQuestions());

        // 3. Overdue vaccinations (placeholder - vaccination system not implemented)
        dashboard.setOverdueVaccinations(0);

        // 4. Health updates this month (placeholder - health updates system not implemented)
        dashboard.setHealthUpdatesThisMonth(0);

        // 5. Upcoming visits
        dashboard.setUpcomingVisits(getUpcomingVisits(motherIds));

        // 6. Medical forum questions
        dashboard.setMedicalForumQuestions(getRecentMedicalForumQuestions());

        // 7. Recent mother health summaries
        dashboard.setRecentMotherHealthSummaries(getRecentMotherHealthSummaries(mohMothers));

        // 8. Infant vaccinations due (placeholder - vaccination system not implemented)
        dashboard.setInfantVaccinationsDue(getInfantVaccinationsDue(motherIds));

        logger.info("Dashboard data compiled successfully for MOH office: {}", mohUser.getOfficeId());
        return dashboard;
    }

    /**
     * Calculate mothers visited this month
     */
    private int calculateMothersVisitedThisMonth(List<String> motherIds, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        try {
            List<FieldVisit> completedVisits = fieldVisitRepository.findByStatusAndCreatedAtBetween(
                "COMPLETED", startOfMonth, endOfMonth);

            // Count unique mothers from completed visits
            return (int) completedVisits.stream()
                .flatMap(visit -> visit.getSelectedMotherIds().stream())
                .filter(motherIds::contains)
                .distinct()
                .count();
        } catch (Exception e) {
            logger.error("Error calculating mothers visited this month: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate total medical forum questions
     */
    private int calculateMedicalForumQuestions() {
        try {
            return (int) forumThreadRepository.countByCategory(ForumThread.Category.MEDICAL);
        } catch (Exception e) {
            logger.error("Error calculating medical forum questions: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get upcoming visits
     */
    private List<DashboardResponse.MotherVisitDto> getUpcomingVisits(List<String> motherIds) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String today = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String nextWeek = now.plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Get all scheduled/calculated visits and filter by date in service layer
            List<FieldVisit> allUpcomingVisits = fieldVisitRepository.findByStatusIn(
                List.of("SCHEDULED", "CALCULATED"));

            return allUpcomingVisits.stream()
                .filter(visit -> visit.getDate() != null && 
                               visit.getDate().compareTo(today) >= 0 && 
                               visit.getDate().compareTo(nextWeek) <= 0)
                .flatMap(visit -> visit.getSelectedMotherIds().stream()
                    .filter(motherIds::contains)
                    .map(motherId -> {
                        Mother mother = motherRepository.findById(motherId).orElse(null);
                        if (mother != null) {
                            DashboardResponse.MotherVisitDto dto = new DashboardResponse.MotherVisitDto();
                            dto.setId(visit.getId());
                            dto.setMotherId(motherId);
                            dto.setMotherName(mother.getName());
                            dto.setAddress(mother.getAddress());
                            dto.setScheduledDate(visit.getDate());
                            dto.setStatus(mapVisitStatus(visit.getStatus()));
                            return dto;
                        }
                        return null;
                    })
                    .filter(dto -> dto != null))
                .limit(10)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting upcoming visits: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get recent medical forum questions
     */
    private List<DashboardResponse.ForumQuestionDto> getRecentMedicalForumQuestions() {
        try {
            List<ForumThread> medicalQuestions = forumThreadRepository.findTop10ByCategoryOrderByCreatedAtDesc(
                ForumThread.Category.MEDICAL);

            return medicalQuestions.stream()
                .map(thread -> {
                    DashboardResponse.ForumQuestionDto dto = new DashboardResponse.ForumQuestionDto();
                    dto.setId(thread.getId());
                    dto.setAuthor(thread.getAuthorName());
                    dto.setQuestion(thread.getTitle());
                    dto.setDate(thread.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    dto.setMedical(thread.getCategory() == ForumThread.Category.MEDICAL);
                    dto.setReplied(!thread.getReplies().isEmpty());
                    return dto;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting medical forum questions: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get recent mother health summaries
     */
    private List<DashboardResponse.MotherHealthSummaryDto> getRecentMotherHealthSummaries(List<Mother> mothers) {
        try {
            return mothers.stream()
                .limit(10)
                .map(mother -> {
                    DashboardResponse.MotherHealthSummaryDto dto = new DashboardResponse.MotherHealthSummaryDto();
                    dto.setMotherId(mother.getId());
                    dto.setMotherName(mother.getName());
                    dto.setPregnancyStage(calculatePregnancyStage(mother.getDueDate()));
                    dto.setLastCheckupDate("N/A"); // Health records system not implemented
                    
                    // Check for upcoming field visit appointment
                    if (mother.getFieldVisitAppointment() != null) {
                        dto.setUpcomingAppointmentDate(mother.getFieldVisitAppointment().getDate());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting mother health summaries: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get infant vaccinations due (placeholder)
     */
    private List<DashboardResponse.InfantVaccinationDto> getInfantVaccinationsDue(List<String> motherIds) {
        try {
            // This is a placeholder since vaccination tracking is not implemented
            // In the future, this would check vaccination schedules against child birth dates
            List<Child> children = childRepository.findByMotherIdIn(motherIds);
            
            return children.stream()
                .limit(5)
                .map(child -> {
                    DashboardResponse.InfantVaccinationDto dto = new DashboardResponse.InfantVaccinationDto();
                    dto.setInfantId(child.getId());
                    dto.setInfantName(child.getName());
                    dto.setVaccineName("BCG Vaccine"); // Placeholder
                    dto.setDueDate("2024-12-25"); // Placeholder
                    dto.setStatus("Due"); // Placeholder
                    return dto;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting infant vaccinations: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Calculate pregnancy stage based on due date
     */
    private String calculatePregnancyStage(String dueDate) {
        if (dueDate == null || dueDate.isEmpty()) {
            return "Unknown";
        }
        
        try {
            // Simple calculation - in real implementation would be more sophisticated
            LocalDateTime due = LocalDateTime.parse(dueDate + "T00:00:00");
            LocalDateTime now = LocalDateTime.now();
            long weeksToGo = java.time.temporal.ChronoUnit.WEEKS.between(now, due);
            
            if (weeksToGo < 0) {
                return "Post-delivery";
            } else if (weeksToGo <= 4) {
                return "Third Trimester (Near Due)";
            } else if (weeksToGo <= 12) {
                return "Third Trimester";
            } else if (weeksToGo <= 24) {
                return "Second Trimester";
            } else {
                return "First Trimester";
            }
        } catch (Exception e) {
            logger.error("Error calculating pregnancy stage: {}", e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Map visit status to frontend format
     */
    private String mapVisitStatus(String status) {
        switch (status.toUpperCase()) {
            case "SCHEDULED":
            case "CALCULATED":
                return "Scheduled";
            case "COMPLETED":
                return "Completed";
            case "CANCELLED":
                return "Canceled";
            default:
                return "Scheduled";
        }
    }
}