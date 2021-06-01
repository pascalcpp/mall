package com.xpcf.mall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/14/2021 2:25 AM
 */
@Data
public class SeckillSkuVo {
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;




}
