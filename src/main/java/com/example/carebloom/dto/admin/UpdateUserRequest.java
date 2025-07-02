package com.example.carebloom.dto.admin;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String email;
    private String state;
}
