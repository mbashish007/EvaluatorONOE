package com.exam.demo.phase3;

import com.exam.demo.ScoreEvent;

import com.exam.demo.repo.ScriptRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AggregatorService {

    @Autowired private ScriptRepo repo;

    @RabbitListener(queues = "score-generated")
    public void consolidate(ScoreEvent event) {
        // 1. Fetch the Document
        repo.findById(event.getScriptId()).ifPresent(script -> {

            // 2. Update the specific answer (Stateful change)
            script.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(event.getQuestionId()))
                    .findFirst()
                    .ifPresent(a -> a.setObtainedScore(event.getObtainedScore()));

            // 3. Recalculate Total
            script.calculateTotal();

            // 4. Check if we are done
            if (script.isFullyGraded()) {
                script.setStatus("CORRECTED");
                System.out.println("✅ [Aggregator] Script " + script.getScriptId() + " FINALIZED. Total Score: " + script.getTotalScore());
            } else {
                System.out.println("[Aggregator] Partial update saved for " + event.getQuestionId());
            }

            // 5. Save State
            repo.save(script);
        });
    }
}