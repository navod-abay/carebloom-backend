package com.example.carebloom.controllers.admin;

import com.example.carebloom.controllers.BaseControllerTest;
import com.example.carebloom.models.PlatformAdmin;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.PlatformAdminRepository;
import com.example.carebloom.repositories.VendorRepository;
import com.example.carebloom.services.admin.EmailCheckService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for Admin Controllers
 * Tests the web layer for admin-specific endpoints
 */
@WebMvcTest({
    PlatformAdminAuthController.class,
    EmailCheckController.class
})
@DisplayName("Admin Controllers Tests")
class AdminControllersTest extends BaseControllerTest {

    @MockitoBean
    private MotherRepository motherRepository;

    @MockitoBean
    private PlatformAdminRepository platformAdminRepository;

    @MockitoBean    
    private EmailCheckService emailCheckService;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @MockitoBean
    private MoHOfficeUserController moHOfficeUserController;
    
    @MockitoBean
    private MoHOfficeUserRepository moHOfficeUserRepository;

        @MockitoBean
    private MidwifeRepository midwifeRepository;
    
    @MockitoBean
    private VendorRepository vendorRepository;

    @Nested
    @DisplayName("PlatformAdminAuthController Tests")
    class PlatformAdminAuthControllerTests {

        @Test
        @DisplayName("Should verify valid admin token successfully")
        void verifyToken_ValidAdmin_ReturnsSuccess() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            String firebaseUid = "firebase-uid-123";
            
            PlatformAdmin admin = new PlatformAdmin();
            admin.setId("admin-id-123");
            admin.setEmail("admin@example.com");
            admin.setFirebaseUid(firebaseUid);

            FirebaseToken decodedToken = mock(FirebaseToken.class);
            when(decodedToken.getUid()).thenReturn(firebaseUid);
            
            // Mock static FirebaseAuth.getInstance() call
            try (var mockedStatic = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                when(firebaseAuthInstance.verifyIdToken(token)).thenReturn(decodedToken);
                
                when(platformAdminRepository.findByFirebaseUid(firebaseUid)).thenReturn(admin);

                // Act & Assert
                mockMvc.perform(post("/api/v1/admin/auth/verify")
                        .header("Authorization", createBearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.role").value("PLATFORM_MANAGER"))
                        .andExpect(jsonPath("$.userId").value("admin-id-123"));
            }
        }

        @Test
        @DisplayName("Should return 401 when admin not found")
        void verifyToken_AdminNotFound_Returns401() throws Exception {
            // Arrange
            String token = "valid-firebase-token";
            String firebaseUid = "firebase-uid-123";

            FirebaseToken decodedToken = mock(FirebaseToken.class);
            when(decodedToken.getUid()).thenReturn(firebaseUid);
            
            // Mock static FirebaseAuth.getInstance() call
            try (var mockedStatic = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                when(firebaseAuthInstance.verifyIdToken(token)).thenReturn(decodedToken);
                
                when(platformAdminRepository.findByFirebaseUid(firebaseUid)).thenReturn(null);

                // Act & Assert
                mockMvc.perform(post("/api/v1/admin/auth/verify")
                        .header("Authorization", createBearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.error").value("Unauthorized: User not found"));
            }
        }

        @Test
        @DisplayName("Should return 401 when Firebase token is invalid")
        void verifyToken_InvalidToken_Returns401() throws Exception {
            // Arrange
            String token = "invalid-firebase-token";
            
            // Mock static FirebaseAuth.getInstance() call
            try (var mockedStatic = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                when(firebaseAuthInstance.verifyIdToken(token))
                    .thenThrow(new RuntimeException("Invalid token"));

                // Act & Assert
                mockMvc.perform(post("/api/v1/admin/auth/verify")
                        .header("Authorization", createBearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.error").exists());
            }
        }
    }

    @Nested
    @DisplayName("EmailCheckController Tests")
    class EmailCheckControllerTests {

        @Test
        @DisplayName("Should return exists=true when email exists in system")
        void checkEmail_EmailExists_ReturnsTrue() throws Exception {
            // Arrange
            String email = "existing@example.com";
            when(emailCheckService.emailExists(email)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(get("/api/v1/admin/users/check-email")
                    .param("email", email)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exists").value(true))
                    .andExpect(jsonPath("$.message").value("Email already exists in the system"));
        }

        @Test
        @DisplayName("Should return exists=false when email doesn't exist")
        void checkEmail_EmailDoesNotExist_ReturnsFalse() throws Exception {
            // Arrange
            String email = "new@example.com";
            when(emailCheckService.emailExists(email)).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/api/v1/admin/users/check-email")
                    .param("email", email)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exists").value(false))
                    .andExpect(jsonPath("$.message").value("Email is available"));
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void checkEmail_InvalidEmailFormat_Returns400() throws Exception {
            // Arrange
            String invalidEmail = "invalid-email";

            // Act & Assert
            mockMvc.perform(get("/api/v1/admin/users/check-email")
                    .param("email", invalidEmail)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid email format"))
                    .andExpect(jsonPath("$.message").value("Please provide a valid email address"));
        }

        @Test
        @DisplayName("Should return 400 when email parameter is missing")
        void checkEmail_MissingEmail_Returns400() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/admin/users/check-email")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void checkEmail_ServiceException_Returns500() throws Exception {
            // Arrange
            String email = "test@example.com";
            when(emailCheckService.emailExists(email))
                .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/admin/users/check-email")
                    .param("email", email)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Server error"))
                    .andExpect(jsonPath("$.message").value("Unable to check email at this time"));
        }
    }
}
