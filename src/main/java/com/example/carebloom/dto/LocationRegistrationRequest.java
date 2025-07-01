package com.example.carebloom.dto;

import lombok.Data;

@Data
public class LocationRegistrationRequest {
    private String district;
    private String division;
    private String recordNumber;
}
