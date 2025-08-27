package com.example.carebloom.repositories;

import com.example.carebloom.models.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface VendorRepository extends MongoRepository<Vendor, String> {
    Vendor findByFirebaseUid(String firebaseUid);

    Vendor findByEmail(String email);

    Vendor findByBusinessRegistrationNumber(String businessRegistrationNumber);

    List<Vendor> findByState(String state);

    boolean existsByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
}
