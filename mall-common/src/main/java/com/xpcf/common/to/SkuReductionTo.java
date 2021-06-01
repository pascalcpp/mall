package com.xpcf.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/22/2020 6:58 PM
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;

}
