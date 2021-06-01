package com.xpcf.mall.order.vo;

import com.xpcf.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/4/2021 2:20 PM
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;
}
