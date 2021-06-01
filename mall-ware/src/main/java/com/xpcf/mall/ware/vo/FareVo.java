package com.xpcf.mall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/3/2021 12:04 AM
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
