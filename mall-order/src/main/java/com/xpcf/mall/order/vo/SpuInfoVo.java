package com.xpcf.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/4/2021 7:02 PM
 */
@Data
public class SpuInfoVo {

    private Long id;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 商品描述
     */
    private String spuDescription;
    /**
     * 所属分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     *
     */
    private BigDecimal weight;
    /**
     * 上架状态[0 - 下架，1 - 上架]
     */
    private Integer publishStatus;
    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;

}
