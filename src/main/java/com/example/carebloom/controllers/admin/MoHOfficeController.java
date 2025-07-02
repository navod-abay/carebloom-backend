package com.example.carebloom.controllers.admin;

import com.example.carebloom.dto.admin.CreateMoHOfficeRequest;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.services.admin.MoHOfficeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/moh-offices")
@CrossOrigin(origins = "${app.cors.admin-origin}")
public class MoHOfficeController {

    @Autowired
    private MoHOfficeManagementService mohOfficeManagementService;

    @PostMapping
    public ResponseEntity<MOHOffice> createMoHOffice(@RequestBody CreateMoHOfficeRequest request) {
        MOHOffice createdOffice = mohOfficeManagementService.createMoHOffice(request);
        return ResponseEntity.ok(createdOffice);
    }

    @GetMapping
    public ResponseEntity<List<MOHOffice>> getAllMoHOffices() {
        List<MOHOffice> offices = mohOfficeManagementService.getAllMoHOffices();
        return ResponseEntity.ok(offices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MOHOffice> getMoHOfficeById(@PathVariable String id) {
        MOHOffice office = mohOfficeManagementService.getMoHOfficeById(id);
        return ResponseEntity.ok(office);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMoHOffice(@PathVariable String id) {
        mohOfficeManagementService.deleteMoHOffice(id);
        return ResponseEntity.noContent().build();
    }
}
