package com.example.carebloom.dto.admin;

import lombok.Data;

@Data
public class HintDTO {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private int trimester;
}