package com.xpcf.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/23/2020 4:06 PM
 */
@Data
public class MergeVO {
    private Long purchaseId;
    private List<Long> items;
}
