package com.xpcf.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/4/2021 12:28 AM
 */
@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    private String orderToken;
    private BigDecimal payPrice;
    private String note;
    
}
