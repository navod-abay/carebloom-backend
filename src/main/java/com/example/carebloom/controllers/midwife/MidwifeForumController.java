package com.example.carebloom.controllers.midwife;

import com.example.carebloom.dto.forum.CreateReplyRequest;
import com.example.carebloom.dto.forum.ForumThreadSummaryDTO;
import com.example.carebloom.models.ForumThread;
import com.example.carebloom.services.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/midwife/forum")
@CrossOrigin(origins = "${app.cors.midwife-origin}")
public class MidwifeForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/threads")
    public ResponseEntity<Page<ForumThreadSummaryDTO>> getThreads(Pageable pageable) {
        Page<ForumThreadSummaryDTO> threads = forumService.getThreads(pageable);
        return ResponseEntity.ok(threads);
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<ForumThread> getThreadById(@PathVariable String threadId) {
        ForumThread thread = forumService.getThreadById(threadId);
        return ResponseEntity.ok(thread);
    }

    @PostMapping("/thread/{threadId}/reply")
    public ResponseEntity<ForumThread> addReply(@PathVariable String threadId, @RequestBody CreateReplyRequest request) {
        ForumThread updatedThread = forumService.addReplyAsMidwife(threadId, request);
        return ResponseEntity.ok(updatedThread);
    }
}
