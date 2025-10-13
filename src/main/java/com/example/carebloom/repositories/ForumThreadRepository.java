package com.example.carebloom.repositories;

import com.example.carebloom.models.ForumThread;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumThreadRepository extends MongoRepository<ForumThread, String> {
}
