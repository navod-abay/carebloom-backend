package com.example.carebloom.services.moh;

import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.ClinicRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.dto.CreateClinicRequest;
import com.example.carebloom.dto.CreateClinicResponse;
import com.example.carebloom.dto.UpdateClinicRequest;
import com.example.carebloom.dto.moh.ClinicWithMothersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class MoHClinicService {

    private static final Logger logger = LoggerFactory.getLogger(MoHClinicService.class);

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    @Autowired
    private MotherRepository motherRepository;

    /**
     * Get all clinics for the current user's MoH office
     */
    public List<Clinic> getAllClinicsByMohOffice() {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return Collections.emptyList();
        }
        return clinicRepository.findByMohOfficeIdAndIsActiveTrue(mohOfficeId);
    }

    /**
     * Get all clinics for the current user's MoH office with populated mother details
     */
    public List<ClinicWithMothersDto> getAllClinicsByMohOfficeWithMothers() {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return Collections.emptyList();
        }
        
        List<Clinic> clinics = clinicRepository.findByMohOfficeIdAndIsActiveTrue(mohOfficeId);
        
        return clinics.stream()
                .map(this::populateClinicWithMothers)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to populate a clinic with mother details
     */
    private ClinicWithMothersDto populateClinicWithMothers(Clinic clinic) {
        List<Mother> mothers = new ArrayList<>();
        
        if (clinic.getRegisteredMotherIds() != null && !clinic.getRegisteredMotherIds().isEmpty()) {
            try {
                // Fetch mothers by their IDs
                mothers = motherRepository.findAllById(clinic.getRegisteredMotherIds());
                logger.debug("Found {} mothers for clinic {}", mothers.size(), clinic.getId());
            } catch (Exception e) {
                logger.error("Error fetching mothers for clinic {}: {}", clinic.getId(), e.getMessage());
            }
        }
        
        return ClinicWithMothersDto.fromClinicWithMothers(clinic, mothers);
    }

    public List<Clinic> getClinicsByDate(String date) {
        return clinicRepository.findByDateAndIsActiveTrue(date);
    }

    public Optional<Clinic> getClinicById(String id) {
        Optional<Clinic> clinicOpt = clinicRepository.findById(id);
        if (!clinicOpt.isPresent()) {
            return Optional.empty();
        }

        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return Optional.empty();
        }

        Clinic clinic = clinicOpt.get();
        if (clinic.getMohOfficeId().equals(mohOfficeId)) {
            return Optional.of(clinic);
        } else {
            logger.warn("User attempted to access clinic from another MoH office: {}", id);
            return Optional.empty();
        }
    }

    /**
     * Get a clinic by ID with populated mother details
     */
    public Optional<ClinicWithMothersDto> getClinicByIdWithMothers(String id) {
        Optional<Clinic> clinicOpt = clinicRepository.findById(id);
        if (!clinicOpt.isPresent()) {
            return Optional.empty();
        }

        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return Optional.empty();
        }

        Clinic clinic = clinicOpt.get();
        if (clinic.getMohOfficeId().equals(mohOfficeId)) {
            return Optional.of(populateClinicWithMothers(clinic));
        } else {
            logger.warn("User attempted to access clinic from another MoH office: {}", id);
            return Optional.empty();
        }
    }

    /**
     * Get available mothers for clinic appointments
     * Fetches all registered mothers in the current user's MoH office
     */
    public List<Mother> getAvailableMothersForClinic() {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return Collections.emptyList();
        }
        
        try {
            List<Mother> mothers = motherRepository.findByMohOfficeId(mohOfficeId);
            logger.info("Found {} available mothers for clinic appointments in MoH office: {}", 
                       mothers.size(), mohOfficeId);
            return mothers;
        } catch (Exception e) {
            logger.error("Error fetching available mothers for MoH office: {}", mohOfficeId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Create a new clinic for the current user's MoH office
     */
    public CreateClinicResponse createClinic(CreateClinicRequest request) {
        try {
            String mohOfficeId = getCurrentUserMohOfficeId();
            if (mohOfficeId == null) {
                return new CreateClinicResponse(false, "Failed to determine MoH office for current user");
            }

            // Convert DTO to entity
            Clinic clinic = new Clinic();
            clinic.setTitle(request.getTitle());
            clinic.setDate(request.getDate());
            clinic.setStartTime(request.getStartTime());
            clinic.setDoctorName(request.getDoctorName());
            clinic.setLocation(request.getLocation());
            clinic.setRegisteredMotherIds(request.getRegisteredMotherIds() != null ? 
                new ArrayList<>(request.getRegisteredMotherIds()) : new ArrayList<>());
            clinic.setMaxCapacity(request.getMaxCapacity());
            clinic.setNotes(request.getNotes());
            clinic.setUnitIds(request.getUnitIds() != null ? 
                new ArrayList<>(request.getUnitIds()) : new ArrayList<>());

            clinic.setMohOfficeId(mohOfficeId);
            clinic.setCreatedAt(LocalDateTime.now());
            clinic.setUpdatedAt(LocalDateTime.now());
            clinic.setActive(true);
            Clinic savedClinic = clinicRepository.save(clinic);
            logger.info("Saved clinic: {}", savedClinic);
            return new CreateClinicResponse(true, "Clinic created successfully", savedClinic);
        } catch (Exception e) {
            return new CreateClinicResponse(false, "Failed to create clinic: " + e.getMessage());
        }
    }

    public Clinic updateClinic(String id, Clinic clinic) {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return null;
        }

        Optional<Clinic> existingClinicOpt = clinicRepository.findById(id);
        if (!existingClinicOpt.isPresent()) {
            return null;
        }

        Clinic existingClinic = existingClinicOpt.get();
        // Verify that the clinic belongs to this MoH office
        if (!existingClinic.getMohOfficeId().equals(mohOfficeId)) {
            logger.warn("User attempted to update clinic from another MoH office: {}", id);
            return null;
        }

        existingClinic.setTitle(clinic.getTitle());
        existingClinic.setDate(clinic.getDate());
        existingClinic.setStartTime(clinic.getStartTime());
        existingClinic.setDoctorName(clinic.getDoctorName());
        existingClinic.setLocation(clinic.getLocation());
        existingClinic.setRegisteredMotherIds(clinic.getRegisteredMotherIds());
        existingClinic.setMaxCapacity(clinic.getMaxCapacity());
        existingClinic.setNotes(clinic.getNotes());
        existingClinic.setUnitIds(clinic.getUnitIds());
        existingClinic.setUpdatedAt(LocalDateTime.now());
        return clinicRepository.save(existingClinic);
    }

    public Clinic updateClinic(String id, UpdateClinicRequest request) {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return null;
        }

        Optional<Clinic> existingClinicOpt = clinicRepository.findById(id);
        if (!existingClinicOpt.isPresent()) {
            return null;
        }

        Clinic existingClinic = existingClinicOpt.get();
        // Verify that the clinic belongs to this MoH office
        if (!existingClinic.getMohOfficeId().equals(mohOfficeId)) {
            logger.warn("User attempted to update clinic from another MoH office: {}", id);
            return null;
        }

        // Update fields from DTO
        if (request.getTitle() != null) {
            existingClinic.setTitle(request.getTitle());
        }
        if (request.getDate() != null) {
            existingClinic.setDate(request.getDate());
        }
        if (request.getStartTime() != null) {
            existingClinic.setStartTime(request.getStartTime());
        }
        if (request.getDoctorName() != null) {
            existingClinic.setDoctorName(request.getDoctorName());
        }
        if (request.getLocation() != null) {
            existingClinic.setLocation(request.getLocation());
        }
        if (request.getRegisteredMotherIds() != null) {
            existingClinic.setRegisteredMotherIds(request.getRegisteredMotherIds());
        }
        if (request.getMaxCapacity() != null) {
            existingClinic.setMaxCapacity(request.getMaxCapacity());
        }
        if (request.getNotes() != null) {
            existingClinic.setNotes(request.getNotes());
        }
        if (request.getUnitIds() != null) {
            existingClinic.setUnitIds(request.getUnitIds());
        }
        existingClinic.setUpdatedAt(LocalDateTime.now());
        return clinicRepository.save(existingClinic);
    }

    /**
     * Delete (soft delete) a clinic, ensuring it belongs to the current user's MoH
     * office
     */
    public boolean deleteClinic(String id) {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return false;
        }

        Optional<Clinic> clinicOpt = clinicRepository.findById(id);
        if (!clinicOpt.isPresent()) {
            return false;
        }

        Clinic clinic = clinicOpt.get();
        // Verify that the clinic belongs to this MoH office
        if (!clinic.getMohOfficeId().equals(mohOfficeId)) {
            logger.warn("User attempted to delete clinic from another MoH office: {}", id);
            return false;
        }

        clinic.setActive(false);
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicRepository.save(clinic);
        return true;
    }

    /**
     * Helper method to get the current user's MoH office ID from the security
     * context
     */
    private String getCurrentUserMohOfficeId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("No authentication context available");
                return null;
            }

            String firebaseUid = authentication.getName();
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);

            if (mohUser == null) {
                logger.error("No MoH user found for Firebase UID: {}", firebaseUid);
                return null;
            }

            return mohUser.getOfficeId();
        } catch (Exception e) {
            logger.error("Error getting current user's MoH office ID", e);
            return null;
        }
    }
}
