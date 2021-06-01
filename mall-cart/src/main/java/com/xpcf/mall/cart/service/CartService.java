package com.xpcf.mall.cart.service;

import com.xpcf.mall.cart.vo.Cart;
import com.xpcf.mall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/29/2021 9:09 AM
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * get cartItem
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getCurrentUserCartItems();
}
