package com.example.carebloom.dto.queue;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartQueueRequest {
    private int maxCapacity = 50;
    private int avgAppointmentTime = 15;
}
