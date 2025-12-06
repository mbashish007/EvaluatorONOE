package com.exam.demo.phase2;


import com.exam.demo.MockQuestionBank;
import com.exam.demo.QuestionMetadata;
import com.exam.demo.dto.EvaluationTask;
import com.exam.demo.dto.ScoreEvent;
import com.exam.demo.repo.ScriptRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AutoEvalService {

    @Autowired private ScriptRepo repo;
    @Autowired private RabbitTemplate rabbit;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String QB_API_URL = "http://localhost:8084/external/qb/keys/";

    @RabbitListener(queues = "grading.objective")
    public void gradeObjective(EvaluationTask task) {
        System.out.println(" [AutoEval] Received Task: " + task.getQuestionId());

        // 1. Fetch Script (Read-Only) to get student response
        repo.findById(task.getScriptId()).ifPresent(script -> {
            script.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(task.getQuestionId()))
                    .findFirst()
                    .ifPresent(answer -> {

                        // 2. Call Mock QB for Key
                        QuestionMetadata key =
                                restTemplate.getForObject(QB_API_URL + answer.getQuestionId(), QuestionMetadata.class);

                        // 3. Calculate Score
                        int score = 0;
                        if (key != null && key.getCorrectOption().equalsIgnoreCase(answer.getResponse())) {
                            score = answer.getMaxMarks();
                        }

                        // 4. Publish Result (Fire & Forget)
                        com.exam.demo.dto.ScoreEvent result = new ScoreEvent(task.getScriptId(), task.getQuestionId(), score);
                        rabbit.convertAndSend("score-generated", result);

                        System.out.println("  [AutoEval] Calculated Score: " + score + ". Sent to Aggregator.");
                    });
        });
    }
}