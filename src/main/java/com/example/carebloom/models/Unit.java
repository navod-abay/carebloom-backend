package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Data
@Document(collection = "units")
public class Unit {
    @Id
    private String id;

    @Field("moh_office_id")
    private String mohOfficeId;

    @Field("name")
    private String name;

    @Field("assigned_midwife_id")
    private String assignedMidwifeId;
}
