package com.xpcf.mall.ware.vo;

import lombok.Data;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/23/2020 6:32 PM
 */
@Data
public class PurchaseItemDoneVO {

    private Long itemId;
    private Integer status;
    private String reason;

}
