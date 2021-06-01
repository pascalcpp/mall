package com.xpcf.mall.auth.vo;

import lombok.Data;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/23/2021 10:11 AM
 */
@Data
public class SocialUser {
    private String access_token;
    private String remind_in;
    private Long expires_in;
    private String uid;
    private String isRealName;
}
