package com.xpcf.mall.order.vo;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/2/2021 12:24 AM
 */
public class OrderConfirmVo {

    @Getter
    @Setter
    private List<MemberAddressVo> address;

    @Getter
    @Setter
    private List<OrderItemVo> items;

    @Getter
    @Setter
    private Integer integration;

    @Getter
    @Setter
    private String orderToken;

    @Getter
    @Setter
    private Map<Long, Boolean> stocks;

    public Integer getCount() {
        int num = 0;
        if (null != items) {
            for (OrderItemVo item : items) {
                num += item.getCount();
            }
        }
        return num;
    }

    // total price
//    private BigDecimal total;

    // really price
//    private BigDecimal payPrice;

    public BigDecimal getTotal() {
        BigDecimal amount = new BigDecimal("0");

        if (null != items) {
            for (OrderItemVo item : items) {
                amount = amount.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }

        return amount;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
