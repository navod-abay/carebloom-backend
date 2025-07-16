package com.example.carebloom.repositories;

import com.example.carebloom.models.QueueEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueEntryRepository extends MongoRepository<QueueEntry, String> {
    List<QueueEntry> findByQueueIdOrderByQueuePosition(String queueId);
    List<QueueEntry> findByQueueIdAndStatus(String queueId, String status);
    List<QueueEntry> findByClinicIdOrderByQueuePosition(String clinicId);
    Optional<QueueEntry> findByQueueIdAndEmail(String queueId, String email);
    long countByQueueIdAndStatus(String queueId, String status);
    Optional<QueueEntry> findTopByQueueIdAndStatusOrderByQueuePosition(String queueId, String status);
}
