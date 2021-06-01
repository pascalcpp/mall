package com.xpcf.mall.ware.feign;

import com.xpcf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/23/2020 7:37 PM
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    /**
     * 1). @FeignClient("mall-product") 直接发请求
     *
     * 2). @FeignClient("mall-gateway") 向网关发请求 注意路径不同
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
