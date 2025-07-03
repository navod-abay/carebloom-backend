package com.example.carebloom.services.moh;

import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.dto.admin.MidwifeRequest;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MidwifeService {
    
    private static final Logger logger = LoggerFactory.getLogger(MidwifeService.class);
    
    @Autowired
    private MidwifeRepository midwifeRepository;
    
    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;
    
    /**
     * Get all midwives for the MOH office user's office
     */
    public List<Midwife> getAllMidwives(String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        return midwifeRepository.findByOfficeId(officeId);
    }
    
    /**
     * Get a specific midwife by ID (within user's office)
     */
    public Midwife getMidwifeById(String midwifeId, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        Midwife midwife = midwifeRepository.findByOfficeIdAndId(officeId, midwifeId);
        
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }
        
        return midwife;
    }
    
    /**
     * Create a new midwife
     */
    public Midwife createMidwife(MidwifeRequest request, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        
        // Validate required fields
        validateMidwifeRequest(request, true);
        
        // Check if email already exists
        if (midwifeRepository.findByEmail(request.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        
        // Check if phone already exists
        if (midwifeRepository.findByPhone(request.getPhone()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists");
        }
        
        Midwife midwife = new Midwife();
        midwife.setOfficeId(officeId);
        midwife.setName(request.getName());
        midwife.setClinic(request.getClinic());
        midwife.setSpecialization(request.getSpecialization());
        midwife.setYearsOfExperience(request.getYearsOfExperience());
        midwife.setCertifications(request.getCertifications());
        midwife.setPhone(request.getPhone());
        midwife.setEmail(request.getEmail());
        midwife.setState("active"); // MOH office can directly activate midwives
        midwife.setCreatedBy(firebaseUid);
        midwife.setCreatedAt(LocalDateTime.now());
        midwife.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Creating new midwife: {} for office: {}", request.getEmail(), officeId);
        return midwifeRepository.save(midwife);
    }
    
    /**
     * Update an existing midwife
     */
    public Midwife updateMidwife(String midwifeId, MidwifeRequest request, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        Midwife existingMidwife = midwifeRepository.findByOfficeIdAndId(officeId, midwifeId);
        
        if (existingMidwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }
        
        // Validate request
        validateMidwifeRequest(request, false);
        
        // Check email uniqueness if being updated
        if (request.getEmail() != null && !request.getEmail().equals(existingMidwife.getEmail())) {
            Midwife emailCheck = midwifeRepository.findByEmail(request.getEmail());
            if (emailCheck != null && !emailCheck.getId().equals(midwifeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }
        }
        
        // Check phone uniqueness if being updated
        if (request.getPhone() != null && !request.getPhone().equals(existingMidwife.getPhone())) {
            Midwife phoneCheck = midwifeRepository.findByPhone(request.getPhone());
            if (phoneCheck != null && !phoneCheck.getId().equals(midwifeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists");
            }
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            existingMidwife.setName(request.getName());
        }
        if (request.getClinic() != null) {
            existingMidwife.setClinic(request.getClinic());
        }
        if (request.getSpecialization() != null) {
            existingMidwife.setSpecialization(request.getSpecialization());
        }
        if (request.getYearsOfExperience() != null) {
            existingMidwife.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getCertifications() != null) {
            existingMidwife.setCertifications(request.getCertifications());
        }
        if (request.getPhone() != null) {
            existingMidwife.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            existingMidwife.setEmail(request.getEmail());
        }
        if (request.getRegistrationNumber() != null) {
            existingMidwife.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getState() != null) {
            existingMidwife.setState(request.getState());
        }
        
        existingMidwife.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Updating midwife: {} for office: {}", existingMidwife.getEmail(), officeId);
        return midwifeRepository.save(existingMidwife);
    }
    
    /**
     * Delete a midwife
     */
    public void deleteMidwife(String midwifeId, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        Midwife midwife = midwifeRepository.findByOfficeIdAndId(officeId, midwifeId);
        
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }
        
        logger.info("Deleting midwife: {} from office: {}", midwife.getEmail(), officeId);
        midwifeRepository.delete(midwife);
    }
    
    /**
     * Get midwives by clinic
     */
    public List<Midwife> getMidwivesByClinic(String clinic, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        List<Midwife> allMidwives = midwifeRepository.findByOfficeId(officeId);
        
        return allMidwives.stream()
                .filter(midwife -> clinic.equals(midwife.getClinic()))
                .toList();
    }
    
    /**
     * Get midwives by specialization
     */
    public List<Midwife> getMidwivesBySpecialization(String specialization, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        List<Midwife> allMidwives = midwifeRepository.findByOfficeId(officeId);
        
        return allMidwives.stream()
                .filter(midwife -> specialization.equals(midwife.getSpecialization()))
                .toList();
    }
    
    /**
     * Helper method to get user's office ID
     */
    private String getUserOfficeId(String firebaseUid) {
        MoHOfficeUser user = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        
        if (!"active".equals(user.getState())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }
        
        return user.getOfficeId();
    }
    
    /**
     * Validate midwife request data
     */
    private void validateMidwifeRequest(MidwifeRequest request, boolean isCreate) {
        if (isCreate || request.getName() != null) {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
            }
        }
        
        if (isCreate || request.getClinic() != null) {
            if (request.getClinic() == null || request.getClinic().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clinic is required");
            }
        }
        
        if (isCreate || request.getSpecialization() != null) {
            if (request.getSpecialization() == null || request.getSpecialization().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialization is required");
            }
        }
        
        if (isCreate || request.getPhone() != null) {
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is required");
            }
        }
        
        if (isCreate || request.getEmail() != null) {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
            }
            
            if (!request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
            }
        }
        
        if (request.getYearsOfExperience() != null && request.getYearsOfExperience() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Years of experience cannot be negative");
        }
    }
}
