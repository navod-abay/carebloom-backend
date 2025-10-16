package com.example.carebloom.models;

import lombok.Data;

@Data
public class QueueSettings {
    private boolean isOpen;
    private int maxCapacity;
    private int avgAppointmentTime; // minutes
    private String closingTime; // HH:mm format
    private boolean autoClose;
}
