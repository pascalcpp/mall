package com.xpcf.mall.product;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xpcf.mall.product.dao.AttrGroupDao;
import com.xpcf.mall.product.entity.BrandEntity;
import com.xpcf.mall.product.service.BrandService;
import com.xpcf.mall.product.service.SkuSaleAttrValueService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallProductApplicationTests {

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;


    @Test
    public void test1() {
        System.out.println(skuSaleAttrValueService.getSaleAttrsBySpuId(10L));
    }
}
