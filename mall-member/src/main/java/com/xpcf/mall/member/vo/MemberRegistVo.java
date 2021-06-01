package com.xpcf.mall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/20/2021 12:33 AM
 */
@Data
public class MemberRegistVo {

    private String userName;
    private String password;
    private String phone;
}
