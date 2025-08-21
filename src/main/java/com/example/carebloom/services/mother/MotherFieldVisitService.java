package com.example.carebloom.services.mother;

import com.example.carebloom.dto.mother.ConfirmFieldVisitTimeRequest;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MotherFieldVisitService {

    private static final Logger logger = LoggerFactory.getLogger(MotherFieldVisitService.class);

    @Autowired
    private MotherRepository motherRepository;

    /**
     * Confirm field visit time and update appointment status to confirmed
     */
    public Mother confirmFieldVisitTime(ConfirmFieldVisitTimeRequest request) {
        // Get current mother from security context
        Mother mother = SecurityUtils.getCurrentMother();
        if (mother == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mother not found in security context");
        }

        // Check if mother has a field visit appointment
        if (mother.getFieldVisitAppointment() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No field visit appointment found");
        }

        // Validate time format
        if (!isValidTimeFormat(request.getAvailableStartTime()) || 
            !isValidTimeFormat(request.getAvailableEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time format. Use HH:MM format");
        }

        // Update the field visit appointment
        Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
        appointment.setStartTime(request.getAvailableStartTime());
        appointment.setEndTime(request.getAvailableEndTime());
        appointment.setStatus("confirmed");

        // Save updated mother
        Mother updatedMother = motherRepository.save(mother);
        
        logger.info("Field visit time confirmed for mother ID: {}, new times: {} - {}", 
                   mother.getId(), request.getAvailableStartTime(), request.getAvailableEndTime());
        
        return updatedMother;
    }

    /**
     * Reschedule field visit by updating status to rescheduled
     */
    public Mother rescheduleFieldVisit() {
        // Get current mother from security context
        Mother mother = SecurityUtils.getCurrentMother();
        if (mother == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mother not found in security context");
        }

        // Check if mother has a field visit appointment
        if (mother.getFieldVisitAppointment() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No field visit appointment found");
        }

        // Update the field visit appointment status
        Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
        appointment.setStatus("rescheduled");

        // Save updated mother
        Mother updatedMother = motherRepository.save(mother);
        
        logger.info("Field visit rescheduled for mother ID: {}", mother.getId());
        
        return updatedMother;
    }

    /**
     * Validate time format (HH:MM)
     */
    private boolean isValidTimeFormat(String time) {
        return time != null && time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }
}
