package com.example.carebloom.controllers;

import com.example.carebloom.models.ForumQuestion;
import com.example.carebloom.services.ForumQuestionService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;;

@RestController
public class ForumQuestionController {
    private final ForumQuestionService forumQuestionService;

    public ForumQuestionController(ForumQuestionService forumQuestionService) {
        this.forumQuestionService = forumQuestionService;
    }

    @PostMapping("/forum-questions")
    public ResponseEntity<ForumQuestion> addQuestion(HttpServletRequest request, @RequestBody ForumQuestion question) {

        String userID = (String) request.getAttribute("userID");
        question.setUserID(userID);
        ForumQuestion savedQuestion = forumQuestionService.addQuestion(question);
        return ResponseEntity.ok(savedQuestion);
    }
}
