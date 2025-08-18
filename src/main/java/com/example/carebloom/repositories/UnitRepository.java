package com.example.carebloom.repositories;

import com.example.carebloom.models.Unit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UnitRepository extends MongoRepository<Unit, String> {
    List<Unit> findByMohOfficeId(String mohOfficeId);
    List<Unit> findByAssignedMidwifeId(String assignedMidwifeId);
    
}
