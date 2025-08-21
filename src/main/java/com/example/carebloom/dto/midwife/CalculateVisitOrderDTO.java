package com.example.carebloom.dto.midwife;

import lombok.Data;

@Data
public class CalculateVisitOrderDTO {
    private Boolean overrideUnconfirmed; // If true, proceed even with unconfirmed mothers
}
