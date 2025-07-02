package com.example.carebloom.services.admin;

import com.example.carebloom.dto.admin.CreateMoHOfficeRequest;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.repositories.MOHOfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class MoHOfficeManagementService {

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;

    public MOHOffice createMoHOffice(CreateMoHOfficeRequest request) {
        MOHOffice office = new MOHOffice();
        office.setDivisionalSecretariat(request.getDivisionalSecretariat());
        office.setAddress(request.getAddress());
        
        MOHOffice.Location location = new MOHOffice.Location();
        location.setLatitude(request.getLocation().getLatitude());
        location.setLongitude(request.getLocation().getLongitude());
        office.setLocation(location);
        
        office.setOfficerInCharge(request.getOfficerInCharge());
        office.setContactNumber(request.getContactNumber());
        office.setAdminEmail(request.getAdminEmail());

        return mohOfficeRepository.save(office);
    }

    public List<MOHOffice> getAllMoHOffices() {
        return mohOfficeRepository.findAll();
    }

    public MOHOffice getMoHOfficeById(String id) {
        return mohOfficeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "MOH Office not found with id: " + id
            ));
    }

    public void deleteMoHOffice(String id) {
        MOHOffice office = getMoHOfficeById(id); // This will throw 404 if not found
        mohOfficeRepository.delete(office);
    }
}
