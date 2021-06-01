package com.xpcf.mall.member.exception;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/20/2021 2:31 AM
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号已存在");
    }
}
