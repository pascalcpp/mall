package com.xpcf.mall.product.feign;

import com.xpcf.common.to.SkuHasStockVO;
import com.xpcf.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/26/2020 12:31 PM
 */
@FeignClient("mall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hastock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
