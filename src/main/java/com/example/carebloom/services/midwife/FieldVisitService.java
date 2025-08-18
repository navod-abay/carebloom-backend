package com.example.carebloom.services.midwife;

import com.example.carebloom.dto.midwife.FieldVisitCreateDTO;
import com.example.carebloom.dto.midwife.FieldVisitResponseDTO;
import com.example.carebloom.models.FieldVisit;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.FieldVisitRepository;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MotherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FieldVisitService {

    @Autowired
    private FieldVisitRepository fieldVisitRepository;

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private MotherRepository motherRepository;

    /**
     * Create a new field visit for a midwife
     */
    public FieldVisitResponseDTO createFieldVisit(FieldVisitCreateDTO createDTO, String firebaseUid) {
        // Find the midwife by Firebase UID
        Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        // Validate the request
        validateFieldVisitRequest(createDTO);

        // Create new field visit
        FieldVisit fieldVisit = new FieldVisit();
        fieldVisit.setMidwifeId(midwife.getId());
        fieldVisit.setDate(createDTO.getDate());
        fieldVisit.setStartTime(createDTO.getStartTime());
        fieldVisit.setEndTime(createDTO.getEndTime());
        fieldVisit.setSelectedMotherIds(createDTO.getSelectedMotherIds());
        fieldVisit.setStatus("SCHEDULED");
        fieldVisit.setCreatedAt(LocalDateTime.now());
        fieldVisit.setUpdatedAt(LocalDateTime.now());

        // Save to database
        FieldVisit savedFieldVisit = fieldVisitRepository.save(fieldVisit);

        // Update each mother's fieldVisitAppointment and collect mother info for response
        List<FieldVisitResponseDTO.MotherBasicInfo> mothers = new ArrayList<>();
        for (String motherId : createDTO.getSelectedMotherIds()) {
            Mother mother = motherRepository.findById(motherId).orElse(null);
            if (mother != null) {
                Mother.FieldVisitAppointment appointment = new Mother.FieldVisitAppointment();
                appointment.setVisitId(savedFieldVisit.getId());
                appointment.setDate(createDTO.getDate());
                appointment.setStartTime(createDTO.getStartTime());
                appointment.setEndTime(createDTO.getEndTime());
                appointment.setStatus("new");
                
                mother.setFieldVisitAppointment(appointment);
                motherRepository.save(mother);
                
                // Add to response list
                FieldVisitResponseDTO.MotherBasicInfo motherInfo = new FieldVisitResponseDTO.MotherBasicInfo();
                motherInfo.setId(motherId);
                motherInfo.setName(mother.getName());
                mothers.add(motherInfo);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found with ID: " + motherId);
            }
        }

        // Build response DTO directly
        FieldVisitResponseDTO dto = new FieldVisitResponseDTO();
        dto.setId(savedFieldVisit.getId());
        dto.setDate(savedFieldVisit.getDate());
        dto.setStartTime(savedFieldVisit.getStartTime());
        dto.setEndTime(savedFieldVisit.getEndTime());
        dto.setMidwifeId(savedFieldVisit.getMidwifeId());
        dto.setStatus(savedFieldVisit.getStatus());
        dto.setMothers(mothers);

        return dto;
    }

    /**
     * Get all field visits for a midwife
     */
    public List<FieldVisitResponseDTO> getFieldVisitsByMidwife(String firebaseUid) {
        // Find the midwife by Firebase UID
        Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        List<FieldVisit> fieldVisits = fieldVisitRepository.findByMidwifeId(midwife.getId());
        List<FieldVisitResponseDTO> responseDTOs = new ArrayList<>();
        
        for (FieldVisit fieldVisit : fieldVisits) {
            FieldVisitResponseDTO dto = new FieldVisitResponseDTO();
            dto.setId(fieldVisit.getId());
            dto.setDate(fieldVisit.getDate());
            dto.setStartTime(fieldVisit.getStartTime());
            dto.setEndTime(fieldVisit.getEndTime());
            dto.setMidwifeId(fieldVisit.getMidwifeId());
            dto.setStatus(fieldVisit.getStatus());
            
            // Get mother details
            List<FieldVisitResponseDTO.MotherBasicInfo> mothers = new ArrayList<>();
            for (String motherId : fieldVisit.getSelectedMotherIds()) {
                Mother mother = motherRepository.findById(motherId).orElse(null);
                FieldVisitResponseDTO.MotherBasicInfo motherInfo = new FieldVisitResponseDTO.MotherBasicInfo();
                motherInfo.setId(motherId);
                motherInfo.setName(mother != null ? mother.getName() : "Unknown Mother");
                mothers.add(motherInfo);
            }
            dto.setMothers(mothers);
            responseDTOs.add(dto);
        }
        
        return responseDTOs;
    }

    /**
     * Validate field visit request
     */
    private void validateFieldVisitRequest(FieldVisitCreateDTO createDTO) {
        if (createDTO.getDate() == null || createDTO.getDate().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }

        if (createDTO.getStartTime() == null || createDTO.getStartTime().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time is required");
        }

        if (createDTO.getEndTime() == null || createDTO.getEndTime().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time is required");
        }

        if (createDTO.getSelectedMotherIds() == null || createDTO.getSelectedMotherIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one mother must be selected");
        }

        // Validate time format (basic validation)
        if (!isValidTimeFormat(createDTO.getStartTime()) || !isValidTimeFormat(createDTO.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time format must be HH:MM");
        }

        // Validate date format (basic validation)
        if (!isValidDateFormat(createDTO.getDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date format must be YYYY-MM-DD");
        }
    }

    /**
     * Validate time format (HH:MM)
     */
    private boolean isValidTimeFormat(String time) {
        return time != null && time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    /**
     * Validate date format (YYYY-MM-DD)
     */
    private boolean isValidDateFormat(String date) {
        return date != null && date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
}
