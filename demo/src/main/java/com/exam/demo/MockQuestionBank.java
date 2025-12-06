package com.exam.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/external/qb")
public class MockQuestionBank {

    @GetMapping("/keys/{questionId}")
    public QuestionMetadata getKey(@PathVariable String questionId) {
        // Mock Database Logic
        // Stub query for Question Bank DB
        String correctOption = "A"; // Default correct answer
        return new QuestionMetadata(questionId, correctOption);
    }


}