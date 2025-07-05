package com.example.carebloom.services.moh;

import com.example.carebloom.models.Clinic;
import com.example.carebloom.repositories.ClinicRepository;
import com.example.carebloom.dto.CreateClinicResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MoHClinicService {

    @Autowired
    private ClinicRepository clinicRepository;

    public List<Clinic> getAllClinics() {
        return clinicRepository.findByIsActiveTrue();
    }

    public List<Clinic> getClinicsByCategory(String category) {
        return clinicRepository.findByCategoryAndIsActiveTrue(category);
    }

    public Optional<Clinic> getClinicById(String id) {
        return clinicRepository.findById(id);
    }

    public CreateClinicResponse createClinic(Clinic clinic) {
        try {
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
            updatedClinic.setCategory(clinic.getCategory());
            updatedClinic.setDescription(clinic.getDescription());
            updatedClinic.setLocation(clinic.getLocation());
            updatedClinic.setStartTime(clinic.getStartTime());
            updatedClinic.setEndTime(clinic.getEndTime());
            updatedClinic.setCapacity(clinic.getCapacity());
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
}
