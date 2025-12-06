package com.exam.demo.phase2;

import com.exam.demo.ScoreEvent;
import com.exam.demo.dto.EvaluationTask;
import com.exam.demo.repo.ScriptRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HumanEvalService {

    @Autowired private ScriptRepo repo;
    @Autowired private RabbitTemplate rabbit;

    @RabbitListener(queues = "grading.subjective")
    public void gradeSubjective(EvaluationTask task) {
        System.out.println("[HumanEval] Received Task: " + task.getQuestionId());

        // 1. Simulate Human Delay
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        // 2. Fetch Script (Read-Only)
        repo.findById(task.getScriptId()).ifPresent(script -> {
            script.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(task.getQuestionId()))
                    .findFirst()
                    .ifPresent(answer -> {

                        // 3. Mock Human Grading Logic (80% marks)
                        int score = (int) (answer.getMaxMarks() * 0.8);

                        // 4. Publish Result
                        ScoreEvent result = new ScoreEvent(task.getScriptId(), task.getQuestionId(), score);
                        rabbit.convertAndSend("score-generated", result);

                        System.out.println("   [HumanEval] Manual Grade: " + score + ". Sent to Aggregator.");
                    });
        });
    }
}