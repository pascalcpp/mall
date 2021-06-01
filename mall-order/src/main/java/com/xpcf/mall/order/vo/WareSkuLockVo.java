package com.xpcf.mall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/5/2021 12:15 AM
 */
@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> locks;

}
