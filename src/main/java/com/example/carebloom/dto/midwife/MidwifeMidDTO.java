package com.example.carebloom.dto.midwife;

import com.example.carebloom.dto.unit.UnitBasicDTO;
import lombok.Data;
import java.util.List;

@Data
public class MidwifeMidDTO {
    private String id;
    private String name;
    private String mohOfficeId;
    private List<UnitBasicDTO> units;
}
