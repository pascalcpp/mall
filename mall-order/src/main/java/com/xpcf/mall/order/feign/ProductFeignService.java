package com.xpcf.mall.order.feign;

import com.xpcf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/4/2021 7:00 PM
 */
@FeignClient("mall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpunInfoBySkuId(@PathVariable("id") Long skuId);
}
