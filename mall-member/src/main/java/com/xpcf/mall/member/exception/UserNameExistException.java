package com.xpcf.mall.member.exception;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/20/2021 2:31 AM
 */
public class UserNameExistException extends RuntimeException {
    public UserNameExistException() {
        super("用户名已存在");
    }
}
