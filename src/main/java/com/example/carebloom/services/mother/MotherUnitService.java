package com.example.carebloom.services.mother;

import com.example.carebloom.dto.unit.UnitBasicDTO;
import com.example.carebloom.models.Unit;
import com.example.carebloom.repositories.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotherUnitService {

    private static final Logger logger = LoggerFactory.getLogger(MotherUnitService.class);

    @Autowired
    private UnitRepository unitRepository;

    /**
     * Get all units assigned to a specific midwife
     * 
     * @param midwifeId The ID of the midwife
     * @return List of UnitBasicDTO assigned to the midwife
     */
    public List<UnitBasicDTO> getUnitsByMidwifeId(String midwifeId) {
        logger.debug("Fetching units for midwife with ID: {}", midwifeId);

        try {
            List<Unit> units = unitRepository.findByAssignedMidwifeId(midwifeId);
            
            List<UnitBasicDTO> unitDTOs = units.stream()
                .map(this::convertToUnitBasicDTO)
                .collect(Collectors.toList());

            logger.info("Found {} units assigned to midwife: {}", unitDTOs.size(), midwifeId);
            return unitDTOs;

        } catch (Exception e) {
            logger.error("Error fetching units for midwife: {}", midwifeId, e);
            throw e;
        }
    }

    /**
     * Convert Unit entity to UnitBasicDTO
     */
    private UnitBasicDTO convertToUnitBasicDTO(Unit unit) {
        UnitBasicDTO dto = new UnitBasicDTO();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        return dto;
    }
}
