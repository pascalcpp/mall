package com.xpcf.mall.search.controller;

import com.xpcf.common.exception.BizCodeEnum;
import com.xpcf.common.to.es.SkuESModel;
import com.xpcf.common.utils.R;
import com.xpcf.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/26/2020 1:39 PM
 */
@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSaveController {


    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels){
        Boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuESModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误: {}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if(b){
            return R.ok();
        }else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }

}
