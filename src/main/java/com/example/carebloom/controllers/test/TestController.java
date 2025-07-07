package com.example.carebloom.controllers.test;

import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MOHOfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;

    @GetMapping("/midwives")
    public ResponseEntity<List<Midwife>> getAllMidwives() {
        List<Midwife> midwives = midwifeRepository.findAll();
        return ResponseEntity.ok(midwives);
    }

    @GetMapping("/midwives/{id}")
    public ResponseEntity<Midwife> getMidwifeById(@PathVariable String id) {
        Optional<Midwife> midwife = midwifeRepository.findById(id);
        return midwife.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/moh-offices")
    public ResponseEntity<List<MOHOffice>> getAllMOHOffices() {
        List<MOHOffice> offices = mohOfficeRepository.findAll();
        return ResponseEntity.ok(offices);
    }

    @PostMapping("/midwives")
    public ResponseEntity<Midwife> createTestMidwife(@RequestBody Midwife midwife) {
        // Get the first office if no office ID is provided
        if (midwife.getOfficeId() == null || midwife.getOfficeId().isEmpty()) {
            List<MOHOffice> offices = mohOfficeRepository.findAll();
            if (!offices.isEmpty()) {
                midwife.setOfficeId(offices.get(0).getId());
            }
        }
        
        Midwife saved = midwifeRepository.save(midwife);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/midwives/{id}")
    public ResponseEntity<Midwife> updateTestMidwife(@PathVariable String id, @RequestBody Midwife midwife) {
        Optional<Midwife> existingMidwife = midwifeRepository.findById(id);
        if (existingMidwife.isPresent()) {
            midwife.setId(id);
            Midwife saved = midwifeRepository.save(midwife);
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/midwives/{id}")
    public ResponseEntity<Void> deleteTestMidwife(@PathVariable String id) {
        if (midwifeRepository.existsById(id)) {
            midwifeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
