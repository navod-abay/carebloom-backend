package com.example.carebloom.services.admin;

import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.dto.admin.CreateUserRequest;
import com.example.carebloom.dto.admin.UpdateUserRequest;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.repositories.MOHOfficeRepository;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MoHOfficeUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(MoHOfficeUserService.class);
    
    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;
    
    @Autowired
    private MOHOfficeRepository mohOfficeRepository;
    
    @Autowired
    private FirebaseUserService firebaseUserService;
    
    public List<MoHOfficeUser> getUsersByOfficeId(String officeId) {
        // Verify office exists
        if (!mohOfficeRepository.existsById(officeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MoH Office not found");
        }
        
        return mohOfficeUserRepository.findByOfficeId(officeId);
    }
    
    public MoHOfficeUser createUser(CreateUserRequest request, String createdBy) {
        // Verify office exists
        if (!mohOfficeRepository.existsById(request.getOfficeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MoH Office not found");
        }
        
        MoHOfficeUser user = new MoHOfficeUser();
        user.setOfficeId(request.getOfficeId());
        user.setEmail(request.getEmail());
        user.setState("pending"); // User needs to be approved before activation
        user.setCreatedBy(createdBy);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Successfully created user {} in pending state", request.getEmail());
        
        return mohOfficeUserRepository.save(user);
    }
    
    public MoHOfficeUser updateUser(String officeId, String userId, UpdateUserRequest request) {
        MoHOfficeUser user = mohOfficeUserRepository.findByOfficeIdAndId(officeId, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in this office");
        }
        
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getState() != null) {
            user.setState(request.getState());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return mohOfficeUserRepository.save(user);
    }
    
    public MoHOfficeUser revokeUser(String officeId, String userId) {
        MoHOfficeUser user = mohOfficeUserRepository.findByOfficeIdAndId(officeId, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in this office");
        }
        
        user.setState("revoked");
        user.setUpdatedAt(LocalDateTime.now());
        return mohOfficeUserRepository.save(user);
    }
    
    public void deleteUser(String officeId, String userId) {
        MoHOfficeUser user = mohOfficeUserRepository.findByOfficeIdAndId(officeId, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in this office");
        }
        
        mohOfficeUserRepository.delete(user);
    }
    
    public MoHOfficeUser approveUser(String officeId, String userId) {
        MoHOfficeUser user = mohOfficeUserRepository.findByOfficeIdAndId(officeId, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in this office");
        }
        
        if (!"pending".equals(user.getState())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in pending state");
        }
        
        try {
            // Create Firebase user account
            UserRecord firebaseUser = firebaseUserService.createFirebaseUser(user.getEmail());
            
            // Update user state to approved/active
            user.setFirebaseUid(firebaseUser.getUid());
            user.setState("active");
            user.setUpdatedAt(LocalDateTime.now());
            
            logger.info("Successfully approved user {} and created Firebase account with UID: {}", 
                user.getEmail(), firebaseUser.getUid());
            
            return mohOfficeUserRepository.save(user);
            
        } catch (Exception e) {
            logger.error("Failed to approve user {}: {}", user.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to approve user: " + e.getMessage());
        }
    }
}
