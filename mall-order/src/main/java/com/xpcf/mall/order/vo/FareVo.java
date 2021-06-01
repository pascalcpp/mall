package com.xpcf.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/4/2021 6:06 PM
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
