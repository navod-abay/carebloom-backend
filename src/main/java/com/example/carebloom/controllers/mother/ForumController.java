package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.forum.CreateForumThreadRequest;
import com.example.carebloom.dto.forum.CreateReplyRequest;
import com.example.carebloom.dto.forum.ForumThreadSummaryDTO;
import com.example.carebloom.models.ForumThread;
import com.example.carebloom.services.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mothers/forum")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @PostMapping("/thread")
    public ResponseEntity<ForumThread> createThread(@RequestBody CreateForumThreadRequest request) {
        ForumThread createdThread = forumService.createThread(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdThread);
    }

    @GetMapping("/threads")
    public ResponseEntity<Page<ForumThreadSummaryDTO>> getThreads(Pageable pageable) {
        Page<ForumThreadSummaryDTO> threads = forumService.getThreads(pageable);
        return ResponseEntity.ok(threads);
    }

    @PostMapping("/thread/{threadId}/reply")
    public ResponseEntity<ForumThread> addReplyToThread(@PathVariable String threadId, @RequestBody CreateReplyRequest request) {
        ForumThread updatedThread = forumService.addReplyToThread(threadId, request);
        return ResponseEntity.ok(updatedThread);
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<ForumThread> getThreadById(@PathVariable String threadId) {
        ForumThread thread = forumService.getThreadById(threadId);
        return ResponseEntity.ok(thread);
    }
}
