package com.xpcf.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.xpcf.common.to.mq.SeckillOrderTo;
import com.xpcf.mall.order.entity.OrderEntity;
import com.xpcf.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/15/2021 5:24 AM
 */
@RabbitListener(queues = "order.seckill.order.queue")
@Service
@Slf4j
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws Exception {
        try {
            log.info("准备创建秒杀单信息");
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

}
