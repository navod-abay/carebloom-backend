package com.example.carebloom.dto.clinics;

import lombok.Data;

@Data
public class ClinicAppointmentDto {
    private String id;
    private String date;
    private String startTime;
    private String title;
    private String doctorName;
    private String location;
    private Boolean queueActive;
    private Integer queueNumber;
    private Integer currentQueueNumber;
    private String estimatedWaitTime;
}
