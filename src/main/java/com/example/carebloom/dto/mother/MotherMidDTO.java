package com.example.carebloom.dto.mother;

import lombok.Data;

@Data
public class MotherMidDTO {
    private String id;
    private String name;
    private String dueDate;
    private String phone;
    private String address;
    private String unit; // Unit Name not the Id
    private String state; // registrationStatus: 'Active' | 'Inactive' | 'Completed' | 'Pending'
}
