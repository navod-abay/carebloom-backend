package com.example.carebloom.repositories;

import com.example.carebloom.models.Workshop;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkshopRepository extends MongoRepository<Workshop, String> {
    // Find by active status and MoH office
    List<Workshop> findByIsActiveTrueAndMohOfficeId(String mohOfficeId);
    
    // Find by user ID and active status
    List<Workshop> findByUserIdAndIsActiveTrue(String userId);
    
    // Find by date and MoH office (for active workshops)
    List<Workshop> findByDateAndIsActiveTrueAndMohOfficeId(String date, String mohOfficeId);
    
    // Find by category and MoH office (for active workshops)
    List<Workshop> findByCategoryAndIsActiveTrueAndMohOfficeId(String category, String mohOfficeId);
    
    // Find by venue and MoH office (for active workshops)
    List<Workshop> findByVenueAndIsActiveTrueAndMohOfficeId(String venue, String mohOfficeId);
    
    // Find all workshops for MoH office (including inactive)
    List<Workshop> findByMohOfficeId(String mohOfficeId);
    
    // Find all workshops for user (including inactive)
    List<Workshop> findByUserId(String userId);
}
