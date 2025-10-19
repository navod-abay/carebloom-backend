package com.example.carebloom.config;

import com.example.carebloom.models.AddedMother;
import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.ClinicRepository;
import com.example.carebloom.repositories.MotherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataMigrationConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataMigrationConfig.class);

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private MotherRepository motherRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateClinicData() {
        logger.info("Starting automatic data migration for clinics...");

        try {
            List<Clinic> allClinics = clinicRepository.findAll();
            int migratedCount = 0;

            // for (Clinic clinic : allClinics) {
            //     // Check if clinic has registeredMotherIds but empty or null addedMothers
            //     if (clinic.getRegisteredMotherIds() != null && 
            //         !clinic.getRegisteredMotherIds().isEmpty() &&
            //         (clinic.getAddedMothers() == null || clinic.getAddedMothers().isEmpty())) {
                    
            //         logger.info("Migrating clinic: {} (ID: {})", clinic.getTitle(), clinic.getId());
                    
            //         List<Mother> mothers = motherRepository.findAllById(clinic.getRegisteredMotherIds());
            //         List<AddedMother> addedMothers = new ArrayList<>();
                    
            //         for (Mother mother : mothers) {
            //             AddedMother am = new AddedMother();
            //             am.setId(mother.getId());
            //             am.setName(mother.getName());
            //             am.setEmail(mother.getEmail());
            //             am.setPhone(mother.getPhone());
            //             am.setDueDate(mother.getDueDate());
            //             am.setAge(25); // Default age
            //             am.setRecordNumber(mother.getRecordNumber());
            //             addedMothers.add(am);
                        
            //             logger.info("  - Added mother: {} ({})", mother.getName(), mother.getEmail());
            //         }
                    
            //         clinic.setAddedMothers(addedMothers);
            //         clinicRepository.save(clinic);
            //         migratedCount++;
                    
            //         logger.info("  ✓ Migrated {} mothers to clinic: {}", addedMothers.size(), clinic.getTitle());
            //     }
            // }

            logger.info("✓ Data migration complete! Migrated {} clinics", migratedCount);

        } catch (Exception e) {
            logger.error("Error during data migration:", e);
        }
    }
}
