package com.example.carebloom.dto.forum;

import lombok.Data;

import java.util.List;

@Data
public class CreateReplyRequest {
    private String content;
    private List<String> path;
}
