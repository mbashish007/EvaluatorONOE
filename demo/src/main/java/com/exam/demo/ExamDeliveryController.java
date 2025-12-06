package com.exam.demo;



import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
public class ExamDeliveryController {

    @Autowired private RabbitTemplate rabbit;

    @PostMapping("/submit-exam")
    public String submit(@RequestBody AnswerScript script) {
        // 1. Generate ID if missing
        if (script.getScriptId() == null) {
            script.setScriptId(UUID.randomUUID().toString());
        }

        // 2. Set Status
        script.setStatus("SUBMITTED");

        System.out.println("[ExamModule] Publishing script submission event: " + script.getScriptId());

        // 3. Fire to Entry Queue
        rabbit.convertAndSend("script.submitted", script);

        return "Submission Accepted. Reference ID: " + script.getScriptId();
    }
}