package com.example.carebloom.repositories;

import com.example.carebloom.models.MOHOffice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MOHOfficeRepository extends MongoRepository<MOHOffice, String> {
    MOHOffice findByFirebaseUid(String firebaseUid);
    MOHOffice findByEmail(String email);
    MOHOffice findByRegistrationNumber(String registrationNumber);
}
