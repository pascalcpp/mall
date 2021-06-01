package com.xpcf.mall.cart.vo;

import lombok.Data;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/29/2021 9:26 AM
 */
@Data
public class UserInfoTo {

    private Long userId;

    private String userKey;

    private Boolean tempUser = false;

}
