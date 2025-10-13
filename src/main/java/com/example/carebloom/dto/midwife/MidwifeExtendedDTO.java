package com.example.carebloom.dto.midwife;

import com.example.carebloom.dto.mother.MotherBasicDTO;
import com.example.carebloom.dto.fieldVisits.FieldVisitBasicDTO;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MidwifeExtendedDTO extends MidwifeBasicDTO {
    // List of assigned mothers with basic details
    private List<MotherBasicDTO> assignedMothers;
    private List<FieldVisitBasicDTO> fieldVisits;
    // List of assigned unit IDs
    private List<String> assignedUnitIds;
    private String state;
}
