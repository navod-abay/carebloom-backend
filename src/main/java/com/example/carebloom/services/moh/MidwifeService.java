package com.example.carebloom.services.moh;

import com.example.carebloom.dto.midwife.MidwifeBasicDTO;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
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

    /**
     * Get all midwives for the MOH office user's office
     */
    public List<Midwife> getAllMidwives(String firebaseUid) {
        String officeId = getUserOfficeId(firebaseUid);
        return midwifeRepository.findByOfficeId(officeId);
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
