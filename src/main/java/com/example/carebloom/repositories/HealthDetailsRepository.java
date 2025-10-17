package com.example.carebloom.repositories;

import com.example.carebloom.models.HealthDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HealthDetailsRepository extends MongoRepository<HealthDetails, String> {
    Optional<HealthDetails> findByMotherId(String motherId);
}
