package com.example.carebloom.repositories;

import com.example.carebloom.models.ChildRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildRecordRepository extends MongoRepository<ChildRecord, String> {
    // add query methods if needed
}
