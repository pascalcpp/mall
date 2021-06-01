package com.xpcf.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.mall.member.entity.MemberEntity;
import com.xpcf.mall.member.exception.PhoneExistException;
import com.xpcf.mall.member.exception.UserNameExistException;
import com.xpcf.mall.member.vo.MemberLoginVo;
import com.xpcf.mall.member.vo.MemberRegistVo;
import com.xpcf.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:32:23
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo) throws RuntimeException;

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UserNameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser vo) throws Exception;
}

