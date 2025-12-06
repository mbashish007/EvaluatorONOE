package com.exam.demo.phase1;

import com.exam.demo.AnswerScript;
import com.exam.demo.dto.EvaluationTask;
import com.exam.demo.repo.ScriptRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;


@Service
public class IngestionService {

    @Autowired private ScriptRepo repo;
    @Autowired private RabbitTemplate rabbit;

    @RabbitListener(queues = "script.submitted")
    public void processSubmission(AnswerScript script) {
        System.out.println(" [Ingestion] Splitting Script: " + script.getScriptId());

        // 1. Persist Initial State (The "Parent" Document)
        script.setStatus("PROCESSING");
        repo.save(script);

        // 2. THE TRUE SPLITTER PATTERN
        // Iterate through every single answer and fire a discrete event
        script.getAnswers().forEach(answer -> {

            // Create the granular task object
            EvaluationTask task = new EvaluationTask(
                    script.getScriptId(),
                    answer.getQuestionId(),
                    answer.getType()
            );

            // 3. Route based on type
            if ("OBJECTIVE".equalsIgnoreCase(answer.getType())) {
                // Send the TASK object, not the ID string
                rabbit.convertAndSend("grading.objective", task);
                System.out.println("   -> Split & Sent Q: " + answer.getQuestionId() + " to AutoEval");
            }
            else if ("SUBJECTIVE".equalsIgnoreCase(answer.getType())) {
                rabbit.convertAndSend("grading.subjective", task);
                System.out.println("   -> Split & Sent Q: " + answer.getQuestionId() + " to HumanEval");
            }
        });

        System.out.println("✅ [Ingestion] Split complete. " + script.getAnswers().size() + " tasks published.");
    }
}