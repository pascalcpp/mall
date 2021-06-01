package com.xpcf.mall.order.feign;

import com.xpcf.common.utils.R;
import com.xpcf.mall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/2/2021 6:48 PM
 */
@FeignClient("mall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hastock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long id);


    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);

}
