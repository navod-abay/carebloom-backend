package com.example.carebloom.repositories;

import com.example.carebloom.models.ForumThread;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForumThreadRepository extends MongoRepository<ForumThread, String> {
    
    @Query("{'category': ?0}")
    long countByCategory(ForumThread.Category category);
    
    @Query("{'category': ?0}")
    List<ForumThread> findTop10ByCategoryOrderByCreatedAtDesc(ForumThread.Category category);
}
