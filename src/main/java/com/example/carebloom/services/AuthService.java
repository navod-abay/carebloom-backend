package com.example.carebloom.services;

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

    public UserProfile verifyIdToken(String idToken) throws Exception {
        String token = idToken.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        
        Mother mother = motherRepository.findByFirebaseUid(decodedToken.getUid());
        if (mother == null) {
            throw new RuntimeException("User not found");
        }

        UserProfile profile = new UserProfile();
        profile.setId(mother.getId());
        profile.setName(mother.getName());
        profile.setEmail(mother.getEmail());
        profile.setRole("mother");
        
        return profile;
    }

    public void registerMother(String idToken, String email) throws Exception {
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
        
        // Save to MongoDB
        motherRepository.save(mother);
    }
}
