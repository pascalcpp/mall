package com.xpcf.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 02:56:37
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

