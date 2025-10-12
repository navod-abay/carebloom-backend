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
import com.example.carebloom.services.QueueService;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MoHClinicService {
    private static final Logger logger = LoggerFactory.getLogger(MoHClinicService.class);

    @Autowired private ClinicRepository clinicRepository;
    @Autowired private MoHOfficeUserRepository mohOfficeUserRepository;
    @Autowired private MotherRepository motherRepository;
    @Autowired private QueueService queueService;

    public Object getQueueStatus(String clinicId) {
        try {
            return queueService.getQueueStatus(clinicId);
        } catch (Exception e) {
            logger.error("Error getting queue status for clinic {}: {}", clinicId, e.getMessage());
            return java.util.Map.of("success", false, "error", "Failed to get queue status: " + e.getMessage());
        }
    }

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
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                // For testing purposes, return a default MoH office ID
                logger.warn("No authenticated user found, using default MoH office ID for testing");
                return "688072f834faf68abe8c8467"; // Default MoH office ID for testing
            }
            String firebaseUid = authentication.getName();
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
            return mohUser != null ? mohUser.getOfficeId() : "688072f834faf68abe8c8467"; // Fallback to default
        } catch (Exception e) {
            logger.error("Error getting current user's MoH office ID, using fallback", e);
            return "688072f834faf68abe8c8467"; // Fallback MoH office ID for testing
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

    // ===== Queue Management Methods =====

    public Object startQueue(String clinicId) {
        try {
            // Ensure the clinic belongs to current user's MoH office
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) {
                throw new IllegalArgumentException("Unable to determine MoH office");
            }
            
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty()) {
                throw new IllegalArgumentException("Clinic not found");
            }
            
            if (!clinicOpt.get().getMohOfficeId().equals(currentMohOfficeId)) {
                throw new IllegalArgumentException("Access denied to this clinic");
            }
            
            return queueService.startQueue(clinicId);
        } catch (Exception e) {
            logger.error("Error starting queue for clinic {}: {}", clinicId, e.getMessage());
            throw e;
        }
    }

    public java.util.Map<String, Object> closeQueue(String clinicId, boolean force) {
        try {
            // Ensure the clinic belongs to current user's MoH office
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) {
                throw new IllegalArgumentException("Unable to determine MoH office");
            }
            
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty()) {
                throw new IllegalArgumentException("Clinic not found");
            }
            
            if (!clinicOpt.get().getMohOfficeId().equals(currentMohOfficeId)) {
                throw new IllegalArgumentException("Access denied to this clinic");
            }
            
            return queueService.closeQueue(clinicId, force);
        } catch (Exception e) {
            logger.error("Error closing queue for clinic {}: {}", clinicId, e.getMessage());
            throw e;
        }
    }

    public Object addPatientToQueue(String clinicId, Object patient) {
        try {
            // Ensure the clinic belongs to current user's MoH office
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) {
                throw new IllegalArgumentException("Unable to determine MoH office");
            }
            
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty()) {
                throw new IllegalArgumentException("Clinic not found");
            }
            
            if (!clinicOpt.get().getMohOfficeId().equals(currentMohOfficeId)) {
                throw new IllegalArgumentException("Access denied to this clinic");
            }
            
            // Convert the patient object to Map if needed
            if (patient instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> patientMap = (java.util.Map<String, Object>) patient;
                return queueService.addPatientToQueue(clinicId, patientMap);
            } else {
                throw new IllegalArgumentException("Invalid patient data format");
            }
        } catch (Exception e) {
            logger.error("Error adding patient to queue for clinic {}: {}", clinicId, e.getMessage());
            throw e;
        }
    }

    public Object updateQueueSettings(String clinicId, Object settings) {
        try {
            // Ensure the clinic belongs to current user's MoH office
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) {
                throw new IllegalArgumentException("Unable to determine MoH office");
            }
            
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty()) {
                throw new IllegalArgumentException("Clinic not found");
            }
            
            if (!clinicOpt.get().getMohOfficeId().equals(currentMohOfficeId)) {
                throw new IllegalArgumentException("Access denied to this clinic");
            }
            
            // Convert the settings object to proper DTO if needed
            return queueService.updateQueueSettings(clinicId, (com.example.carebloom.dto.queue.QueueSettingsDto) settings);
        } catch (Exception e) {
            logger.error("Error updating queue settings for clinic {}: {}", clinicId, e.getMessage());
            throw e;
        }
    }

    public Object processNextPatient(String clinicId) {
        try {
            // Ensure the clinic belongs to current user's MoH office
            String currentMohOfficeId = getCurrentUserMohOfficeId();
            if (currentMohOfficeId == null) {
                throw new IllegalArgumentException("Unable to determine MoH office");
            }
            
            Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
            if (clinicOpt.isEmpty()) {
                throw new IllegalArgumentException("Clinic not found");
            }
            
            if (!clinicOpt.get().getMohOfficeId().equals(currentMohOfficeId)) {
                throw new IllegalArgumentException("Access denied to this clinic");
            }
            
            return queueService.processNextPatient(clinicId);
        } catch (Exception e) {
            logger.error("Error processing next patient for clinic {}: {}", clinicId, e.getMessage());
            throw e;
        }
    }
}
