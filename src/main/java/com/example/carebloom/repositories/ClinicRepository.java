package com.example.carebloom.repositories;

import com.example.carebloom.models.Clinic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClinicRepository extends MongoRepository<Clinic, String> {
    List<Clinic> findByIsActiveTrue();

    List<Clinic> findByDateAndIsActiveTrue(String date);

    List<Clinic> findByMohOfficeIdAndIsActiveTrue(String mohOfficeId);

    List<Clinic> findByMohOfficeIdAndDateAndIsActiveTrue(String mohOfficeId, String date);

    List<Clinic> findByUserIdAndIsActiveTrue(String userId);

    List<Clinic> findByUserIdAndDateAndIsActiveTrue(String userId, String date);

    // Find upcoming clinics for mother's MoH office
    List<Clinic> findByMohOfficeIdAndIsActiveTrueOrderByDateAsc(String mohOfficeId);
    
    // Find clinics where mother is registered
    List<Clinic> findByRegisteredMotherIdsContainingAndIsActiveTrueOrderByDateAsc(String motherId);

}