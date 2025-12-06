package com.exam.demo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "scripts")
public class AnswerScript {
    @Id
    private String scriptId;
    private String studentId;
    private String status; // SUBMITTED, PROCESSING, CORRECTED

    // Unified polymorphic list
    private List<AnswerItem> answers;

    // Final consolidated score
    private Integer totalScore;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {
        private String questionId;
        private String type;        // "OBJECTIVE" or "SUBJECTIVE"
        private String response;
        private Integer maxMarks;   // Weightage from Question Paper
        private Integer obtainedScore; // Initially null
    }

    // Helper to calculate total
    public void calculateTotal() {
        this.totalScore = this.answers.stream()
                .filter(a -> a.getObtainedScore() != null)
                .mapToInt(AnswerItem::getObtainedScore)
                .sum();
    }

    // Helper to check completion
    public boolean isFullyGraded() {
        return this.answers.stream().noneMatch(a -> a.getObtainedScore() == null);
    }
}