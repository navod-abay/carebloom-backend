package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "articles")
public class Article {
    @Id
    private String id;
    private String title;
    private String headerImage;
    private int likes;
    private String summary;
    private String content;
    private String category;
    private String createdAt;
}