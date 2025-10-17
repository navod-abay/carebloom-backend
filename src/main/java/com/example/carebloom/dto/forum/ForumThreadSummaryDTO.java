package com.example.carebloom.dto.forum;

import com.example.carebloom.models.ForumThread;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumThreadSummaryDTO {
    private String id;
    private String title;
    private ForumThread.Category category;
    private String authorId;
    private String authorName;
    private String authorProfileImage;
    private LocalDateTime createdAt;
    private boolean isClosed;
    private int replyCount;
}
