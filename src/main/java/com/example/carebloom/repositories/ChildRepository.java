package com.example.carebloom.repositories;

import com.example.carebloom.models.Child;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChildRepository extends MongoRepository<Child, String> {
    List<Child> findByMotherId(String motherId);
}
