package com.example.carebloom.config;

import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MOHOfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (mohOfficeRepository.count() == 0) {
            initializeMOHOffices();
        }
        
        if (midwifeRepository.count() == 0) {
            initializeMidwives();
        }
    }

    private void initializeMOHOffices() {
        // Create sample MOH Office
        MOHOffice mohOffice = new MOHOffice();
        mohOffice.setDivisionalSecretariat("Colombo MOH Office");
        mohOffice.setAddress("123 Main Street, Colombo 07");
        mohOffice.setContactNumber("+94112345678");
        mohOffice.setAdminEmail("colombo.moh@health.gov.lk");
        mohOffice.setOfficerInCharge("Dr. Kumara Silva");
        
        // Set location
        MOHOffice.Location location = new MOHOffice.Location();
        location.setLatitude(6.9271);
        location.setLongitude(79.8612);
        mohOffice.setLocation(location);
        
        MOHOffice savedOffice = mohOfficeRepository.save(mohOffice);
        System.out.println("Created MOH Office: " + savedOffice.getId());
    }

    private void initializeMidwives() {
        // Get the first MOH office
        MOHOffice mohOffice = mohOfficeRepository.findAll().get(0);
        
        // Create sample midwives
        Midwife midwife1 = new Midwife();
        midwife1.setOfficeId(mohOffice.getId());
        midwife1.setName("Dr. Sarah Johnson");
        midwife1.setClinic("ANC Clinic A");
        midwife1.setSpecialization("Obstetrics and Gynecology");
        midwife1.setYearsOfExperience(5);
        midwife1.setCertifications(Arrays.asList("Certified Nurse Midwife", "Basic Life Support"));
        midwife1.setPhone("(555) 123-4567");
        midwife1.setEmail("sarah.johnson@clinic.com");
        midwife1.setRegistrationNumber("MID2024001");
        midwife1.setState("active");
        midwife1.setCreatedBy("admin");
        midwife1.setCreatedAt(LocalDateTime.now());
        midwife1.setUpdatedAt(LocalDateTime.now());

        Midwife midwife2 = new Midwife();
        midwife2.setOfficeId(mohOffice.getId());
        midwife2.setName("Dr. Emily Davis");
        midwife2.setClinic("Regional Hospital A");
        midwife2.setSpecialization("Family Medicine");
        midwife2.setYearsOfExperience(8);
        midwife2.setCertifications(Arrays.asList("Certified Midwife", "Advanced Cardiac Life Support", "Neonatal Resuscitation"));
        midwife2.setPhone("(555) 987-6543");
        midwife2.setEmail("emily.davis@hospital.com");
        midwife2.setRegistrationNumber("MID2024002");
        midwife2.setState("pending");
        midwife2.setCreatedBy("admin");
        midwife2.setCreatedAt(LocalDateTime.now());
        midwife2.setUpdatedAt(LocalDateTime.now());

        midwifeRepository.save(midwife1);
        midwifeRepository.save(midwife2);
        
        System.out.println("Created 2 sample midwives");
    }
    
}
