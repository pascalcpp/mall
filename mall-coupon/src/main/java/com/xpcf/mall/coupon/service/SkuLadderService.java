package com.xpcf.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.coupon.entity.SkuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 02:56:37
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

