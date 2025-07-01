package com.example.carebloom.dto;

import lombok.Data;

@Data
public class PersonalRegistrationRequest {
    private String name;
    private String dueDate;
    private String phone;
    private String address;
}
