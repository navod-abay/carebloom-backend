package com.example.carebloom.repositories;

import com.example.carebloom.models.VisitRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitRecordRepository extends MongoRepository<VisitRecord, String> {
    Page<VisitRecord> findByMotherId(String motherId, Pageable pageable);
    Page<VisitRecord> findByMotherIdAndVisitDateBetween(String motherId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    Optional<VisitRecord> findTopByMotherIdOrderByVisitDateDesc(String motherId);
    List<VisitRecord> findByMotherIdOrderByVisitDateDesc(String motherId);
    List<VisitRecord> findByMotherId(String motherId);
}
