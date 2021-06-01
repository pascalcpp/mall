package com.xpcf.common.exception;

import lombok.Getter;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/18/2020 4:04 AM
 *
 * 10 common
 * 11
 * 12
 * 13
 * 14
 * 15 member
 */
@Getter
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    TO_MANY_REQUEST(10002,"请求流量过大"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    SMS_CODE_EXCEPTION(10002,"验证码获取频率太高，请稍后再试"),
    USER_EXIST_EXCEPTION(15001, "用户名存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号存在"),
    LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION(15003, "账号密码错误"),
    NO_STOCK_EXCEPTION(21000, "商品库存不足");
    private int code;
    private String msg;

    BizCodeEnum(int code,String msg){
        this.code = code;
        this.msg = msg;
    }


}
