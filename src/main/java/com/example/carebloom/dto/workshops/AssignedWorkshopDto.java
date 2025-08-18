package com.example.carebloom.dto.workshops;

import lombok.Data;

@Data
public class AssignedWorkshopDto {
    private String id;
    private String title;
    private String date;
    private String time;
    private String venue;
    private String description;
    private String category;
    private Boolean isActive;
}
