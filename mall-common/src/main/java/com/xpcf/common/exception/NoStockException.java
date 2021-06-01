package com.xpcf.common.exception;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/5/2021 2:42 PM
 */
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId) {
        super("sku id: " + skuId + "没有足够库存");
        setSkuId(skuId);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}

