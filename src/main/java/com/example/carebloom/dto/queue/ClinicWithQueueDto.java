package com.example.carebloom.dto.queue;

import com.example.carebloom.models.AddedMother;
import com.example.carebloom.models.QueueSettings;
import lombok.Data;
import java.util.List;

@Data
public class ClinicWithQueueDto {
    private String id;
    private String title;
    private String date;
    private String startTime;
    private List<AddedMother> addedMothers;
    private QueueSettings queueSettings;
    // Add other fields as needed (description, doctorName, etc.)
}
