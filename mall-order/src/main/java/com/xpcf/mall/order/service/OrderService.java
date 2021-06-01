package com.xpcf.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.to.mq.SeckillOrderTo;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.order.entity.OrderEntity;
import com.xpcf.mall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:41:21
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 返回订单确认页需要数据
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}

