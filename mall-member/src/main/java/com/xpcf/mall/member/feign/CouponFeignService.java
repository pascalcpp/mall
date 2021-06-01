package com.xpcf.mall.member.feign;

import com.xpcf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/13/2020 12:22 AM
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R coupons();

}
