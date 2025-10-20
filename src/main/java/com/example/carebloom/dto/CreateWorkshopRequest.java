package com.example.carebloom.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateWorkshopRequest {
    private String title;
    private String date; // YYYY-MM-DD format
    private String time; // HH:MM format
    private String venue; // Location/venue name
    private String description; // Workshop description
    private String category; // Workshop category
    private Integer capacity; // Maximum number of participants
    private List<String> registeredMotherIds; // Optional: List of mother IDs to assign to this workshop
    private List<String> unitIds; // Optional: List of unit IDs to assign this workshop to
}
