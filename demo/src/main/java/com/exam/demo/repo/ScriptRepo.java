package com.exam.demo.repo;

import com.exam.demo.AnswerScript;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScriptRepo extends MongoRepository<AnswerScript, String> {
}
