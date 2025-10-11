package com.example.carebloom.controllers.mother;

import com.example.carebloom.controllers.BaseControllerTest;
import com.example.carebloom.dto.LocationRegistrationRequest;
import com.example.carebloom.dto.PersonalRegistrationRequest;
import com.example.carebloom.dto.RegistrationRequest;
import com.example.carebloom.dto.hospitals.HospitalDashboardDto;
import com.example.carebloom.dto.moh_offices.MoHOfficeInfoDto;
import com.example.carebloom.dto.midwives.AssignedMidwifeDto;
import com.example.carebloom.models.MotherProfile;
import com.example.carebloom.services.AuthService;
import com.example.carebloom.services.mother.MotherHospitalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for Mother Controllers
 * Tests the web layer for mother-specific endpoints
 */
@WebMvcTest({MotherAuthController.class, MotherHospitalController.class})
@DisplayName("Mother Controllers Tests")
class MotherControllersTest extends BaseControllerTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MotherHospitalService motherHospitalService;

    @Nested
    @DisplayName("MotherAuthController Tests")
    class MotherAuthControllerTests {

        @Test
        @DisplayName("Should verify valid mother token successfully")
        void verifyToken_ValidToken_ReturnsMotherProfile() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            MotherProfile profile = createMockMotherProfile();

            when(authService.verifyIdToken(createBearerToken(token))).thenReturn(profile);

            // Act & Assert
            mockMvc.perform(post("/api/v1/mothers/auth/verify")
                    .header("Authorization", createBearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(profile.getId()))
                    .andExpect(jsonPath("$.email").value(profile.getEmail()))
                    .andExpect(jsonPath("$.name").value(profile.getName()));
        }

        @Test
        @DisplayName("Should return 400 when token verification fails")
        void verifyToken_InvalidToken_Returns400() throws Exception {
            // Arrange
            String token = "invalid-firebase-token";
            when(authService.verifyIdToken(createBearerToken(token)))
                .thenThrow(new RuntimeException("Invalid token"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/mothers/auth/verify")
                    .header("Authorization", createBearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should register mother successfully")
        void registerMother_ValidRequest_ReturnsMotherProfile() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            RegistrationRequest request = new RegistrationRequest();
            request.setEmail("mother@example.com");
            
            MotherProfile profile = createMockMotherProfile();
            when(authService.registerMother(createBearerToken(token), request.getEmail()))
                .thenReturn(profile);

            // Act & Assert
            mockMvc.perform(post("/api/v1/mothers/auth/register")
                    .header("Authorization", createBearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(profile.getId()))
                    .andExpect(jsonPath("$.email").value(profile.getEmail()));
        }

        @Test
        @DisplayName("Should return 400 when registration fails")
        void registerMother_RegistrationFails_Returns400() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            RegistrationRequest request = new RegistrationRequest();
            request.setEmail("mother@example.com");
            
            when(authService.registerMother(createBearerToken(token), request.getEmail()))
                .thenThrow(new RuntimeException("Registration failed"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/mothers/auth/register")
                    .header("Authorization", createBearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should update personal info successfully")
        void registerPersonal_ValidRequest_ReturnsMotherProfile() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            PersonalRegistrationRequest request = new PersonalRegistrationRequest();
            request.setName("Jane Doe");
            request.setDueDate("2024-12-01");
            
            MotherProfile profile = createMockMotherProfile();
            when(authService.updatePersonalInfo(createBearerToken(token), request))
                .thenReturn(profile);

            // Act & Assert
            mockMvc.perform(post("/api/v1/mothers/auth/register/personal")
                    .header("Authorization", createBearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(profile.getId()));
        }

        @Test
        @DisplayName("Should update location successfully")
        void registerLocation_ValidRequest_ReturnsMotherProfile() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            LocationRegistrationRequest request = new LocationRegistrationRequest();
            request.setDistrict("Colombo");
            request.setMohOfficeId("moh-office-123");
            
            MotherProfile profile = createMockMotherProfile();
            when(authService.updateLocation(createBearerToken(token), request))
                .thenReturn(profile);

            // Act & Assert
            mockMvc.perform(post("/api/v1/mothers/auth/register/location")
                    .header("Authorization", createBearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(profile.getId()));
        }

        @Test
        @DisplayName("Should skip location successfully")
        void skipLocation_ValidRequest_ReturnsMotherProfile() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            MotherProfile profile = createMockMotherProfile();
            when(authService.skipLocation(createBearerToken(token))).thenReturn(profile);

            // Act & Assert
            mockMvc.perform(post("/api/v1/mothers/auth/register/skip-location")
                    .header("Authorization", createBearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(profile.getId()));
        }

        @Test
        @DisplayName("Should get profile when user is authenticated")
        void getProfile_AuthenticatedUser_ReturnsProfile() throws Exception {
            // Arrange
            String firebaseUid = "firebase-uid-123";
            MotherProfile profile = createMockMotherProfile();
            
            // Mock SecurityContext
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(firebaseUid);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            when(authService.getProfileByFirebaseUid(firebaseUid)).thenReturn(profile);

            // Act & Assert
            mockMvc.perform(get("/api/v1/mothers/auth/profile")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(profile.getId()))
                    .andExpect(jsonPath("$.email").value(profile.getEmail()));
            
            // Cleanup
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Should return empty object when user not authenticated")
        void getProfile_NotAuthenticated_ReturnsEmptyObject() throws Exception {
            // Arrange
            SecurityContextHolder.clearContext();

            // Act & Assert
            mockMvc.perform(get("/api/v1/mothers/auth/profile")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("MotherHospitalController Tests")
    class MotherHospitalControllerTests {

        @Test
        @DisplayName("Should return hospital dashboard for valid mother ID")
        void getHospitalDashboard_ValidMotherId_ReturnsSuccess() throws Exception {
            // Arrange
            String motherId = "mother-id-123";
            HospitalDashboardDto dashboard = createMockHospitalDashboard();

            when(motherHospitalService.getMotherHospitalDashboard()).thenReturn(dashboard);

            // Act & Assert
            mockMvc.perform(get("/api/v1/mother/{motherId}/hospitals", motherId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hospital").exists())
                    .andExpect(jsonPath("$.midwife").exists());
        }

        @Test
        @DisplayName("Should return 404 when mother not found")
        void getHospitalDashboard_MotherNotFound_Returns404() throws Exception {
            // Arrange
            String motherId = "non-existent-mother";
            when(motherHospitalService.getMotherHospitalDashboard())
                .thenThrow(new RuntimeException("Mother not found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/mother/{motherId}/hospitals", motherId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void getHospitalDashboard_ServiceException_Returns500() throws Exception {
            // Arrange
            String motherId = "mother-id-123";
            when(motherHospitalService.getMotherHospitalDashboard())
                .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/mother/{motherId}/hospitals", motherId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
    }

    /**
     * Helper method to create a mock HospitalDashboardDto for testing
     */
    private HospitalDashboardDto createMockHospitalDashboard() {
        HospitalDashboardDto dashboard = new HospitalDashboardDto();
        
        // Mock hospital info
        MoHOfficeInfoDto hospital = new MoHOfficeInfoDto();
        hospital.setDivisionalSecretariat("Colombo Central Hospital");
        hospital.setAddress("Colombo");
        hospital.setContactNumber("011-1234567");
        dashboard.setHospital(hospital);
        
        // Mock midwife info
        AssignedMidwifeDto midwife = new AssignedMidwifeDto();
        midwife.setId("midwife-123");
        midwife.setName("Jane Smith");
        midwife.setPhone("011-9876543");
        dashboard.setMidwife(midwife);
        
        return dashboard;
    }

    /**
     * Helper method to create a mock MotherProfile for testing
     */
    private MotherProfile createMockMotherProfile() {
        MotherProfile profile = new MotherProfile();
        profile.setId("user-id-123");
        profile.setEmail("mother@example.com");
        profile.setName("Jane Doe");
        profile.setRole("mother");
        return profile;
    }
}
