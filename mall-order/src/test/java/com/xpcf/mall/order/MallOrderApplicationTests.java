package com.xpcf.mall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class MallOrderApplicationTests {


    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void test1() {
        Queue queue = new Queue("hellopasdzxzxxz", true, false, false);
    }

    @Test
    public void receiveMessage() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        rabbitTemplate.setReceiveTimeout(100000000);

        CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
            Message message = rabbitTemplate.receive("test1");
            System.out.println(Thread.currentThread().getName() + "" + message);
        }, executorService);

        CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
            Message message = rabbitTemplate.receive("test1");
            System.out.println(Thread.currentThread().getName() + "" + message);
        }, executorService);

        TimeUnit.SECONDS.sleep(60);

    }

}
