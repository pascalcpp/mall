package com.xpcf.mall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/29/2021 8:16 AM
 */
public class Cart {
    private List<CartItem> items;

    private Integer countNum;

    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;

        if (null != items && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }

        return count;
    }


    public Integer getCountType() {

        int count = 0;

        if (null != items && items.size() > 0) {
            for (CartItem item : items) {
                count += 1;
            }
        }

        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");

        if (null != items && items.size() > 0) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    amount = amount.add(item.getTotalPrice());
                }

            }
        }

        return amount.subtract(getReduce());

    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
