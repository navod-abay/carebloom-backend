package com.example.carebloom.dto.forum;

import lombok.Data;

@Data
public class CreateForumThreadRequest {
    private String title;
    private String content;
    private Boolean isMedical;
}
