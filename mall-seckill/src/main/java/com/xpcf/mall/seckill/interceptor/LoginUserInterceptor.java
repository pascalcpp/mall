package com.xpcf.mall.seckill.interceptor;

import com.xpcf.common.constant.AuthServerConstant;
import com.xpcf.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/1/2021 11:57 PM
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {


    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        if (pathMatcher.match("/kill", requestURI)) {
            MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (null == attribute) {
                request.getSession().setAttribute("msg", "请先登录");
                response.sendRedirect("http://auth.mall.com/login.html");
                return false;
            } else {
                threadLocal.set(attribute);
                return true;
            }
        }
        return true;



    }
}
