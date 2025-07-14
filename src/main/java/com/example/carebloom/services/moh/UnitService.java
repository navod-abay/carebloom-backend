package com.example.carebloom.services.moh;

import com.example.carebloom.models.Unit;
import com.example.carebloom.repositories.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnitService {
    @Autowired
    private UnitRepository unitRepository;

    public Unit createUnit(Unit unit) {
        return unitRepository.save(unit);
    }

    public Unit updateUnit(String unitId, Unit request) {
        Unit existing = unitRepository.findById(unitId).orElse(null);
        if (existing == null) {
            return null;
        }
        if (request.getName() != null) existing.setName(request.getName());
        if (request.getMohOfficeId() != null) existing.setMohOfficeId(request.getMohOfficeId());
        if (request.getAssignedMidwifeId() != null) existing.setAssignedMidwifeId(request.getAssignedMidwifeId());
        return unitRepository.save(existing);
    }
}
