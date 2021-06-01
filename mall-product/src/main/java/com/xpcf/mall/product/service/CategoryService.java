package com.xpcf.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.product.entity.CategoryEntity;
import com.xpcf.mall.product.vo.Catalog2VO;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 02:06:34
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);


    /**
     * 找到完整路径
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Categories();

    Map<String, List<Catalog2VO>> getCatalogJson();
}

