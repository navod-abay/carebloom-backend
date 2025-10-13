package com.example.carebloom.services.moh;

import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Unit;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Service
public class UnitService {
    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private MidwifeRepository midwifeRepository;

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

    @Transactional
    public Unit assignMidwifeToUnit(String unitId, String midwifeId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));
        Midwife midwife = midwifeRepository.findById(midwifeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found"));

        // Check if the unit is already assigned to another midwife
        if (unit.getAssignedMidwifeId() != null && !unit.getAssignedMidwifeId().equals(midwifeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit is already assigned to another midwife");
        }

        unit.setAssignedMidwifeId(midwifeId);

        if (midwife.getAssignedUnitIds() == null) {
            midwife.setAssignedUnitIds(new ArrayList<>());
        }
        if (!midwife.getAssignedUnitIds().contains(unitId)) {
            midwife.getAssignedUnitIds().add(unitId);
        }

        midwifeRepository.save(midwife);
        return unitRepository.save(unit);
    }

    @Transactional
    public Unit unassignMidwifeFromUnit(String unitId, String midwifeId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));
        Midwife midwife = midwifeRepository.findById(midwifeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found"));

        if (unit.getAssignedMidwifeId() == null || !unit.getAssignedMidwifeId().equals(midwifeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Midwife is not assigned to this unit");
        }

        unit.setAssignedMidwifeId(null);

        if (midwife.getAssignedUnitIds() != null) {
            midwife.getAssignedUnitIds().remove(unitId);
        }

        midwifeRepository.save(midwife);
        return unitRepository.save(unit);
    }
}
