package com.xpcf.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.xpcf.common.exception.NoStockException;
import com.xpcf.common.to.SkuHasStockVO;
import com.xpcf.common.to.mq.OrderTo;
import com.xpcf.common.to.mq.StockDetailTo;
import com.xpcf.common.to.mq.StockLockedTo;
import com.xpcf.common.utils.R;
import com.xpcf.mall.ware.entity.WareOrderTaskDetailEntity;
import com.xpcf.mall.ware.entity.WareOrderTaskEntity;
import com.xpcf.mall.ware.feign.OrderFeignService;
import com.xpcf.mall.ware.feign.ProductFeignService;
import com.xpcf.mall.ware.service.WareOrderTaskDetailService;
import com.xpcf.mall.ware.service.WareOrderTaskService;
import com.xpcf.mall.ware.vo.OrderItemVo;
import com.xpcf.mall.ware.vo.OrderVo;
import com.xpcf.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.common.utils.Query;

import com.xpcf.mall.ware.dao.WareSkuDao;
import com.xpcf.mall.ware.entity.WareSkuEntity;
import com.xpcf.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    OrderFeignService orderFeignService;



    private void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        baseMapper.unlockStock(skuId, wareId, num);
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(taskDetailId);
        detailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(detailEntity);

    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 如果还没有这个库存记录 新增
        List<WareSkuEntity> entities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (null == entities || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //TODO 远程查询skuName 设置 如果失败事务不会rollback
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode().equals(0)) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            baseMapper.insert(wareSkuEntity);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVO> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVO> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVO vo = new SkuHasStockVO();

            //SELECT SUM(`stock`-`stock_locked`) FROM `wms_ware_sku` WHERE `sku_id` =

            Long count = baseMapper.getSkuStock(skuId);
            if (null == count) {
                count = 0L;
            }
            vo.setSkuId(skuId);
            vo.setHasStock(count > 0);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    @Transactional(rollbackFor = NoStockException.class) // imp
    public Boolean orderLockStock(WareSkuLockVo vo) {

        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);


        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            List<Long> wareIds = baseMapper.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            skuWareHasStock.setNum(item.getCount());

            return skuWareHasStock;
        }).collect(Collectors.toList());

        Boolean allLock = true;

        for (SkuWareHasStock stock : collect) {

            Long skuId = stock.getSkuId();
            Boolean skuLock = false;
            List<Long> wareIds = stock.getWareId();

            if (null == wareIds && wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }

            for (Long wareId : wareIds) {
                Long count = baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                if (count.equals(1L)) {
                    skuLock = true;
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", stock.getNum(), wareOrderTaskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    StockLockedTo stockLockedTo = new StockLockedTo();

                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);

                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    stockLockedTo.setDetail(stockDetailTo);

                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                } else {

                }
            }

            if (skuLock == false) {
                allLock = false;
                throw new NoStockException(skuId);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public void unlockStock(StockLockedTo to) {

        Long id = to.getId();
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailId);
        if (null != detailEntity) {
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode().equals(0)) {
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                });

                if (null == orderVo || orderVo.getStatus().equals(4)) {
                    if (detailEntity.getLockStatus().equals(1)){
                        unlockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum(), detailId);
                    }
                }
            } else {
                throw new RuntimeException("orderFeignService.getOrderStatus(orderSn) code error");
            }

        } else {

        }
    }

    @Transactional
    @Override
    public void unlockStock(OrderTo to) {
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOrderTaskByOrderSn(to.getOrderSn());
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", taskEntity.getId())
                        .eq("lock_status", 1)
        );
        entities.forEach(entity -> {
            // 保证接口幂等性
            if (entity.getLockStatus().equals(1)) {
                unlockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
            }
        });
    }


    @Data
    public static class SkuWareHasStock {
        private Long skuId;
        private List<Long> wareId;
        private Integer num;
    }

}