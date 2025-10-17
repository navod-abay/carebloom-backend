package com.example.carebloom.repositories;

import com.example.carebloom.models.Mother;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MotherRepository extends MongoRepository<Mother, String> {
    Mother findByFirebaseUid(String firebaseUid);

    List<Mother> findByMohOfficeId(String mohOfficeId); // Find mothers by MOH Office ID

    List<Mother> findByDistrict(String district); // Find mothers by district

    List<Mother> findByAreaMidwifeId(String areaMidwifeId); // Find mothers assigned to a specific midwife

    List<Mother> findByUnitId(String UnitId);

    // Get total count of mothers with accepted statuses
    @Query(value = "{ 'registrationStatus': { $in: ['complete', 'normal', 'accepted'] } }", count = true)
    long totalMothersCount();

    // Get count of mothers with accepted statuses within a date range
    @Query(value = "{ 'registrationStatus': { $in: ['complete', 'normal', 'accepted'] }, 'createdAt': { $gte: ?0, $lt: ?1 } }", count = true)
    long countByAcceptedStatusesAndCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Get count by specific registration status within date range
    @Query(value = "{ 'registrationStatus': ?0, 'createdAt': { $gte: ?1, $lt: ?2 } }", count = true)
    long countByRegistrationStatusAndCreatedAtBetween(String registrationStatus, LocalDateTime startDate,
            LocalDateTime endDate);

    // Get count by specific registration status (all time)
    @Query(value = "{ 'registrationStatus': ?0 }", count = true)
    long countByRegistrationStatus(String registrationStatus);

    // Get count by district with accepted statuses within date range
    @Query(value = "{ 'registrationStatus': { $in: ['complete', 'normal', 'accepted'] }, 'district': ?0, 'createdAt': { $gte: ?1, $lt: ?2 } }", count = true)
    long countByDistrictAndAcceptedStatusesAndCreatedAtBetween(String district, LocalDateTime startDate,
            LocalDateTime endDate);

    // Get all distinct districts with accepted statuses within date range
    @Query(value = "{ 'registrationStatus': { $in: ['complete', 'normal', 'accepted'] }, 'createdAt': { $gte: ?0, $lt: ?1 } }", fields = "{ 'district': 1 }")
    List<Mother> findDistinctDistrictsByAcceptedStatusesAndCreatedAtBetween(LocalDateTime startDate,
            LocalDateTime endDate);
}
