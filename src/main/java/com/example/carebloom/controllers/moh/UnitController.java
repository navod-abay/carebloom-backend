package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Unit;
import com.example.carebloom.repositories.UnitRepository;
import com.example.carebloom.services.moh.UnitService;
import com.example.carebloom.dto.unit.AssignMidwifeRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class UnitController {
    @Autowired
    private UnitService unitService;

    @Autowired
    private UnitRepository unitRepository;


    @PostMapping("/unit")
    public ResponseEntity<Unit> createUnit(
            @RequestBody Unit request
    ) {
        Unit created = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/units/{mohOfficeId}")
    public ResponseEntity<List<Unit>> getUnitsByMohOfficeId(@PathVariable String mohOfficeId) {
        List<Unit> units = unitRepository.findByMohOfficeId(mohOfficeId);
        return ResponseEntity.ok(units);
    }


    @PutMapping("/unit/{unitId}")
    public ResponseEntity<Unit> updateUnit(@PathVariable String unitId, @RequestBody Unit request) {
        Unit updated = unitService.updateUnit(unitId, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/unit/{unitId}")
    public ResponseEntity<Void> deleteUnit(@PathVariable String unitId) {
        if (!unitRepository.existsById(unitId)) {
            return ResponseEntity.notFound().build();
        }
        unitRepository.deleteById(unitId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unit/{unitId}/assign-midwife")
    public ResponseEntity<Unit> assignMidwifeToUnit(@PathVariable String unitId, @RequestBody AssignMidwifeRequest request) {
        Unit updatedUnit = unitService.assignMidwifeToUnit(unitId, request.getMidwifeId());
        return ResponseEntity.ok(updatedUnit);
    }

    @PostMapping("/unit/{unitId}/unassign-midwife")
    public ResponseEntity<Unit> unassignMidwifeFromUnit(@PathVariable String unitId, @RequestBody AssignMidwifeRequest request) {
        Unit updatedUnit = unitService.unassignMidwifeFromUnit(unitId, request.getMidwifeId());
        return ResponseEntity.ok(updatedUnit);
    }
}
