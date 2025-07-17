package com.example.carebloom.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carebloom.models.Article;
import com.example.carebloom.repositories.ArticleRepository;

@Service
public class ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    private ArticleRepository articleRepository;

    public List<Article> getAllArticles() {
        logger.info("Fetching all articles from database");
        return articleRepository.findAll();
    }

    public List<Article> getArticlesByCategory(String category) {
        logger.info("Fetching articles for category: {}", category);
        return articleRepository.findByCategory(category);
    }

    public Article getArticleById(String id) {
        logger.info("Fetching article with id: {}", id);
        Optional<Article> article = articleRepository.findById(id);
        return article.orElse(null);
    }
}
