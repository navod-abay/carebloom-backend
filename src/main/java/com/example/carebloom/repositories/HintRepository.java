package com.example.carebloom.repositories;

import com.example.carebloom.models.Hint;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HintRepository extends MongoRepository<Hint, String> {
}