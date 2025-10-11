package com.example.carebloom.services;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.carebloom.dto.clinics.ClinicMidwifeDTO;
import com.example.carebloom.repositories.ClinicRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClinicService {

    private static final Logger logger = LoggerFactory.getLogger(ClinicService.class.getName());

    @Autowired
    private ClinicRepository clinicRepository;

    public List<ClinicMidwifeDTO> getClinicsForMidwife(String mohOfficeId) {
        logger.info("Fetching clinics for MoH Office ID: " + mohOfficeId);
        return clinicRepository.findClinicMidwifeDTOs(mohOfficeId);
    }
}
