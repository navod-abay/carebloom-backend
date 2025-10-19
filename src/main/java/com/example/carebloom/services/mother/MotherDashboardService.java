package com.example.carebloom.services.mother;

import com.example.carebloom.dto.MOHOfficeDto;
import com.example.carebloom.dto.midwife.MidwifeBasicDTO;
import com.example.carebloom.dto.mother.DashboardResponse;
import com.example.carebloom.models.*;
import com.example.carebloom.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotherDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(MotherDashboardService.class);

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private HintRepository hintRepository;

    @Autowired
    private WorkshopRepository workshopRepository;

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private com.example.carebloom.services.mothers.MotherClinicService motherClinicService;

    public DashboardResponse getDashboardData(String firebaseUid) {
        try {
            logger.info("Fetching dashboard data for Firebase UID: {}", firebaseUid);

            // Get mother profile
            Mother mother = motherRepository.findByFirebaseUid(firebaseUid);
            if (mother == null) {
                logger.error("No mother found for Firebase UID: {}", firebaseUid);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother profile not found");
            }

            DashboardResponse response = new DashboardResponse();

            // Build user profile from mother data
            response.setMotherProfile(buildMotherProfile(mother));

            // Get health tips (hints)
            response.setHealthTips(getHealthTips());

            // Get workshops for mother's MOH office
            response.setWorkshops(getWorkshops(mother.getMohOfficeId()));

            // Get clinic appointments for the mother
            response.setClinics(motherClinicService.getClinicAppointmentsForMother(firebaseUid));

            logger.info("Dashboard data successfully fetched for mother: {}", mother.getId());
            return response;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching dashboard data for Firebase UID: {}", firebaseUid, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to fetch dashboard data: " + e.getMessage());
        }
    }

    private DashboardResponse.MotherProfile buildMotherProfile(Mother mother) {
        DashboardResponse.MotherProfile MotherProfile = new DashboardResponse.MotherProfile();
        
        MotherProfile.setId(mother.getId());
        MotherProfile.setEmail(mother.getEmail());
        MotherProfile.setName(mother.getName());
        MotherProfile.setDueDate(mother.getDueDate());
        MotherProfile.setPhone(mother.getPhone());
        MotherProfile.setAddress(mother.getAddress());
        MotherProfile.setDistrict(mother.getDistrict());
        MotherProfile.setRecordNumber(mother.getRecordNumber());
        MotherProfile.setRegistrationStatus(mother.getRegistrationStatus());
        MotherProfile.setFirebaseUid(mother.getFirebaseUid());
        MotherProfile.setProfilePhotoUrl(mother.getProfilePhotoUrl());

        // Get MOH Office details
        if (mother.getMohOfficeId() != null) {
            try {
                MOHOffice mohOffice = mohOfficeRepository.findByid(mother.getMohOfficeId());
                if (mohOffice != null) {
                    MotherProfile.setMohOffice(MOHOfficeDto.fromEntity(mohOffice));
                }
            } catch (Exception e) {
                logger.warn("Could not fetch MOH office details for mother: {}", mother.getId(), e);
            }
        }

        // Get Area Midwife details
        if (mother.getAreaMidwifeId() != null) {
            try {
                Midwife midwife = midwifeRepository.findById(mother.getAreaMidwifeId()).orElse(null);
                if (midwife != null) {
                    MidwifeBasicDTO midwifeDto = new MidwifeBasicDTO();
                    midwifeDto.setId(midwife.getId());
                    midwifeDto.setOfficeId(midwife.getOfficeId());
                    midwifeDto.setName(midwife.getName());
                    midwifeDto.setPhone(midwife.getPhone());
                    midwifeDto.setEmail(midwife.getEmail());
                    MotherProfile.setAreaMidwife(midwifeDto);
                }
            } catch (Exception e) {
                logger.warn("Could not fetch midwife details for mother: {}", mother.getId(), e);
            }
        }

        return MotherProfile;
    }

    private List<DashboardResponse.HealthTip> getHealthTips() {
        try {
            List<Hint> hints = hintRepository.findAll();
            return hints.stream()
                    .map(this::convertToHealthTip)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching health tips: {}", e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }

    private List<DashboardResponse.Workshop> getWorkshops(String mohOfficeId) {
        try {
            if (mohOfficeId == null) {
                logger.debug("No MOH office ID provided, returning empty workshops list");
                return List.of();
            }

            List<Workshop> workshops = workshopRepository.findByIsActiveTrueAndMohOfficeId(mohOfficeId);
            return workshops.stream()
                    .map(this::convertToWorkshopDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching workshops for MOH office: {}", mohOfficeId, e);
            return List.of(); // Return empty list on error
        }
    }

    private DashboardResponse.HealthTip convertToHealthTip(Hint hint) {
        DashboardResponse.HealthTip healthTip = new DashboardResponse.HealthTip();
        healthTip.setId(hint.getId());
        healthTip.setTitle(hint.getTitle());
        healthTip.setDescription(hint.getDescription());
        healthTip.setTrimester(hint.getTrimester());
        healthTip.setImageUrl(hint.getImageUrl());
        return healthTip;
    }

    private DashboardResponse.Workshop convertToWorkshopDto(Workshop workshop) {
        DashboardResponse.Workshop workshopDto = new DashboardResponse.Workshop();
        workshopDto.setId(workshop.getId());
        workshopDto.setTitle(workshop.getTitle());
        workshopDto.setDescription(workshop.getDescription());
        workshopDto.setDate(workshop.getDate());
        workshopDto.setLocation(workshop.getVenue()); // venue maps to location
        workshopDto.setCapacity(workshop.getCapacity());
        workshopDto.setEnrolled(workshop.getEnrolled());
        workshopDto.setMohOfficeId(workshop.getMohOfficeId());
        return workshopDto;
    }
}
