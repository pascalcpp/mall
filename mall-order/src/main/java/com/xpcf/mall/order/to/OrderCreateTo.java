package com.xpcf.mall.order.to;

import com.xpcf.mall.order.entity.OrderEntity;
import com.xpcf.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/4/2021 2:50 PM
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;

}
