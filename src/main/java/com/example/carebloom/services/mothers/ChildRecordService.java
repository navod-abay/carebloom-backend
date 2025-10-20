package com.example.carebloom.services.mothers;

import com.example.carebloom.models.ChildRecord;
import com.example.carebloom.repositories.ChildRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ChildRecordService {

    private final ChildRecordRepository repo;

    @Autowired
    public ChildRecordService(ChildRecordRepository repo) {
        this.repo = repo;
    }

    public ChildRecord save(ChildRecord record) {
        return repo.save(record);
    }
}
