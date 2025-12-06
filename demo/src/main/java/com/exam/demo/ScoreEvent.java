package com.exam.demo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreEvent {
    private String scriptId;
    private String questionId;
    private int obtainedScore;
}