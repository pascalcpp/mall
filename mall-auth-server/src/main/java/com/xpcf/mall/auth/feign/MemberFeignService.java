package com.xpcf.mall.auth.feign;

import com.xpcf.common.utils.R;
import com.xpcf.mall.auth.vo.SocialUser;
import com.xpcf.mall.auth.vo.UserLoginVo;
import com.xpcf.mall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/20/2021 5:12 AM
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser vo) throws Exception;

}
