package com.example.carebloom.services;

import com.example.carebloom.dto.LocationRegistrationRequest;
import com.example.carebloom.dto.PersonalRegistrationRequest;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.UserProfile;
import com.example.carebloom.repositories.MotherRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private MotherRepository motherRepository;

    private UserProfile createUserProfile(Mother mother) {
        UserProfile profile = new UserProfile();
        profile.setId(mother.getId());
        profile.setName(mother.getName());
        profile.setEmail(mother.getEmail());
        profile.setRole("mother");
        profile.setRegistrationStatus(mother.getRegistrationStatus());
        return profile;
    }

    public UserProfile verifyIdToken(String idToken) throws Exception {
        String token = idToken.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        
        Mother mother = motherRepository.findByFirebaseUid(decodedToken.getUid());
        if (mother == null) {
            throw new RuntimeException("User not found");
        }

        return createUserProfile(mother);
    }

    public UserProfile registerMother(String idToken, String email) throws Exception {
        String token = idToken.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        
        // Check if user already exists
        Mother existingMother = motherRepository.findByFirebaseUid(decodedToken.getUid());
        if (existingMother != null) {
            throw new RuntimeException("User already registered");
        }

        // Create new mother
        Mother mother = new Mother();
        mother.setEmail(email);
        mother.setFirebaseUid(decodedToken.getUid());
        mother.setRegistrationStatus("initial");
        
        // Save to MongoDB
        mother = motherRepository.save(mother);
        
        return createUserProfile(mother);
    }
    
    public UserProfile skipLocation(String idToken) throws Exception {
        String token = idToken.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        
        Mother mother = motherRepository.findByFirebaseUid(decodedToken.getUid());
        if (mother == null) {
            throw new RuntimeException("User not found");
        }
        
        if (!"location_pending".equals(mother.getRegistrationStatus())) {
            throw new RuntimeException("Invalid registration step");
        }
        
        mother.setRegistrationStatus("normal");
        mother = motherRepository.save(mother);
        return createUserProfile(mother);
    }

    public UserProfile updateLocation(String idToken, LocationRegistrationRequest request) throws Exception {
        String token = idToken.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        
        Mother mother = motherRepository.findByFirebaseUid(decodedToken.getUid());
        if (mother == null) {
            throw new RuntimeException("User not found");
        }
        
        // Allow location update if status is location_pending or normal
        if (!"location_pending".equals(mother.getRegistrationStatus()) &&
            !"normal".equals(mother.getRegistrationStatus())) {
            throw new RuntimeException("Invalid registration step");
        }
        
        mother.setDistrict(request.getDistrict());
        mother.setMohOfficeId(request.getMohOfficeId()); // Updated to use mohOfficeId
        mother.setRecordNumber(request.getRecordNumber());
        mother.setAreaMidwifeId(request.getAreaMidwifeId()); // Updated to use areaMidwifeId
        mother.setUnitId(request.getUnitId()); // Updated to use unitId
        mother.setRegistrationStatus("complete");
        
        mother = motherRepository.save(mother);
        return createUserProfile(mother);
    }
    
    public UserProfile updatePersonalInfo(String idToken, PersonalRegistrationRequest request) throws Exception {
        String token = idToken.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        
        Mother mother = motherRepository.findByFirebaseUid(decodedToken.getUid());
        if (mother == null) {
            throw new RuntimeException("User not found");
        }
        
        // Allow personal info update if status is initial
        if (!"initial".equals(mother.getRegistrationStatus())) {
            throw new RuntimeException("Invalid registration step");
        }
        
        mother.setName(request.getName());
        mother.setDueDate(request.getDueDate());
        mother.setPhone(request.getPhone());
        mother.setAddress(request.getAddress());
        mother.setRegistrationStatus("location_pending");
        
        mother = motherRepository.save(mother);
        return createUserProfile(mother);
    }
    
    public UserProfile getProfileByFirebaseUid(String firebaseUid) {
        Mother mother = motherRepository.findByFirebaseUid(firebaseUid);
        if (mother == null) {
            return null;
        }
        return createUserProfile(mother);
    }
}
