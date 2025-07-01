package com.example.carebloom.repositories;

import com.example.carebloom.models.Midwife;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MidwifeRepository extends MongoRepository<Midwife, String> {
    Midwife findByFirebaseUid(String firebaseUid);
    Midwife findByEmail(String email);
    Midwife findByRegistrationNumber(String registrationNumber);
}
