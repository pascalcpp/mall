package com.xpcf.mall.member.feign;

import com.xpcf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/11/2021 4:51 AM
 */
@FeignClient("mall-order")
public interface OrderFeignService {
    @RequestMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
