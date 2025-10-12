package com.example.carebloom.repositories;

import com.example.carebloom.models.QueueUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueUserRepository extends MongoRepository<QueueUser, String> {
    List<QueueUser> findByClinicIdOrderByPosition(String clinicId);
    
    List<QueueUser> findByClinicIdAndStatusOrderByPosition(String clinicId, String status);
    
    Optional<QueueUser> findFirstByClinicIdAndStatus(String clinicId, String status);
    
    Optional<QueueUser> findByClinicIdAndPosition(String clinicId, int position);
    
    Optional<QueueUser> findByMotherIdAndClinicId(String motherId, String clinicId);
    
    long countByClinicId(String clinicId);
    
    long countByClinicIdAndStatus(String clinicId, String status);
    
    void deleteByClinicId(String clinicId);
}
