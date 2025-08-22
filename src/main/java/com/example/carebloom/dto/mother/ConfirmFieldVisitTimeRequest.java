package com.example.carebloom.dto.mother;

import lombok.Data;

@Data
public class ConfirmFieldVisitTimeRequest {
    private String availableStartTime;
    private String availableEndTime;
}
