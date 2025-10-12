package com.example.carebloom.dto.queue;

import lombok.Data;

@Data
public class AddedMotherDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String dueDate;
    private int age;
}
