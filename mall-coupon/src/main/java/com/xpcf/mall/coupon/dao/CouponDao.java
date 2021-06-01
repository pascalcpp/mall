package com.xpcf.mall.coupon.dao;

import com.xpcf.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 02:56:37
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
