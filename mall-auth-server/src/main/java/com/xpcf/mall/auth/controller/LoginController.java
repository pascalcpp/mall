package com.xpcf.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.netflix.ribbon.proxy.annotation.Http;
import com.xpcf.common.constant.AuthServerConstant;
import com.xpcf.common.exception.BizCodeEnum;
import com.xpcf.common.utils.R;
import com.xpcf.common.vo.MemberRespVo;
import com.xpcf.mall.auth.feign.MemberFeignService;
import com.xpcf.mall.auth.feign.ThirdPartFeignService;
import com.xpcf.mall.auth.vo.UserLoginVo;
import com.xpcf.mall.auth.vo.UserRegistVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/18/2021 5:16 AM
 */
@Controller
@Slf4j
public class LoginController {
//
//    @GetMapping("/login.html")
//    public String loginPage() {
//        return "login";
//    }
//
//    @GetMapping("/reg.html")
//    public String regPage() {
//        return "reg";
//    }

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {

        //TODO 接口防刷

        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);

        if (null != redisCode) {
            Long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }



        String code = UUID.randomUUID().toString().substring(0,5)+"_"+System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,10L, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code.substring(0,5));

        return R.ok();

    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {

        if (null != session.getAttribute(AuthServerConstant.LOGIN_USER)) {
            return "redirect:http://mall.com";
        } else {
            return "login";
        }

    }


    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (u,v) -> v));
            redirectAttributes.addFlashAttribute("errors",errors);
            // forward:/reg.html 需要get 这里是post 出现 post not support
            // redirect 取数据 利用 session
            return "redirect:http://auth.mall.com/reg.html";
        }

        String code = vo.getCode();
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

        if (!StringUtils.isEmpty(redisCode)) {

            if (code.equalsIgnoreCase(redisCode.split("_")[0])) {
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                R r = memberFeignService.regist(vo);

                if (r.getCode().equals(0)) {

                    return "redirect:http://auth.mall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/reg.html";
                }

            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.mall.com/reg.html";
            }

        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code","未发送验证码");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.mall.com/reg.html";
        }


    }


    @PostMapping("/login")
    public String login(UserLoginVo vo,
                        RedirectAttributes redirectAttributes,
                        HttpSession session) {

        // 远程登录
        R r = memberFeignService.login(vo);
        if (r.getCode().equals(0)) {
            MemberRespVo data = r.getData(new TypeReference<MemberRespVo>() {
            });
            log.info("login success vo info: {}", data);
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://mall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/login.html";
        }


    }


}
