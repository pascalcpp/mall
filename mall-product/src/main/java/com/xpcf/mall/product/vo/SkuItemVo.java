package com.xpcf.mall.product.vo;

import com.xpcf.mall.product.entity.SkuImagesEntity;
import com.xpcf.mall.product.entity.SkuInfoEntity;
import com.xpcf.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/16/2021 2:27 AM
 */
@Data
public class SkuItemVo {

    private SkuInfoEntity info;

    private Boolean hasStock = true;

    private List<SkuImagesEntity> images;

    private List<SkuItemSaleAttrVo> saleAttrs;

    private SpuInfoDescEntity desp;

    private List<SpuItemAttrGroupVo> groupAttrs;

    private SeckillInfoVo seckillInfo;

}
