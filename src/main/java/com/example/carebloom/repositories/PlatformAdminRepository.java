package com.example.carebloom.repositories;

import com.example.carebloom.models.PlatformAdmin;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlatformAdminRepository extends MongoRepository<PlatformAdmin, String> {
    PlatformAdmin findByFirebaseUid(String firebaseUid);
    PlatformAdmin findByEmail(String email);
}
