package com.example.carebloom.dto.queue;

import lombok.Data;

@Data
public class QueueSettingsDto {
    private boolean isOpen;
    private int maxCapacity;
    private int avgAppointmentTime;
    private String closingTime;
    private boolean autoClose;
}
