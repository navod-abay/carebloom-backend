package com.example.carebloom.repositories;

import com.example.carebloom.models.Workshop;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkshopRepository extends MongoRepository<Workshop, String> {
    List<Workshop> findByIsActiveTrueAndMohOfficeId(String mohOfficeId);

    // Find by user ID and active status
    List<Workshop> findByUserIdAndIsActiveTrue(String userId);

    // Find by date and user ID (for active workshops)
    List<Workshop> findByDateAndUserIdAndIsActiveTrue(String date, String userId);

    // Find by category and user ID (for active workshops)
    List<Workshop> findByCategoryAndUserIdAndIsActiveTrue(String category, String userId);

    // Find by venue and user ID (for active workshops)
    List<Workshop> findByVenueAndUserIdAndIsActiveTrue(String venue, String userId);

    // Find all workshops for user (including inactive)
    List<Workshop> findByUserId(String userId);

    // Find all active workshops
    List<Workshop> findByIsActiveTrue();
}
