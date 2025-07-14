package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Document(collection = "children")
public class Child {
    @Id
    private String id;
    
    @Field("mother_id")
    private String motherId;
    
    private String name;
    private String dob;
    private String gender;
    
    @Field("birth_weight")
    private String birthWeight;
    
    @Field("birth_length")
    private String birthLength;
    
    private List<String> vaccinations = new ArrayList<>();
    
    @Field("health_notes")
    private String healthNotes;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
