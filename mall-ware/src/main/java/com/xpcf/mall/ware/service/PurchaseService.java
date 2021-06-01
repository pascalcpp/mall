package com.xpcf.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.ware.entity.PurchaseEntity;
import com.xpcf.mall.ware.vo.MergeVO;
import com.xpcf.mall.ware.vo.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:46:53
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVO mergeVO);

    void received(List<Long> ids);

    void done(PurchaseDoneVO purchaseDoneVO);
}

