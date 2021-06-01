package com.xpcf.mall.seckill.feign;

import com.xpcf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/14/2021 2:22 AM
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();
}
