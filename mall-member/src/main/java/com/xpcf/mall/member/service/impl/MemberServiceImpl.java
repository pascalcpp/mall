package com.xpcf.mall.member.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xpcf.common.utils.HttpUtils;
import com.xpcf.mall.member.dao.MemberLevelDao;
import com.xpcf.mall.member.entity.MemberLevelEntity;
import com.xpcf.mall.member.exception.PhoneExistException;
import com.xpcf.mall.member.exception.UserNameExistException;
import com.xpcf.mall.member.vo.MemberLoginVo;
import com.xpcf.mall.member.vo.MemberRegistVo;
import com.xpcf.mall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.common.utils.Query;

import com.xpcf.mall.member.dao.MemberDao;
import com.xpcf.mall.member.entity.MemberEntity;
import com.xpcf.mall.member.service.MemberService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    public static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) throws RuntimeException {

        // 检查唯一性
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        MemberEntity memberEntity = new MemberEntity();
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();

        memberEntity.setLevelId(levelEntity.getId());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());

        // encrypt
        passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));

        if (count > 0) {
            throw new PhoneExistException();
        }

    }

    @Override
    public void checkUserNameUnique(String userName) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));

        if (count > 0) {
            throw new UserNameExistException();
        }

    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAccount = vo.getLoginAccount();
        String password = vo.getPassword();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));

        if (null == memberEntity) {
            return null;
        } else {

            // rawPassword , passwordDB
            if (passwordEncoder.matches(password, memberEntity.getPassword())) {
                return memberEntity;
            } else {
                return null;
            }

        }

    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {

        // 登陆注册业务
        String uid = socialUser.getUid();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (null != memberEntity) {

            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            baseMapper.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;


        } else {
            MemberEntity regist = new MemberEntity();
            try {
                Map<String, String> querys = new HashMap<>();
                querys.put("access_token", socialUser.getAccess_token());
                querys.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), querys);
                if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");

                    regist.setNickname(name);
                    regist.setGender((!StringUtils.isEmpty(gender)) ? (gender.equalsIgnoreCase("m") ? 1 : 0) : 1);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            baseMapper.insert(regist);
            return regist;
        }

    }

}