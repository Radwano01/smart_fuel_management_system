package com.example.smart_fuel_management_system.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "auth.exchange";

    public static final String EMAIL_QUEUE = "notification.email.queue";
    public static final String SMS_QUEUE = "notification.phone.number.queue";
    public static final String RESET_QUEUE = "notification.reset.password.queue";

    public static final String EMAIL_KEY = "notification.email";
    public static final String SMS_KEY = "notification.phone.number";
    public static final String RESET_KEY = "notification.reset.password";

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(SMS_QUEUE, true);
    }

    @Bean
    public Queue resetQueue() {
        return new Queue(RESET_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailQueue).to(exchange).with(EMAIL_KEY);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(smsQueue).to(exchange).with(SMS_KEY);
    }

    @Bean
    public Binding resetBinding(Queue resetQueue, TopicExchange exchange) {
        return BindingBuilder.bind(resetQueue).to(exchange).with(RESET_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
