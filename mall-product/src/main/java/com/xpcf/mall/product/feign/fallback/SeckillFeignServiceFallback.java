package com.xpcf.mall.product.feign.fallback;

import com.xpcf.common.exception.BizCodeEnum;
import com.xpcf.common.utils.R;
import com.xpcf.mall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/17/2021 1:37 AM
 */
@Component
@Slf4j
public class SeckillFeignServiceFallback implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.error("熔断方法调用");
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }
}
