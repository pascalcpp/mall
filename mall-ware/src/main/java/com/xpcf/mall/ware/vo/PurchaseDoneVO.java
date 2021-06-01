package com.xpcf.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/23/2020 5:50 PM
 */
@Data
public class PurchaseDoneVO {

    @NotNull
    private Long id;

    private List<PurchaseItemDoneVO> items;

}
