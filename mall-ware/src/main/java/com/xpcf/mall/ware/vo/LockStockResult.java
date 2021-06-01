package com.xpcf.mall.ware.vo;

import lombok.Data;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/5/2021 12:26 AM
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean lock;
}
