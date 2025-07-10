package com.example.carebloom.repositories;

import com.example.carebloom.models.MOHOffice;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MOHOfficeRepository extends MongoRepository<MOHOffice, String> {
    MOHOffice findByadminEmail(String adminEmail);
    MOHOffice findByid(String id);
    List<MOHOffice> findByDistrict(String district);
}
