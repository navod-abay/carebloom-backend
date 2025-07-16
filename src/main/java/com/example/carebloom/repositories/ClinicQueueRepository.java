package com.example.carebloom.repositories;

import com.example.carebloom.models.ClinicQueue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicQueueRepository extends MongoRepository<ClinicQueue, String> {
    Optional<ClinicQueue> findByClinicId(String clinicId);
    List<ClinicQueue> findByMohOfficeId(String mohOfficeId);
    List<ClinicQueue> findByStatus(String status);
    List<ClinicQueue> findByMohOfficeIdAndStatus(String mohOfficeId, String status);
}
