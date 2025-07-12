package com.example.carebloom.services.admin;

import com.example.carebloom.models.Article;
import com.example.carebloom.models.Hint;
import com.example.carebloom.repositories.ArticleRepository;
import com.example.carebloom.repositories.HintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceHubService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private HintRepository hintRepository;

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Article saveArticle(Article article) {
        if (article.getCreatedAt() == null) {
            article.setCreatedAt(java.time.LocalDateTime.now().toString());
        }
        return articleRepository.save(article);
    }

    public void deleteArticle(String id) {
        articleRepository.deleteById(id);
    }

    public List<Hint> getAllHints() {
        return hintRepository.findAll();
    }

    public Hint saveHint(Hint hint) {
        return hintRepository.save(hint);
    }

    public void deleteHint(String id) {
        hintRepository.deleteById(id);
    }
}