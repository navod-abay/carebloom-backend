package com.example.carebloom.repositories;

import com.example.carebloom.models.ForumQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ForumQuestionRepository extends MongoRepository<ForumQuestion, String> {

}
