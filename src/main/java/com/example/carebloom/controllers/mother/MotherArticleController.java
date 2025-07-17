package com.example.carebloom.controllers.mother;

import com.example.carebloom.models.Article;
import com.example.carebloom.services.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mothers/articles")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherArticleController {

    private static final Logger logger = LoggerFactory.getLogger(MotherArticleController.class);

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles(@RequestHeader("Authorization") String idToken) {
        try {
            logger.info("Fetching all articles for mother");
            List<Article> articles = articleService.getAllArticles();
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            logger.error("Error fetching articles: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Article>> getArticlesByCategory(
            @RequestHeader("Authorization") String idToken,
            @PathVariable String categoryName) {
        try {
            logger.info("Fetching articles for category: {}", categoryName);
            List<Article> articles = articleService.getArticlesByCategory(categoryName);
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            logger.error("Error fetching articles for category {}: {}", categoryName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(
            @RequestHeader("Authorization") String idToken,
            @PathVariable String id) {
        try {
            logger.info("Fetching article with id: {}", id);
            Article article = articleService.getArticleById(id);
            if (article != null) {
                return ResponseEntity.ok(article);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching article with id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
