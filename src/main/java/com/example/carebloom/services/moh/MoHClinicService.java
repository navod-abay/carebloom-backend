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

    public List<Clinic> getAllClinicsByUserId(String userId) {
        return clinicRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public List<Clinic> getClinicsByDate(String date) {
        return clinicRepository.findByDateAndIsActiveTrue(date);
    }

    public Optional<Clinic> getClinicById(String id) {
        return clinicRepository.findById(id);
    }

    public CreateClinicResponse createClinic(Clinic clinic, String userId) {
        try {
            clinic.setUserId(userId);
            clinic.setCreatedAt(LocalDateTime.now());
            clinic.setUpdatedAt(LocalDateTime.now());
            clinic.setActive(true);
            Clinic savedClinic = clinicRepository.save(clinic);
            return new CreateClinicResponse(true, "Clinic created successfully", savedClinic);
        } catch (Exception e) {
            return new CreateClinicResponse(false, "Failed to create clinic: " + e.getMessage());
        }
    }

    public Clinic updateClinic(String id, Clinic clinic) {
        Optional<Clinic> existingClinic = clinicRepository.findById(id);
        if (existingClinic.isPresent()) {
            Clinic updatedClinic = existingClinic.get();
            updatedClinic.setTitle(clinic.getTitle());
            updatedClinic.setDate(clinic.getDate());
            updatedClinic.setStartTime(clinic.getStartTime());
            updatedClinic.setDoctorName(clinic.getDoctorName());
            updatedClinic.setLocation(clinic.getLocation());
            updatedClinic.setUpdatedAt(LocalDateTime.now());
            return clinicRepository.save(updatedClinic);
        }
        return null;
    }

    public boolean deleteClinic(String id) {
        Optional<Clinic> clinic = clinicRepository.findById(id);
        if (clinic.isPresent()) {
            Clinic existingClinic = clinic.get();
            existingClinic.setActive(false);
            existingClinic.setUpdatedAt(LocalDateTime.now());
            clinicRepository.save(existingClinic);
            return true;
        }
        return false;
    }

    public List<Clinic> getClinicsByUserId(String userId) {
        return clinicRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public List<Clinic> getClinicsByUserIdAndDate(String userId, String date) {
        return clinicRepository.findByUserIdAndDateAndIsActiveTrue(userId, date);
    }
}
