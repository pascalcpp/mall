package com.xpcf.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xpcf.common.to.mq.SeckillOrderTo;
import com.xpcf.common.utils.R;
import com.xpcf.common.vo.MemberRespVo;
import com.xpcf.mall.seckill.feign.CouponFeignService;
import com.xpcf.mall.seckill.feign.ProductFeignService;
import com.xpcf.mall.seckill.interceptor.LoginUserInterceptor;
import com.xpcf.mall.seckill.service.SeckillService;
import com.xpcf.mall.seckill.to.SeckillSkuRedisTo;
import com.xpcf.mall.seckill.vo.SeckillSessionsWithSkus;
import com.xpcf.mall.seckill.vo.SeckillSkuVo;
import com.xpcf.mall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/13/2021 7:41 AM
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUSKILL_CACHE_PREFIX = "seckill:skus";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode().equals(0)) {
            List<SeckillSessionsWithSkus> sessionsData = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            if (null != sessionsData) {
                saveSessionInfos(sessionsData);
                saveSessionSkuInfos(sessionsData);
            }

        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        long curTime = System.currentTimeMillis();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        if (null != keys) {
            for (String key : keys) {
                String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                long startTime = Long.parseLong(s[0]);
                long endTime = Long.parseLong(s[1]);
                if (curTime >= startTime && curTime <= endTime) {
                    List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUSKILL_CACHE_PREFIX);
                    List<String> objects = hashOps.multiGet(range);
                    if (null != objects) {
                        List<SeckillSkuRedisTo> collect = objects.stream().map(object -> {
                            SeckillSkuRedisTo seckillSkuRedisTo = JSONObject.parseObject(object.toString(), SeckillSkuRedisTo.class);
                            // 当前秒杀开始需要随机码
                            return seckillSkuRedisTo;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUSKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        String reg = "\\d_" + skuId;
        for (String key : keys) {
            if (Pattern.matches(reg, key)) {
                SeckillSkuRedisTo to = JSON.parseObject(hashOps.get(key), SeckillSkuRedisTo.class);
                long current = System.currentTimeMillis();
                if (current >= to.getStartTime() && current <= to.getEndTime()) {
                    return to;
                } else {
                    to.setRandomCode(null);
                }

                return to;

            }
        }
        return null;
    }

    /**
     * TODO 秒杀锁库存 为每个数据加ttl 秒杀后续的流程未完成
     *
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        long t1 = System.currentTimeMillis();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUSKILL_CACHE_PREFIX);
        String info = hashOps.get(killId);
        if (StringUtils.isEmpty(info)) {
            return null;
        } else {
            SeckillSkuRedisTo to = JSON.parseObject(info, SeckillSkuRedisTo.class);
            // 检验合法性
            long current = System.currentTimeMillis();
            long ttl = to.getEndTime() - current;
            // 1、校验时间合法性
            if (current >= to.getStartTime() && current <= to.getEndTime()) {

                // 2、校验code 和 id
                String randomCode = to.getRandomCode();
                Long skuId = to.getSkuId();
                Long promotionSessionId = to.getPromotionSessionId();
                String id = promotionSessionId + "_" + skuId;
                if (id.equals(killId) && randomCode.equals(key)) {
                    // 3、检验购买数量
                    if (num > 0 && num <= to.getSeckillLimit().intValue()) {
                        // 4、校验是否重复购买 保证幂等性
                        String redisKey = memberRespVo.getId() + "_" + promotionSessionId + "_" + skuId;
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                                boolean b = semaphore.tryAcquire(num);
                                if (b) {
                                    // 秒杀成功
                                    // 发送mq 快速下单
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(timeId);
                                    seckillOrderTo.setMemberId(memberRespVo.getId());
                                    seckillOrderTo.setNum(num);
                                    seckillOrderTo.setPromotionSessionId(promotionSessionId);
                                    seckillOrderTo.setSkuId(skuId);
                                    seckillOrderTo.setSeckillPrice(to.getSeckillPrice());
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                    log.warn("use {} ms", System.currentTimeMillis() - t1);
                                    return timeId;
                                } else {
                                    return null;
                                }


                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }

                } else {
                    return null;
                }

            } else {
                return null;
            }
        }

    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessionsData) {
        sessionsData.forEach(session -> {
            long start = session.getStartTime().getTime();
            long end = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + start + "_" + end;
            if (!redisTemplate.hasKey(key)) {
                List<String> skuIds = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessionsData){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUSKILL_CACHE_PREFIX);
        sessionsData.forEach(session -> {
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                String randomCode = UUID.randomUUID().toString().replace("-", "");
                String hashKey = seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString();
                if (!hashOps.hasKey(hashKey)) {
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (r.getCode().equals(0)) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuRedisTo.setSkuInfo(skuInfo);
                    }
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());
                    seckillSkuRedisTo.setRandomCode(randomCode);

                    BeanUtils.copyProperties(seckillSkuVo, seckillSkuRedisTo);

                    String jsonString = JSONObject.toJSONString(seckillSkuRedisTo);
                    hashOps.put(hashKey, jsonString);

                    String semaphoreKey = SKU_STOCK_SEMAPHORE + randomCode;
                    //限流
                    RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                    // TODO lock ware
                }

            });
        });
    }


}
