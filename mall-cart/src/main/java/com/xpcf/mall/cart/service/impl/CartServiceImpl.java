package com.xpcf.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xpcf.common.utils.R;
import com.xpcf.mall.cart.feign.ProductFeignService;
import com.xpcf.mall.cart.intercepter.CartInterceptor;
import com.xpcf.mall.cart.service.CartService;
import com.xpcf.mall.cart.vo.Cart;
import com.xpcf.mall.cart.vo.CartItem;
import com.xpcf.mall.cart.vo.SkuInfoVo;
import com.xpcf.mall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.thread.strategy.ProduceConsume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/29/2021 9:09 AM
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    public final String CART_PREFIX = "mall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> ops = getCartOps();

        String res = (String) ops.get(skuId.toString());

        if (StringUtils.isEmpty(res)) {
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfoVo = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setSkuId(skuId);
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfoVo.getSkuDefaultImg());
                cartItem.setPrice(skuInfoVo.getPrice());
                cartItem.setTitle(skuInfoVo.getSkuTitle());
            }, executor);

            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuSaleAttrValues, getSkuInfoTask).get();

            String jsonString = JSON.toJSONString(cartItem);
            ops.put(skuId.toString(), jsonString);
            return cartItem;
        } else {
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            ops.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }


    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String json = (String) ops.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        if (null != userInfoTo.getUserId()) {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);

            if (null != tempCartItems) {
                tempCartItems.forEach(item -> {
                    try {
                        addToCart(item.getSkuId(), item.getCount());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                clearCart(tempCartKey);
            }

            cart.setItems(getCartItems(cartKey));
        } else {
            cart.setItems(getCartItems(CART_PREFIX + userInfoTo.getUserKey()));
        }

        return cart;

    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        cartItem.setCheck(check.equals(1) ? true : false);
        ops.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        ops.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        // focus must String type
        ops.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        if (null == userInfoTo.getUserId()) {
            return null;
        } else {
            List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserId());
            return cartItems.stream().filter(CartItem::getCheck)
                    .map(item -> {
                        BigDecimal price = productFeignService.getPrice(item.getSkuId());
                        item.setPrice(price);
                        return item;
                    }).collect(Collectors.toList());
        }


    }


    /**
     * @return ops
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (null != userInfoTo.getUserId()) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if (null != values && values.size() > 0) {
            return values.stream().map(obj -> {
                return JSON.parseObject((String) obj, CartItem.class);
            }).collect(Collectors.toList());
        }
        return null;
    }


}
