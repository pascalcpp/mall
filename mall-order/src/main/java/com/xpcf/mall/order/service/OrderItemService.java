package com.xpcf.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.order.entity.OrderItemEntity;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:41:21
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

