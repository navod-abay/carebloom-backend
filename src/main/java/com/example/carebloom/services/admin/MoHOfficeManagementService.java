package com.example.carebloom.services.admin;

import com.example.carebloom.dto.admin.CreateMoHOfficeRequest;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.MOHOfficeRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MoHOfficeManagementService {

    private static final Logger logger = LoggerFactory.getLogger(MoHOfficeManagementService.class);

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;
    
    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;
    
    @Autowired
    private FirebaseUserService firebaseUserService;

    @Transactional
    public MOHOffice createMoHOffice(CreateMoHOfficeRequest request) {
        // Create the MoH Office
        MOHOffice office = new MOHOffice();
        office.setDivisionalSecretariat(request.getDivisionalSecretariat());
        office.setDistrict(request.getDistrict()); // Add district field
        office.setAddress(request.getAddress());        
        office.setOfficerInCharge(request.getOfficerInCharge());
        office.setContactNumber(request.getContactNumber());
        office.setAdminEmail(request.getAdminEmail());

        // Save the office first to get the ID
        MOHOffice savedOffice = mohOfficeRepository.save(office);
        
        // Create an admin user account for this office
        createAdminAccount(savedOffice, request.getAdminEmail());
        
        logger.info("Created MoH Office: {} in district: {} with admin account: {}", 
            savedOffice.getDivisionalSecretariat(), savedOffice.getDistrict(), request.getAdminEmail());
        
        return savedOffice;
    }
    
    private void createAdminAccount(MOHOffice office, String adminEmail) {
        try {
            // Create Firebase user account first
            UserRecord firebaseUser = firebaseUserService.createFirebaseUser(adminEmail);

            logger.info("Firebase user created for MoH Office: {} with email: {}, with UID: {}", 
                office.getDivisionalSecretariat(), adminEmail, firebaseUser.getUid());
            
            // Create MoH Office User with Firebase UID
            MoHOfficeUser adminUser = new MoHOfficeUser();
            adminUser.setOfficeId(office.getId());
            adminUser.setEmail(adminEmail);
            adminUser.setName(office.getOfficerInCharge()); // Use officer in charge name as default
            adminUser.setAccountType("admin");
            adminUser.setRole("MOH_OFFICE_ADMIN"); // Set role as MOH_OFFICE_ADMIN
            adminUser.setFirebaseUid(firebaseUser.getUid()); // Store Firebase UID
            adminUser.setState("active"); // Admin account is active immediately
            adminUser.setCreatedBy("system"); // Created by system during office creation
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());
            
            mohOfficeUserRepository.save(adminUser);
            logger.info("Created admin account for office: {} with email: {} and Firebase UID: {}", 
                office.getDivisionalSecretariat(), adminEmail, firebaseUser.getUid());
                
        } catch (Exception e) {
            logger.error("Failed to create admin account for office {}: {}", 
                office.getDivisionalSecretariat(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to create admin account: " + e.getMessage());
        }
    }

    public List<MOHOffice> getAllMoHOffices() {
        return mohOfficeRepository.findAll();
    }

    public MOHOffice getMoHOfficeById(String id) {
        return mohOfficeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "MOH Office not found with id: " + id
            ));
    }

    public void deleteMoHOffice(String id) {
        MOHOffice office = getMoHOfficeById(id); // This will throw 404 if not found
        mohOfficeRepository.delete(office);
    }
}
