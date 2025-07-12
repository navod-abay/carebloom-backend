package com.example.carebloom.services.moh;

import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.ClinicRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.dto.CreateClinicResponse;
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

@Service
public class MoHClinicService {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

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
     * Create a new clinic for the current user's MoH office
     */
    public CreateClinicResponse createClinic(Clinic clinic) {
        try {
            String mohOfficeId = getCurrentUserMohOfficeId();
            if (mohOfficeId == null) {
                return new CreateClinicResponse(false, "Failed to determine MoH office for current user");
            }

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
