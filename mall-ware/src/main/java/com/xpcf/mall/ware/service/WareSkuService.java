package com.xpcf.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.to.SkuHasStockVO;
import com.xpcf.common.to.mq.OrderTo;
import com.xpcf.common.to.mq.StockLockedTo;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.ware.entity.WareSkuEntity;
import com.xpcf.mall.ware.vo.LockStockResult;
import com.xpcf.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:46:53
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVO> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);


    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo to);
}

