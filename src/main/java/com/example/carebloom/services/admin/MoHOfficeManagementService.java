package com.example.carebloom.services.admin;

import com.example.carebloom.dto.admin.CreateMoHOfficeRequest;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.MOHOfficeRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
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

    @Transactional
    public MOHOffice createMoHOffice(CreateMoHOfficeRequest request) {
        // Create the MoH Office
        MOHOffice office = new MOHOffice();
        office.setDivisionalSecretariat(request.getDivisionalSecretariat());
        office.setAddress(request.getAddress());
        
        MOHOffice.Location location = new MOHOffice.Location();
        location.setLatitude(request.getLocation().getLatitude());
        location.setLongitude(request.getLocation().getLongitude());
        office.setLocation(location);
        
        office.setOfficerInCharge(request.getOfficerInCharge());
        office.setContactNumber(request.getContactNumber());
        office.setAdminEmail(request.getAdminEmail());

        // Save the office first to get the ID
        MOHOffice savedOffice = mohOfficeRepository.save(office);
        
        // Create an admin user account for this office
        createAdminAccount(savedOffice, request.getAdminEmail());
        
        logger.info("Created MoH Office: {} with admin account: {}", 
            savedOffice.getDivisionalSecretariat(), request.getAdminEmail());
        
        return savedOffice;
    }
    
    private void createAdminAccount(MOHOffice office, String adminEmail) {
        MoHOfficeUser adminUser = new MoHOfficeUser();
        adminUser.setOfficeId(office.getId());
        adminUser.setEmail(adminEmail);
        adminUser.setName(office.getOfficerInCharge()); // Use officer in charge name as default
        adminUser.setAccountType("admin");
        adminUser.setState("pending"); // Admin will need to be approved like other users
        adminUser.setCreatedBy("system"); // Created by system during office creation
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        
        mohOfficeUserRepository.save(adminUser);
        logger.info("Created admin account for office: {} with email: {}", 
            office.getDivisionalSecretariat(), adminEmail);
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
