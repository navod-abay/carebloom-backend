package com.example.carebloom.repositories;

import com.example.carebloom.models.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VendorRepository extends MongoRepository<Vendor, String> {
    Vendor findByFirebaseUid(String firebaseUid);
    Vendor findByEmail(String email);
    Vendor findByBusinessRegistrationNumber(String businessRegistrationNumber);
}
