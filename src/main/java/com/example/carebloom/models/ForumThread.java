package com.example.carebloom.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "forumthreads")
public class ForumThread {
    public enum Category {
        MEDICAL,
        NON_MEDICAL
    }
    @Id
    private String id;
    private String title;
    private String content;
    private Category category;
    private String authorId;
    private String authorName;
    private String authorProfileImage;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isClosed = false;
    private List<ForumReply> replies = new ArrayList<>();
}
