package com.example.asanturagent.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    public static final String QUEUE = "offer_queue";
    public static final String ROUTING_KEY = "offer_routingKey";
    public static final String EXCHANGE = "rabbit_exchange";

    public static final String QUEUE2 = "request_queue";
    public static final String ROUTING_KEY2 = "request_routingKey";

    public static final String QUEUE3 = "accept_queue";
    public static final String ROUTING_KEY3 = "accept_routingKey";

    public static final String QUEUE4 = "active_queue";
    public static final String ROUTING_KEY4 = "active_routingKey";


    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }

    @Bean
    public Queue queue2() {
        return new Queue(QUEUE2);
    }

    @Bean
    public Queue queue3() {
        return new Queue(QUEUE3);
    }

    @Bean
    public Queue queue4() {
        return new Queue(QUEUE4);
    }


    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding binding2(@Qualifier("queue2") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY2);
    }

    @Bean
    public Binding binding3(@Qualifier("queue3") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY3);
    }

    @Bean
    public Binding binding4(@Qualifier("queue4") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY4);
    }


    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
