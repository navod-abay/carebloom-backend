package com.example.carebloom.dto.admin;

import lombok.Data;

@Data
public class ArticleDTO {
    private String id;
    private String title;
    private String headerImage;
    private int likes;
    private String summary;
    private String content;
    private String createdAt;
}