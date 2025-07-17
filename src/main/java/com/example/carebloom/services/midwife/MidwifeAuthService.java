package com.example.carebloom.services.midwife;

import com.example.carebloom.dto.mother.MotherMidDTO;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Unit;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MidwifeAuthService {

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private UnitRepository unitRepository;

    /**
     * Get assigned mothers for a midwife by Firebase UID
     */
    public List<MotherMidDTO> getAssignedMothers(String firebaseUid) {
        // Find the midwife by Firebase UID
        Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        // Find all mothers assigned to this midwife
        List<Mother> assignedMothers = motherRepository.findByAreaMidwifeId(midwife.getId());

        // Convert to MotherMidDTO
        return assignedMothers.stream()
                .map(this::convertToMotherMidDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Mother entity to MotherMidDTO
     */
    private MotherMidDTO convertToMotherMidDTO(Mother mother) {
        MotherMidDTO dto = new MotherMidDTO();
        dto.setId(mother.getId());
        dto.setName(mother.getName());
        dto.setDueDate(mother.getDueDate());
        dto.setPhone(mother.getPhone());
        dto.setAddress(mother.getAddress());
        
        // Map registration status to state
        dto.setState(mother.getRegistrationStatus());
        
        // Get unit name if mohOfficeId exists
        if (mother.getMohOfficeId() != null) {
            List<Unit> units = unitRepository.findByMohOfficeId(mother.getMohOfficeId());
            if (!units.isEmpty()) {
                // For now, we'll use the first unit's name
                // You might want to add a specific unitId field to Mother model for better mapping
                dto.setUnit(units.get(0).getName());
            } else {
                dto.setUnit("No Unit Assigned");
            }
        } else {
            dto.setUnit("No Unit Assigned");
        }
        
        return dto;
    }

    
}
