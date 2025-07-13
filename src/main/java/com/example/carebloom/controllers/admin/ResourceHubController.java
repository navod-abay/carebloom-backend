package com.example.carebloom.controllers.admin;

import com.example.carebloom.models.Article;
import com.example.carebloom.models.Hint;
import com.example.carebloom.repositories.ArticleRepository;
import com.example.carebloom.repositories.HintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ResourceHubController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private HintRepository hintRepository;


    @GetMapping("/hints")
    public ResponseEntity<List<Hint>> getAllHints() {
        try {
            List<Hint> hints = hintRepository.findAll();
            System.out.println("Fetched hints: " + hints);
            return ResponseEntity.ok(hints);
        } catch (Exception e) {
            System.err.println("Error fetching hints: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/hints")
    public ResponseEntity<Hint> createHint(@RequestBody Hint hint) {
        try {
            System.out.println("Received hint data: " + hint);
            Hint savedHint = hintRepository.save(hint);
            System.out.println("Hint saved to MongoDB: " + savedHint);
            return ResponseEntity.ok(savedHint);
        } catch (Exception e) {
            System.err.println("Error creating hint: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/hints/{id}")
    public ResponseEntity<Hint> updateHint(@PathVariable String id, @RequestBody Hint hint) {
        try {
            hint.setId(id);
            Hint savedHint = hintRepository.save(hint);
            return ResponseEntity.ok(savedHint);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/hints/{id}")
    public ResponseEntity<Void> deleteHint(@PathVariable String id) {
        try {
            hintRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/articles")
    public ResponseEntity<List<Article>> getAllArticles() {
        try {
            List<Article> articles = articleRepository.findAll();
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/articles")
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        try {
            article.setCreatedAt(LocalDateTime.now().toString());
            Article savedArticle = articleRepository.save(article);
            return ResponseEntity.ok(savedArticle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable String id, @RequestBody Article article) {
        try {
            article.setId(id);
            Article savedArticle = articleRepository.save(article);
            return ResponseEntity.ok(savedArticle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable String id) {
        try {
            articleRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}