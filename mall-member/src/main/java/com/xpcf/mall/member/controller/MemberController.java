package com.xpcf.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.xpcf.common.exception.BizCodeEnum;
import com.xpcf.mall.member.exception.PhoneExistException;
import com.xpcf.mall.member.exception.UserNameExistException;
import com.xpcf.mall.member.feign.CouponFeignService;
import com.xpcf.mall.member.vo.MemberLoginVo;
import com.xpcf.mall.member.vo.MemberRegistVo;
import com.xpcf.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xpcf.mall.member.entity.MemberEntity;
import com.xpcf.mall.member.service.MemberService;
import com.xpcf.common.utils.PageUtils;
import com.xpcf.common.utils.R;



/**
 * 会员
 *
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:32:23
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private CouponFeignService couponFeignService;



    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser vo) throws Exception {


        MemberEntity memberEntity = memberService.login(vo);
        if (null != memberEntity) {

            // TODO process success
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }

    }

    @RequestMapping("/coupons")
    public R coupons(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R coupons = couponFeignService.coupons();
        return new R().put("member",memberEntity).put("coupons",coupons.get("coupons"));

    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (null != memberEntity) {
            // TODO process success

            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }

    }


    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo) {

        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
