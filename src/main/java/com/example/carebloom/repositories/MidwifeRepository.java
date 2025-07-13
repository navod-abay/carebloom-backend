package com.example.carebloom.repositories;

import com.example.carebloom.dto.midwife.MidwifeBasicDTO;
import com.example.carebloom.models.Midwife;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface MidwifeRepository extends MongoRepository<Midwife, String> {
    
    @Query(value = "{'office_id': ?0}", fields = "{'_id': 1, 'office_id': 1, 'name': 1, 'phone': 1, 'email': 1}")
    List<MidwifeBasicDTO> findBasicDetailsByOfficeId(String officeId);
    
    @Query("{'office_id': ?0}")
    List<Midwife> findByOfficeId(String officeId);
    
    @Query("{'firebase_uid': ?0}")
    Midwife findByFirebaseUid(String firebaseUid);
    
    @Query("{'email': ?0}")
    Midwife findByEmail(String email);
    
    @Query("{'registration_number': ?0}")
    Midwife findByRegistrationNumber(String registrationNumber);
    
    @Query("{'office_id': ?0, '_id': ?1}")
    Midwife findByOfficeIdAndId(String officeId, String midwifeId);
    
    @Query("{'phone': ?0}")
    Midwife findByPhone(String phone);
    
    @Query("{'clinic': ?0}")
    List<Midwife> findByClinic(String clinic);
    
    @Query("{'specialization': ?0}")
    List<Midwife> findBySpecialization(String specialization);
    
}
