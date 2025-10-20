package com.example.carebloom.repositories;

import com.example.carebloom.models.FieldVisit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface FieldVisitRepository extends MongoRepository<FieldVisit, String> {
    
    @Query("{'midwifeId': ?0}")
    List<FieldVisit> findByMidwifeId(String midwifeId);
    
    @Query("{'midwifeId': ?0, 'date': ?1}")
    List<FieldVisit> findByMidwifeIdAndDate(String midwifeId, String date);
    
    @Query("{'midwifeId': ?0, 'status': ?1}")
    List<FieldVisit> findByMidwifeIdAndStatus(String midwifeId, String status);

    @Query("{'midwifeId': ?0, 'status': { $in: ?1 }}")
    List<FieldVisit> findByMidwifeIdAndStatusIn(String midwifeId, List<String> statuses);

    @Query("{'status': ?0, 'createdAt': { $gte: ?1, $lte: ?2 }}")
    List<FieldVisit> findByStatusAndCreatedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("{'status': { $in: ?0 }}")
    List<FieldVisit> findByStatusIn(List<String> statuses);
}
