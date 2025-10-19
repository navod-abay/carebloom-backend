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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MoHClinicService {
    private static final Logger logger = LoggerFactory.getLogger(MoHClinicService.class);

    @Autowired private ClinicRepository clinicRepository;
    @Autowired private MoHOfficeUserRepository mohOfficeUserRepository;
    @Autowired private MotherRepository motherRepository;

    // Queue methods moved to NewQueueService

    public List<Clinic> getAllClinicsByMohOffice() {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return Collections.emptyList();
        }
        return clinicRepository.findByMohOfficeIdAndIsActiveTrue(mohOfficeId);
    }

    public List<ClinicWithMothersDto> getAllClinicsByMohOfficeWithMothers() {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) {
            logger.error("Failed to get MoH office ID for current user");
            return Collections.emptyList();
        }
        List<Clinic> clinics = clinicRepository.findByMohOfficeIdAndIsActiveTrue(mohOfficeId);
        return clinics.stream().map(this::populateClinicWithMothers).collect(Collectors.toList());
    }

    private ClinicWithMothersDto populateClinicWithMothers(Clinic clinic) {
        List<Mother> mothers = new ArrayList<>();
        if (clinic.getRegisteredMotherIds() != null && !clinic.getRegisteredMotherIds().isEmpty()) {
            try {
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
        if (clinicOpt.isEmpty()) return Optional.empty();
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) return Optional.empty();
        Clinic clinic = clinicOpt.get();
        return clinic.getMohOfficeId().equals(mohOfficeId) ? Optional.of(clinic) : Optional.empty();
    }

    public Optional<ClinicWithMothersDto> getClinicByIdWithMothers(String id) {
        Optional<Clinic> clinicOpt = clinicRepository.findById(id);
        if (clinicOpt.isEmpty()) return Optional.empty();
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) return Optional.empty();
        Clinic clinic = clinicOpt.get();
        return clinic.getMohOfficeId().equals(mohOfficeId)
            ? Optional.of(populateClinicWithMothers(clinic))
            : Optional.empty();
    }

    public List<Mother> getAvailableMothersForClinic() {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) return Collections.emptyList();
        try {
            List<Mother> mothers = motherRepository.findByMohOfficeId(mohOfficeId);
            logger.info("Found {} available mothers for MoH office {}", mothers.size(), mohOfficeId);
            return mothers;
        } catch (Exception e) {
            logger.error("Error fetching available mothers for MoH office: {}", mohOfficeId, e);
            return Collections.emptyList();
        }
    }

    public CreateClinicResponse createClinic(CreateClinicRequest request) {
        try {
            String mohOfficeId = getCurrentUserMohOfficeId();
            if (mohOfficeId == null) {
                return new CreateClinicResponse(false, "Failed to determine MoH office for current user");
            }
            Clinic clinic = new Clinic();
            clinic.setTitle(request.getTitle());
            clinic.setDate(request.getDate());
            clinic.setStartTime(request.getStartTime());
            clinic.setDoctorName(request.getDoctorName());
            clinic.setLocation(request.getLocation());
            clinic.setRegisteredMotherIds(request.getRegisteredMotherIds() != null ? new ArrayList<>(request.getRegisteredMotherIds()) : new ArrayList<>());
            clinic.setMaxCapacity(request.getMaxCapacity());
            clinic.setNotes(request.getNotes());
            clinic.setUnitIds(request.getUnitIds() != null ? new ArrayList<>(request.getUnitIds()) : new ArrayList<>());
            clinic.setMohOfficeId(mohOfficeId);
            clinic.setCreatedAt(LocalDateTime.now());
            clinic.setUpdatedAt(LocalDateTime.now());
            clinic.setActive(true);
            
            // Populate addedMothers array with full mother objects
            if (request.getRegisteredMotherIds() != null && !request.getRegisteredMotherIds().isEmpty()) {
                List<Mother> mothers = motherRepository.findAllById(request.getRegisteredMotherIds());
                List<com.example.carebloom.models.AddedMother> addedMothers = new ArrayList<>();
                
                for (Mother mother : mothers) {
                    // Only add mothers from the same MOH office
                    if (mohOfficeId.equals(mother.getMohOfficeId())) {
                        com.example.carebloom.models.AddedMother am = new com.example.carebloom.models.AddedMother();
                        am.setId(mother.getId());
                        am.setName(mother.getName());
                        am.setEmail(mother.getEmail());
                        am.setPhone(mother.getPhone());
                        am.setDueDate(mother.getDueDate());
                        am.setAge(25); // Default age, should be updated with actual age
                        am.setRecordNumber(mother.getRecordNumber());
                        addedMothers.add(am);
                    }
                }
                
                clinic.setAddedMothers(addedMothers);
                logger.info("Added {} mothers to clinic with record numbers populated", addedMothers.size());
            }
            
            Clinic savedClinic = clinicRepository.save(clinic);
            return new CreateClinicResponse(true, "Clinic created successfully", savedClinic);
        } catch (Exception e) {
            return new CreateClinicResponse(false, "Failed to create clinic: " + e.getMessage());
        }
    }

    public Clinic updateClinic(String id, Clinic clinic) {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) return null;
        Optional<Clinic> existingClinicOpt = clinicRepository.findById(id);
        if (existingClinicOpt.isEmpty()) return null;
        Clinic existingClinic = existingClinicOpt.get();
        if (!existingClinic.getMohOfficeId().equals(mohOfficeId)) return null;
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
        if (mohOfficeId == null) return null;
        Optional<Clinic> existingClinicOpt = clinicRepository.findById(id);
        if (existingClinicOpt.isEmpty()) return null;
        Clinic existingClinic = existingClinicOpt.get();
        if (!existingClinic.getMohOfficeId().equals(mohOfficeId)) return null;
        if (request.getTitle() != null) existingClinic.setTitle(request.getTitle());
        if (request.getDate() != null) existingClinic.setDate(request.getDate());
        if (request.getStartTime() != null) existingClinic.setStartTime(request.getStartTime());
        if (request.getDoctorName() != null) existingClinic.setDoctorName(request.getDoctorName());
        if (request.getLocation() != null) existingClinic.setLocation(request.getLocation());
        if (request.getRegisteredMotherIds() != null) existingClinic.setRegisteredMotherIds(request.getRegisteredMotherIds());
        if (request.getMaxCapacity() != null) existingClinic.setMaxCapacity(request.getMaxCapacity());
        if (request.getNotes() != null) existingClinic.setNotes(request.getNotes());
        if (request.getUnitIds() != null) existingClinic.setUnitIds(request.getUnitIds());
        existingClinic.setUpdatedAt(LocalDateTime.now());
        return clinicRepository.save(existingClinic);
    }

    public boolean deleteClinic(String id) {
        String mohOfficeId = getCurrentUserMohOfficeId();
        if (mohOfficeId == null) return false;
        Optional<Clinic> clinicOpt = clinicRepository.findById(id);
        if (clinicOpt.isEmpty()) return false;
        Clinic clinic = clinicOpt.get();
        if (!clinic.getMohOfficeId().equals(mohOfficeId)) return false;
        clinic.setActive(false);
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicRepository.save(clinic);
        return true;
    }

    private String getCurrentUserMohOfficeId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) return null;
            String firebaseUid = authentication.getName();
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
            return mohUser != null ? mohUser.getOfficeId() : null;
        } catch (Exception e) {
            logger.error("Error getting current user's MoH office ID", e);
            return null;
        }
    }

    public Clinic addMothersToClinic(String clinicId, List<String> motherIds) {
        try {
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) return null;
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty() || !clinicOpt.get().getMohOfficeId().equals(currentMohOfficeId)) return null;
            Clinic clinic = clinicOpt.get();
            List<Mother> mothers = motherRepository.findAllById(motherIds);
            List<Mother> validMothers = mothers.stream().filter(m -> currentMohOfficeId.equals(m.getMohOfficeId())).collect(Collectors.toList());
            if (clinic.getAddedMothers() == null) clinic.setAddedMothers(new ArrayList<>());
            for (Mother mother : validMothers) {
                boolean alreadyAdded = clinic.getAddedMothers().stream().anyMatch(am -> am.getId().equals(mother.getId()));
                if (!alreadyAdded) {
                    com.example.carebloom.models.AddedMother am = new com.example.carebloom.models.AddedMother();
                    am.setId(mother.getId());
                    am.setName(mother.getName());
                    am.setEmail(mother.getEmail());
                    am.setPhone(mother.getPhone());
                    am.setDueDate(mother.getDueDate());
                    am.setAge(25);
                    am.setRecordNumber(mother.getRecordNumber()); // Set the record number
                    clinic.getAddedMothers().add(am);
                }
            }
            clinic.setUpdatedAt(LocalDateTime.now());
            return clinicRepository.save(clinic);
        } catch (Exception e) {
            logger.error("Error adding mothers to clinic", e);
            return null;
        }
    }

    public Clinic removeMotherFromClinic(String clinicId, String motherId) {
        try {
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) return null;
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty() || !clinicOpt.get().getMohOfficeId().equals(currentMohOfficeId)) return null;
            Clinic clinic = clinicOpt.get();
            if (clinic.getAddedMothers() != null) clinic.getAddedMothers().removeIf(am -> am.getId().equals(motherId));
            clinic.setUpdatedAt(LocalDateTime.now());
            return clinicRepository.save(clinic);
        } catch (Exception e) {
            logger.error("Error removing mother from clinic", e);
            return null;
        }
    }

    // ===== Queue Management Methods moved to NewQueueService =====
    
    /**
     * Migration method to update record numbers for existing clinics
     * This fixes clinics where mothers were added before the recordNumber fix
     */
    public Map<String, Object> updateClinicRecordNumbers(String clinicId) {
        try {
            logger.info("Updating record numbers for clinic: {}", clinicId);
            
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) {
                return Map.of("success", false, "error", "Could not determine MoH office");
            }
            
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty()) {
                return Map.of("success", false, "error", "Clinic not found");
            }
            
            Clinic clinic = clinicOpt.get();
            if (!clinic.getMohOfficeId().equals(currentMohOfficeId)) {
                return Map.of("success", false, "error", "Access denied");
            }
            
            if (clinic.getAddedMothers() == null || clinic.getAddedMothers().isEmpty()) {
                return Map.of("success", true, "message", "No mothers to update", "updated", 0);
            }
            
            int updated = 0;
            for (com.example.carebloom.models.AddedMother addedMother : clinic.getAddedMothers()) {
                // Only update if recordNumber is null or empty
                if (addedMother.getRecordNumber() == null || addedMother.getRecordNumber().isEmpty()) {
                    // Find the mother by ID
                    Optional<Mother> motherOpt = motherRepository.findById(addedMother.getId());
                    if (motherOpt.isPresent()) {
                        Mother mother = motherOpt.get();
                        if (mother.getRecordNumber() != null && !mother.getRecordNumber().isEmpty()) {
                            addedMother.setRecordNumber(mother.getRecordNumber());
                            updated++;
                            logger.info("Updated record number for mother: {} to {}", 
                                       mother.getName(), mother.getRecordNumber());
                        }
                    }
                }
            }
            
            if (updated > 0) {
                clinic.setUpdatedAt(LocalDateTime.now());
                clinicRepository.save(clinic);
                logger.info("Updated {} record numbers for clinic: {}", updated, clinicId);
            }
            
            return Map.of(
                "success", true, 
                "message", "Record numbers updated successfully",
                "updated", updated,
                "total", clinic.getAddedMothers().size()
            );
        } catch (Exception e) {
            logger.error("Error updating clinic record numbers", e);
            return Map.of("success", false, "error", "Failed to update record numbers: " + e.getMessage());
        }
    }
}

