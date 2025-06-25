package com.example.carebloom.repositories;

import com.example.carebloom.models.Mother;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MotherRepository extends MongoRepository<Mother, String> {
    Mother findByFirebaseUid(String firebaseUid);
}
