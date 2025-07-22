package com.example.carebloom.services.moh;

import com.example.carebloom.dto.midwife.MidwifeBasicDTO;
import com.example.carebloom.dto.midwife.MidwifeExtendedDTO;
import com.example.carebloom.dto.mother.MotherBasicDTO;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.repositories.MotherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import com.example.carebloom.services.admin.FirebaseUserService;
import com.google.firebase.auth.UserRecord;
import java.util.List;
import java.util.Optional;

@Service
public class MidwifeService {

    private static final Logger logger = LoggerFactory.getLogger(MidwifeService.class);

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private FirebaseUserService firebaseUserService;

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    @Autowired
    private MotherRepository motherRepository;

    /**
     * Get all midwives for the MOH office user's office
     */
    public List<MidwifeBasicDTO> getAllMidwives(String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        return midwifeRepository.findBasicDetailsByOfficeId(officeId);
    }

    /**
     * Get a specific midwife by ID (within user's office)
     */
    public Midwife getMidwifeById(String midwifeId, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        Midwife midwife = midwifeRepository.findByOfficeIdAndId(officeId, midwifeId);

        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        return midwife;
    }

    /**
     * Create a new midwife
     */
    public Midwife createMidwife(MidwifeBasicDTO request, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        request.setOfficeId(officeId);

        // Validate required fields
        validateMidwifeRequest(request, true);

        // Check if email already exists
        if (midwifeRepository.findByEmail(request.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        // Check if phone already exists
        if (midwifeRepository.findByPhone(request.getPhone()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists");
        }
        Midwife midwife = new Midwife();

        // Create Firebase user for the midwife
        try {
            UserRecord firebaseUser = firebaseUserService.createFirebaseUser(request.getEmail());
            String uid = firebaseUser.getUid();
            midwife.setFirebaseUid(uid);

            logger.info("Created Firebase user for midwife: {} with UID: {}", request.getEmail(),
                    firebaseUser.getUid());
        } catch (Exception e) {
            logger.error("Failed to create Firebase user for midwife: {}", request.getEmail(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create user account for midwife");
        }

        midwife.setOfficeId(officeId);
        midwife.setName(request.getName());
        midwife.setPhone(request.getPhone());
        midwife.setEmail(request.getEmail());
        midwife.setState("pending"); // MOH office can directly activate midwives
        midwife.setCreatedAt(LocalDateTime.now());
        midwife.setUpdatedAt(LocalDateTime.now());

        logger.info("Creating new midwife: {} for office: {}", request.getEmail(), officeId);
        return midwifeRepository.save(midwife);
    }

    // suspending a midwife
    public Midwife suspendMidwife(String midwifeId) {
        Optional<Midwife> midwifeOpt = midwifeRepository.findById(midwifeId);
        if (!midwifeOpt.isPresent()) {
            throw new RuntimeException("Midwife not found with ID: " + midwifeId);
        }

        Midwife midwife = midwifeOpt.get();
        midwife.setState("suspended");
        midwife.setUpdatedAt(LocalDateTime.now());

        return midwifeRepository.save(midwife);
    }

    /**
     * Update an existing midwife
     */
    public Midwife updateMidwife(String midwifeId, MidwifeBasicDTO request, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        Midwife existingMidwife = midwifeRepository.findByOfficeIdAndId(officeId, midwifeId);

        if (existingMidwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        // Validate request
        validateMidwifeRequest(request, false);

        // Check email uniqueness if being updated
        if (request.getEmail() != null && !request.getEmail().equals(existingMidwife.getEmail())) {
            Midwife emailCheck = midwifeRepository.findByEmail(request.getEmail());
            if (emailCheck != null && !emailCheck.getId().equals(midwifeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }
        }

        // Check phone uniqueness if being updated
        if (request.getPhone() != null && !request.getPhone().equals(existingMidwife.getPhone())) {
            Midwife phoneCheck = midwifeRepository.findByPhone(request.getPhone());
            if (phoneCheck != null && !phoneCheck.getId().equals(midwifeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists");
            }
        }

        // Update fields if provided
        if (request.getName() != null) {
            existingMidwife.setName(request.getName());
        }

        if (request.getPhone() != null) {
            existingMidwife.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            existingMidwife.setEmail(request.getEmail());
        }

        existingMidwife.setUpdatedAt(LocalDateTime.now());

        logger.info("Updating midwife: {} for office: {}", existingMidwife.getEmail(), officeId);
        return midwifeRepository.save(existingMidwife);
    }

    /**
     * Delete a midwife
     */
    public void deleteMidwife(String midwifeId, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        Midwife midwife = midwifeRepository.findByOfficeIdAndId(officeId, midwifeId);

        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        logger.info("Deleting midwife: {} from office: {}", midwife.getEmail(), officeId);
        midwifeRepository.delete(midwife);
    }

    public void grantAccessToMidwife(String midwifeId) {
        Midwife midwife = midwifeRepository.findById(midwifeId).orElse(null);

        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }
        MoHOfficeUser user = new MoHOfficeUser();
        user.setOfficeId(midwife.getOfficeId());
        user.setEmail(midwife.getEmail());
        user.setName(midwife.getName());
        user.setFirebaseUid(midwife.getFirebaseUid());
        user.setAccountType("normal"); // Default account type
        user.setRole("MOH_OFFICE_USER");
        user.setState("pending");
        mohOfficeUserRepository.save(user);
    }

    /**
     * Get extended midwife details
     */
    public MidwifeExtendedDTO getMidwifeExtendedDetails(String midwifeId, String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        Midwife midwife = midwifeRepository.findByOfficeIdAndId(officeId, midwifeId);
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        MidwifeExtendedDTO dto = new MidwifeExtendedDTO();
        dto.setId(midwife.getId());
        dto.setOfficeId(midwife.getOfficeId());
        dto.setName(midwife.getName());
        dto.setPhone(midwife.getPhone());
        dto.setEmail(midwife.getEmail());
        dto.setAssignedUnitIds(midwife.getAssignedUnitIds());

        // Map assigned mothers
        if (midwife.getAssignedMotherIds() != null && !midwife.getAssignedMotherIds().isEmpty()) {
            List<MotherBasicDTO> assignedMothers = midwife.getAssignedMotherIds().stream()
                .map(motherId -> {
                    Mother mother = motherRepository.findById(motherId).orElse(null);
                    if (mother == null) return null;
                    MotherBasicDTO mDto = new MotherBasicDTO();
                    mDto.setName(mother.getName());
                    mDto.setDueDate(mother.getDueDate());
                    mDto.setPhone(mother.getPhone());
                    return mDto;
                })
                .filter(m -> m != null)
                .toList();
            dto.setAssignedMothers(assignedMothers);
        }

        return dto;
    }

    /**
     * Helper method to get user's office ID
     */
    private String getUserOfficeId(String firebaseUid) {
        MoHOfficeUser user = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        if (!"active".equals(user.getState())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }

        return user.getOfficeId();
    }

    /**
     * Validate midwife request data
     */
    private void validateMidwifeRequest(MidwifeBasicDTO request, boolean isCreate) {
        if (isCreate || request.getName() != null) {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
            }
        }

        if (isCreate || request.getPhone() != null) {
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is required");
            }
        }

        if (isCreate || request.getEmail() != null) {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
            }

            if (!request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
            }
        }

    }
}
