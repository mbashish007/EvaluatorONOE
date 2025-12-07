package com.exam.demo.phase3;

import com.exam.demo.ScoreEvent;
import com.exam.demo.repo.ScriptRepo;
import com.exam.demo.AnswerScript;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class AggregatorService {

    @Autowired private ScriptRepo repo;
    @Autowired private MongoTemplate mongoTemplate; // <--- The Power Tool

    @RabbitListener(queues = "score-generated")
    public void consolidate(ScoreEvent event) {

        // 1: ATOMIC PARTIAL UPDATE "Parallel Friendly"
        // Query: Find the Script AND the specific Question inside the array
        Query query = new Query(Criteria.where("_id").is(event.getScriptId())
                .and("answers.questionId").is(event.getQuestionId()));

        // Update: Set ONLY that specific score using the positional operator ($)
        Update update = new Update().set("answers.$.obtainedScore", event.getObtainedScore());

        // Execute: This happens on DB side. No locking the whole Java object.
        mongoTemplate.updateFirst(query, update, AnswerScript.class);

        System.out.println("[Aggregator] Atomic update for Q: " + event.getQuestionId());


        // 2: CHECK COMPLETION
        // Since Step 1 is atomic, we will see the latest state.
        repo.findById(event.getScriptId()).ifPresent(script -> {

            if (script.isFullyGraded() && !"CORRECTED".equals(script.getStatus())) {

                // Calculate final totals
                script.calculateTotal();
                script.setStatus("CORRECTED");

                // Save the FINAL state (Status change)
                repo.save(script);
                System.out.println("[Aggregator] Script FINALIZED. Total Score: " + script.getTotalScore());
            }
        });
    }
}