package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "hints")
public class Hint {
    @Id
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private int trimester;
}