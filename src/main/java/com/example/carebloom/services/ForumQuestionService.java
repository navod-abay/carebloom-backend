package com.example.carebloom.services;

import com.example.carebloom.models.ForumQuestion;
import com.example.carebloom.repositories.ForumQuestionRepository;
import org.springframework.stereotype.Service;

public class ForumQuestionService {
    private final ForumQuestionRepository forumQuestionRepository;
    private final AiService aiService; // classification service

    public ForumQuestionService(ForumQuestionRepository forumQuestionRepository, AiService aiService) {
        this.forumQuestionRepository = forumQuestionRepository;
        this.aiService = aiService;
    }

    public ForumQuestion addQuestion(ForumQuestion question) {
        String category = aiService.classifyQuestion(question.getContent());
        question.setCategory(category);
        return forumQuestionRepository.save(question);
    }

}
