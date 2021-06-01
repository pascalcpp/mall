package com.xpcf.mall.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xpcf.common.utils.HttpUtils;
import com.xpcf.common.utils.R;
import com.xpcf.common.vo.MemberRespVo;
import com.xpcf.mall.auth.feign.MemberFeignService;
import com.xpcf.mall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/22/2021 2:09 AM
 */
@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> querys = new HashMap<>();
        querys.put("client_id", "235824765");
        querys.put("client_secret", "0eec0ee7650ac4e5f38eb0a158f286f3");
        querys.put("grant_type", "authorization_code");
        querys.put("redirect_uri", "http://auth.mall.com/oauth2.0/weibo/success");
        querys.put("code", code);

        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), querys, (Map<String, String>) null);
        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSONObject.parseObject(json, new TypeReference<SocialUser>() {
            });
//           登陆或者注册这个用户
            log.info("response info: {}",socialUser);
            R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode().equals(0)) {
                MemberRespVo data = r.getData(new TypeReference<MemberRespVo>() {
                });
                log.info("登陆成功，用户信息: "+data);
                session.setAttribute("loginUser", data);
                return "redirect:http://mall.com";
            } else {
                return "redirect:http://auth.mall.com/login.html";
            }

        } else {
            return "redirect:http://auth.mall.com/login.html";
        }


    }
}
