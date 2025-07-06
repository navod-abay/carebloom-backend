package com.example.carebloom.repositories;

import com.example.carebloom.models.Clinic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClinicRepository extends MongoRepository<Clinic, String> {
    List<Clinic> findByIsActiveTrue();
    List<Clinic> findByDateAndIsActiveTrue(String date);
}
