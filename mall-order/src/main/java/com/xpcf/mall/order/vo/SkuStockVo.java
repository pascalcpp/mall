package com.xpcf.mall.order.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/2/2021 6:52 PM
 */
@Data
public class SkuStockVo {
    private Long skuId;
    private Boolean hasStock;

}
