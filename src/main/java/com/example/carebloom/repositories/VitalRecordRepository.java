package com.example.carebloom.repositories;

import com.example.carebloom.models.VitalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VitalRecordRepository extends MongoRepository<VitalRecord, String> {
    Page<VitalRecord> findByMotherId(String motherId, Pageable pageable);
    Page<VitalRecord> findByMotherIdAndRecordedDateBetween(String motherId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    Optional<VitalRecord> findTopByMotherIdOrderByRecordedDateDesc(String motherId);
    List<VitalRecord> findByMotherIdOrderByRecordedDateDesc(String motherId);
    List<VitalRecord> findByMotherId(String motherId);
}
