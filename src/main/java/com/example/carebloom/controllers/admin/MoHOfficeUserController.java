package com.example.carebloom.controllers.admin;

import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.dto.admin.CreateUserRequest;
import com.example.carebloom.dto.admin.UpdateUserRequest;
import com.example.carebloom.services.admin.MoHOfficeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/moh-offices")
@CrossOrigin(origins = "${app.cors.admin-origin}")
public class MoHOfficeUserController {

    @Autowired
    private MoHOfficeUserService mohOfficeUserService;

    @GetMapping("/{officeId}/users")
    public ResponseEntity<List<MoHOfficeUser>> getUsersByOffice(@PathVariable String officeId) {
        List<MoHOfficeUser> users = mohOfficeUserService.getUsersByOfficeId(officeId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{officeId}/users")
    public ResponseEntity<MoHOfficeUser> createUser(
            @PathVariable String officeId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {
        
        CreateUserRequest request = new CreateUserRequest();
        request.setOfficeId(officeId);
        request.setEmail(requestBody.get("email"));
        
        // Get the admin who is creating this user
        String createdBy = authentication.getName(); // This will be the Firebase UID
        
        MoHOfficeUser createdUser = mohOfficeUserService.createUser(request, createdBy);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{officeId}/users/{userId}")
    public ResponseEntity<MoHOfficeUser> updateUser(
            @PathVariable String officeId,
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request) {
        
        MoHOfficeUser updatedUser = mohOfficeUserService.updateUser(officeId, userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{officeId}/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String officeId,
            @PathVariable String userId) {
        
        mohOfficeUserService.deleteUser(officeId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{officeId}/users/{userId}/approve")
    public ResponseEntity<MoHOfficeUser> approveUser(
            @PathVariable String officeId,
            @PathVariable String userId) {
        
        MoHOfficeUser approvedUser = mohOfficeUserService.approveUser(officeId, userId);
        return ResponseEntity.ok(approvedUser);
    }
}
