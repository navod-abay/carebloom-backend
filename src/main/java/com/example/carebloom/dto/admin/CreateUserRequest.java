package com.example.carebloom.dto.admin;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String officeId;
    private String email;
}
