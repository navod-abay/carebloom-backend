package com.example.carebloom.repositories;

import com.example.carebloom.models.MOHOffice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface MOHOfficeRepository extends MongoRepository<MOHOffice, String> {
    MOHOffice findByadminEmail(String adminEmail);

    MOHOffice findByid(String id);

    List<MOHOffice> findByDistrict(String district);

    // Get count of MOH offices within a date range
    @Query(value = "{ 'createdAt': { $gte: ?0, $lt: ?1 } }", count = true)
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Get count of MOH offices by district within a date range
    @Query(value = "{ 'district': ?0, 'createdAt': { $gte: ?1, $lt: ?2 } }", count = true)
    long countByDistrictAndCreatedAtBetween(String district, LocalDateTime startDate, LocalDateTime endDate);
    
    // Get all distinct districts within date range
    @Query(value = "{ 'createdAt': { $gte: ?0, $lt: ?1 } }", fields = "{ 'district': 1 }")
    List<MOHOffice> findDistinctDistrictsByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
