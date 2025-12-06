package com.exam.demo;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
    @Bean public Queue submissionQueue() { return new Queue("script.submitted", true); }
    @Bean public Queue objQueue() { return new Queue("grading.objective", true); }
    @Bean public Queue subjQueue() { return new Queue("grading.subjective", true); }
    @Bean public Queue scoreQueue() { return new Queue("score-generated", true); }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}