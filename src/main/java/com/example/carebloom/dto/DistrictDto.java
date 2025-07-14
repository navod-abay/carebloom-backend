package com.example.carebloom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for district information.
 * This allows for future expansion if we need to include more information
 * about each district (like codes, regions, etc.).
 */
@Data
@AllArgsConstructor
public class DistrictDto {
    private String name;
}
