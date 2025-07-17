package com.example.carebloom.repositories;

import com.example.carebloom.models.Article;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ArticleRepository extends MongoRepository<Article, String> {
    List<Article> findByCategory(String category);
}