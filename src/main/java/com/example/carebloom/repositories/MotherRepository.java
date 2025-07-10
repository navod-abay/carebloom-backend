package com.example.carebloom.repositories;

import com.example.carebloom.models.Mother;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MotherRepository extends MongoRepository<Mother, String> {
    Mother findByFirebaseUid(String firebaseUid);
    List<Mother> findByMohOfficeId(String mohOfficeId); // Find mothers by MOH Office ID
    List<Mother> findByDistrict(String district); // Find mothers by district
}
