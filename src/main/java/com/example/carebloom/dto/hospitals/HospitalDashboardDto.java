package com.example.carebloom.dto.hospitals;

import com.example.carebloom.dto.moh_offices.MoHOfficeInfoDto;
import com.example.carebloom.dto.midwives.AssignedMidwifeDto;
import com.example.carebloom.dto.clinics.ClinicAppointmentDto;
import com.example.carebloom.dto.workshops.AssignedWorkshopDto;
import com.example.carebloom.models.Mother.FieldVisitAppointment;

import lombok.Data;
import java.util.List;

@Data
public class HospitalDashboardDto {
    private MoHOfficeInfoDto hospital;
    private AssignedMidwifeDto midwife;
    private FieldVisitAppointment fieldVisitAppointment;
    private List<ClinicAppointmentDto> clinics;
    private List<AssignedWorkshopDto> workshops;
}
