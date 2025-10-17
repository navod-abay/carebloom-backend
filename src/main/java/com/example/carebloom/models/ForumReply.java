package com.example.carebloom.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ForumReply {

    public enum AuthorRole {
        MOTHER,
        MIDWIFE,
        MOH_OFFICE
    }

    @Id
    private String id = new ObjectId().toString();
    private String content;
    private String authorId;
    private String authorName;
    private String authorProfileImage;
    private AuthorRole authorRole;
    private LocalDateTime createdAt = LocalDateTime.now();
    private List<ForumReply> replies = new ArrayList<>();
}
