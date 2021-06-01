package com.xpcf.mall.product.feign;

import com.xpcf.common.to.es.SkuESModel;
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
 * @date 12/26/2020 2:15 PM
 */
@FeignClient("mall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuESModel> skuESModels);

}
