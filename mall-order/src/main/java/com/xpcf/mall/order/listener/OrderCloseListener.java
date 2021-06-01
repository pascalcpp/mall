package com.xpcf.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.xpcf.common.to.mq.StockLockedTo;
import com.xpcf.mall.order.entity.OrderEntity;
import com.xpcf.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @date 2/9/2021 2:51 AM
 */
@RabbitListener(queues = "order.release.order.queue")
@Service
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(OrderCloseListener.class);

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        try {
            log.info("收到 order 准备 close order  {}", orderEntity.getOrderSn());
            // 手动关单
            //获得初始化的AlipayClient
//            AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);
//
//            //设置请求参数
//            AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
//            //商户订单号，商户网站订单系统中唯一订单号
//            String out_trade_no = new String(request.getParameter("WIDTCout_trade_no").getBytes("ISO-8859-1"),"UTF-8");
//            //支付宝交易号
//            String trade_no = new String(request.getParameter("WIDTCtrade_no").getBytes("ISO-8859-1"),"UTF-8");
//            //请二选一设置
//
//            alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\"," +"\"trade_no\":\""+ trade_no +"\"}");
//
//            //请求
//            String result = alipayClient.execute(alipayRequest).getBody();
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

}
