package com.example.carebloom.services.mother;

import com.example.carebloom.dto.hospitals.HospitalDashboardDto;
import com.example.carebloom.dto.moh_offices.MoHOfficeInfoDto;
import com.example.carebloom.dto.midwives.AssignedMidwifeDto;
import com.example.carebloom.dto.clinics.ClinicAppointmentDto;
import com.example.carebloom.dto.workshops.AssignedWorkshopDto;
import com.example.carebloom.models.*;
import com.example.carebloom.repositories.*;
import com.example.carebloom.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotherHospitalService {
    
    private static final Logger logger = LoggerFactory.getLogger(MotherHospitalService.class);
    
    @Autowired
    private MOHOfficeRepository mohOfficeRepository;
    
    @Autowired
    private MidwifeRepository midwifeRepository;
    
    @Autowired
    private ClinicRepository clinicRepository;
    
    @Autowired
    private WorkshopRepository workshopRepository;

    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public HospitalDashboardDto getMotherHospitalDashboard() {
        
        // Get the current mother from security context - no database query needed!
        Mother mother = SecurityUtils.getCurrentMother();
        if (mother == null) {
            throw new RuntimeException("Mother not found in security context");
        }
        
        logger.info("Building hospital dashboard for mother ID: {}", mother.getId());
        
        // Build dashboard
        HospitalDashboardDto dashboard = new HospitalDashboardDto();        // Set hospital info
        if (mother.getMohOfficeId() != null) {
            mohOfficeRepository.findById(mother.getMohOfficeId()).ifPresent(office -> {
                MoHOfficeInfoDto hospitalDto = new MoHOfficeInfoDto();
                hospitalDto.setDivisionalSecretariat(office.getDivisionalSecretariat());
                hospitalDto.setAddress(office.getAddress());
                hospitalDto.setContactNumber(office.getContactNumber());
                hospitalDto.setDistrict(office.getDistrict());
                hospitalDto.setOfficerInCharge(office.getOfficerInCharge());
                dashboard.setHospital(hospitalDto);
            });
        }

        logger.info("Successfully retrieved hospital info for mother ID: {}", mother.getId());
        logger.info("Mother's unit id: {}", mother.getUnitId());
        // Unit unit = unitRepository.findById(mother.getUnitId()).orElseThrow(() -> new RuntimeException("Unit not found with ID: " + mother.getUnitId()));

        logger.info("Successfully retrieved unit info for mother ID: {}", mother.getId());

        // Set assigned midwife
        if (mother.getAreaMidwifeId() != null) {
            midwifeRepository.findById(mother.getAreaMidwifeId()).ifPresent(midwife -> {
                AssignedMidwifeDto midwifeDto = new AssignedMidwifeDto();
                midwifeDto.setId(midwife.getId());
                midwifeDto.setName(midwife.getName());
                midwifeDto.setPhone(midwife.getPhone());
                midwifeDto.setEmail(midwife.getEmail());
                dashboard.setMidwife(midwifeDto);
            });
        }

        logger.info("Successfully retrieved midwife info for mother ID: {}", mother.getId());

        dashboard.setFieldVisitAppointment(mother.getFieldVisitAppointment());

        logger.info("Successfully retrieved home visit info for mother ID: {}", mother.getId());
        
        // Set upcoming clinics
        List<ClinicAppointmentDto> clinicDtos = buildUpcomingClinics(mother);
        dashboard.setClinics(clinicDtos);

        logger.info("Successfully retrieved clinic info for mother ID: {}", mother.getId());

        // Set available workshops
        if (mother.getMohOfficeId() != null) {
            List<AssignedWorkshopDto> workshopDtos = buildAvailableWorkshops(mother.getMohOfficeId());
            dashboard.setWorkshops(workshopDtos);
        }
        
        logger.info("Successfully built hospital dashboard for mother ID: {}", mother.getId());
        return dashboard;
    }
    
    private List<ClinicAppointmentDto> buildUpcomingClinics(Mother mother) {
        // Get clinics where mother is registered
        List<Clinic> registeredClinics = clinicRepository
            .findByRegisteredMotherIdsContainingAndIsActiveTrueOrderByDateAsc(mother.getId());
        
        // Filter for upcoming dates only
        String today = LocalDate.now().format(DATE_FORMATTER);
        
        // Combine and filter registered clinics
        List<ClinicAppointmentDto> upcomingClinics = registeredClinics.stream()
            .filter(clinic -> clinic.getDate().compareTo(today) >= 0)
            .map(clinic -> {
                ClinicAppointmentDto dto = new ClinicAppointmentDto();
                dto.setId(clinic.getId());
                dto.setDate(clinic.getDate());
                dto.setStartTime(clinic.getStartTime());
                dto.setTitle(clinic.getTitle());
                dto.setDoctorName(clinic.getDoctorName());
                dto.setLocation(clinic.getLocation());
                return dto;
            })
            .collect(Collectors.toList());
        return upcomingClinics;
    }
    
    private List<AssignedWorkshopDto> buildAvailableWorkshops(String mohOfficeId) {
        List<Workshop> workshops = workshopRepository
            .findByMohOfficeIdAndIsActiveTrueOrderByDateAsc(mohOfficeId);
        
        // Filter for upcoming workshops
        String today = LocalDate.now().format(DATE_FORMATTER);
        
        return workshops.stream()
            .filter(workshop -> workshop.getDate().compareTo(today) >= 0)
            .map(workshop -> {
                AssignedWorkshopDto dto = new AssignedWorkshopDto();
                dto.setId(workshop.getId());
                dto.setTitle(workshop.getTitle());
                dto.setDate(workshop.getDate());
                dto.setTime(workshop.getTime());
                dto.setVenue(workshop.getVenue());
                dto.setDescription(workshop.getDescription());
                dto.setCategory(workshop.getCategory());
                dto.setIsActive(workshop.isActive());
                return dto;
            })
            .collect(Collectors.toList());
    }
}
