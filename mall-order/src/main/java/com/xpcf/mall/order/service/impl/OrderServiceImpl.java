package com.xpcf.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xpcf.common.exception.NoStockException;
import com.xpcf.common.to.mq.OrderTo;
import com.xpcf.common.to.mq.SeckillOrderTo;
import com.xpcf.common.utils.R;
import com.xpcf.common.vo.MemberRespVo;
import com.xpcf.mall.order.constant.OrderConstant;
import com.xpcf.mall.order.dao.OrderItemDao;
import com.xpcf.mall.order.entity.OrderItemEntity;
import com.xpcf.mall.order.entity.PaymentInfoEntity;
import com.xpcf.mall.order.enume.OrderStatusEnum;
import com.xpcf.mall.order.feign.CartFeignService;
import com.xpcf.mall.order.feign.MemberFeignService;
import com.xpcf.mall.order.feign.ProductFeignService;
import com.xpcf.mall.order.feign.WmsFeignService;
import com.xpcf.mall.order.interceptor.LoginUserInterceptor;
import com.xpcf.mall.order.service.OrderItemService;
import com.xpcf.mall.order.service.PaymentInfoService;
import com.xpcf.mall.order.to.OrderCreateTo;
import com.xpcf.mall.order.vo.*;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.common.utils.Query;

import com.xpcf.mall.order.dao.OrderDao;
import com.xpcf.mall.order.entity.OrderEntity;
import com.xpcf.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    PaymentInfoService paymentInfoService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );
        OrderService orderService = (OrderService) AopContext.currentProxy();
        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // asyn thread 不同 RequestContextHolder使用错误

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> getCartItemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<Long> collect = orderConfirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            System.out.println(collect);
            System.out.println(data);
            if (null != data) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(map);
            }
        }, executor);


        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // TODO order 幂等性
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(getCartItemsFuture, getAddressFuture).get();

        return orderConfirmVo;
    }

    @Override
    @Transactional
//    @GlobalTransactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        submitVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        // atomic
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (result.equals(1L)) {

            OrderCreateTo order = creatOrder();
            BigDecimal payPrice = vo.getPayPrice();
            BigDecimal curPayPrice = order.getPayPrice();
            if (Math.abs(payPrice.subtract(curPayPrice).doubleValue()) < 0.01) {
                // TODO save order
                saveOrder(order);

                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                OrderEntity orderEntity = order.getOrder();
                wareSkuLockVo.setOrderSn(orderEntity.getOrderSn());
                wareSkuLockVo.setLocks(order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList()));
                // TODO remote lock stock
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode().equals(0)) {
                    responseVo.setCode(0);
                    responseVo.setOrder(orderEntity);
//                    int a = 10/0;
                    //  TODO remote
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderEntity);
                    return responseVo;
                } else {
                    NoStockException exception = r.getData(new TypeReference<NoStockException>() {
                    });
                    throw exception;
                }

            } else {
                // verify price failed
                responseVo.setCode(2);
                return responseVo;
            }

        } else {
            // verify token failed
            responseVo.setCode(1);
            return responseVo;
        }

    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity entity = getById(orderEntity.getId());

        if (entity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            entity.setStatus(OrderStatusEnum.CANCLED.getCode());
            updateById(entity);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(entity, orderTo);
            try {
                // 在mysql 记录消息
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = getOrderByOrderSn(orderSn);
        List<OrderItemEntity> entities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        payVo.setOut_trade_no(orderSn);
        payVo.setTotal_amount(orderEntity.getTotalAmount().setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setSubject(entities.get(0).getSkuName());
        payVo.setBody(entities.get(0).getSkuAttrsVals());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        IPage<OrderEntity> page = page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> collect = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(collect);

        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo vo) {
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(paymentInfoEntity);

        String status = vo.getTrade_status();
        if (status.equals("TRADE_FINISHED") || status.equals("TRADE_SUCCESS")) {
            String orderSn = vo.getOut_trade_no();
            baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
            return "success";
        }
        return "failed";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        BigDecimal amount = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + seckillOrderTo.getNum()));
        orderEntity.setPayAmount(amount);
        orderEntity.setTotalAmount(amount);
        save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(amount);
        orderItemEntity.setSkuQuantity(seckillOrderTo.getNum());
        //TODO
        orderItemEntity.setSkuAttrsVals("test");
        orderItemEntity.setSkuName(seckillOrderTo.getSkuId().toString());
        orderItemService.save(orderItemEntity);
        // TODO 保存订单项 进行其他设置
    }


    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        save(orderEntity);

        orderItemService.saveBatch(order.getOrderItems());

    }

    private OrderCreateTo creatOrder() {

        OrderCreateTo orderCreateTo = new OrderCreateTo();
        String orderSn = IdWorker.getTimeId();

        OrderEntity orderEntity = buildOrder(orderSn);
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);
        computePrice(orderEntity, orderItems);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItems);
        orderCreateTo.setFare(orderEntity.getFreightAmount());
        orderCreateTo.setPayPrice(orderEntity.getPayAmount());

        return orderCreateTo;
    }


    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {

//        BigDecimal total = orderItems.stream().map(OrderItemEntity::getRealAmount)
//                .reduce((i1, i2) -> i1.add(i2)).get();

        BigDecimal total = new BigDecimal("0.0");
        BigDecimal totalCoupon = new BigDecimal("0.0");
        BigDecimal totalIntegration = new BigDecimal("0.0");
        BigDecimal totalPromotion = new BigDecimal("0.0");
        Integer totalGift = 0;
        Integer totalGrowth = 0;

        for (OrderItemEntity orderItem : orderItems) {
            total = total.add(orderItem.getRealAmount());
            totalCoupon = totalCoupon.add(orderItem.getCouponAmount());
            totalPromotion = totalPromotion.add(orderItem.getPromotionAmount());
            totalIntegration = totalIntegration.add(orderItem.getIntegrationAmount());
            totalGift =  totalGift + orderItem.getGiftIntegration();
            totalGrowth = totalGrowth + orderItem.getGiftGrowth();
        }

        orderEntity.setIntegration(totalGift);
        orderEntity.setGrowth(totalGrowth);

        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(totalPromotion);
        orderEntity.setIntegrationAmount(totalIntegration);
        orderEntity.setCouponAmount(totalCoupon);


        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        orderEntity.setDeleteStatus(0);


    }

    private OrderEntity buildOrder(String orderSn) {

        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberRespVo.getId());


        R r = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = r.getData(new TypeReference<FareVo>() {

        });

        orderEntity.setFreightAmount(fareVo.getFare());
        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setBillReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
        orderEntity.setCreateTime(new Date());
        return orderEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        if (null != cartItems && cartItems.size() > 0) {
            List<OrderItemEntity> collect = cartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        R r = productFeignService.getSpunInfoBySkuId(item.getSkuId());
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());

        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";"));
        orderItemEntity.setSkuQuantity(item.getCount());


        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal real = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());

        orderItemEntity.setRealAmount(real);

        return orderItemEntity;
    }

}