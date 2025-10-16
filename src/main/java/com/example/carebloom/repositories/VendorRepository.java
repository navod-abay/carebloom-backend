package com.example.carebloom.repositories;

import com.example.carebloom.models.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface VendorRepository extends MongoRepository<Vendor, String> {
    Vendor findByFirebaseUid(String firebaseUid);

    Vendor findByEmail(String email);

    Vendor findByBusinessRegistrationNumber(String businessRegistrationNumber);

    List<Vendor> findByState(String state);

    boolean existsByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    // Analytics queries for vendor registrations
    @Query("{'created_at': {$gte: ?0, $lte: ?1}}")
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Fallback query using createdAt field (without @Field mapping)
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    long countByCreatedAtBetweenFallback(LocalDateTime startDate, LocalDateTime endDate);

    // Get total count of all vendors (for fallback when no date filtering works)
    @Query(value = "{}", count = true)
    long getTotalVendorCount();
}
