package com.example.carebloom.services.midwife;

import com.example.carebloom.models.VitalRecord;
import com.example.carebloom.repositories.VitalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class VitalRecordService {

    @Autowired
    private VitalRecordRepository vitalRecordRepository;

    public VitalRecord saveVisitRecord(VitalRecord record) {
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return vitalRecordRepository.save(record);
    }

    public VitalRecord updateVisitRecord(VitalRecord record) {
        record.setUpdatedAt(LocalDateTime.now());
        return vitalRecordRepository.save(record);
    }

    public List<VitalRecord> getVisitRecordsByMotherId(String motherId) {
        return vitalRecordRepository.findByMotherId(motherId);
    }
}
