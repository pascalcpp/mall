package com.xpcf.mall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.xpcf.common.to.mq.OrderTo;
import com.xpcf.common.to.mq.StockDetailTo;
import com.xpcf.common.to.mq.StockLockedTo;
import com.xpcf.common.utils.R;
import com.xpcf.mall.ware.dao.WareSkuDao;
import com.xpcf.mall.ware.entity.WareOrderTaskDetailEntity;
import com.xpcf.mall.ware.entity.WareOrderTaskEntity;
import com.xpcf.mall.ware.feign.OrderFeignService;
import com.xpcf.mall.ware.service.WareOrderTaskDetailService;
import com.xpcf.mall.ware.service.WareOrderTaskService;
import com.xpcf.mall.ware.service.WareSkuService;
import com.xpcf.mall.ware.vo.OrderVo;
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
 * @date 2/9/2021 12:06 AM
 */
@RabbitListener(queues = "stock.release.stock.queue")
@Service
@Slf4j
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;


    /**
     * consume 保证幂等性
     * @param to
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        try {
            log.info("收到stock release msg");
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("{}", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {

        try {
            log.info("订单取消 准备解锁库存");
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("{}", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


}
