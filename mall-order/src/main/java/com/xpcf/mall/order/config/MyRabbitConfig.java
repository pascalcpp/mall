package com.xpcf.mall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/31/2021 5:36 PM
 */
@Configuration
@Slf4j
public class MyRabbitConfig {


    @Bean
    ApplicationRunner rabbitRunner(RabbitTemplate rabbitTemplate, ConnectionFactory cf) {
        return args -> {
            initRabbitTemplate(rabbitTemplate);
//            cf.createConnection().close();
        };
    }




//    @Autowired
//    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    public void initRabbitTemplate(RabbitTemplate rabbitTemplate) {
        // exchange callback
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("correlation: {}=== ack: {} === cause: {}", correlationData, ack, cause);
            }
        });

        // failed callback queue
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("failed message{} === replyCode{} === replyText{} === exchange{} === routingKey{} === ", message, replyCode, replyText, exchange, routingKey);
            }
        });
    }

}
