package com.xpcf.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * ้่ดงๅๅ 
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:41:21
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

