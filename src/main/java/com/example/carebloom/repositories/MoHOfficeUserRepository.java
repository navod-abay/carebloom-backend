package com.example.carebloom.repositories;

import com.example.carebloom.models.MoHOfficeUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface MoHOfficeUserRepository extends MongoRepository<MoHOfficeUser, String> {
    
    @Query("{'office_id': ?0}")
    List<MoHOfficeUser> findByOfficeId(String officeId);
    
    @Query("{'firebase_uid': ?0}")
    MoHOfficeUser findByFirebaseUid(String firebaseUid);
    
    @Query("{'office_id': ?0, '_id': ?1}")
    MoHOfficeUser findByOfficeIdAndId(String officeId, String userId);
    
    @Query("{'office_id': ?0, 'email': ?1}")
    MoHOfficeUser findByOfficeIdAndEmail(String officeId, String email);
}
