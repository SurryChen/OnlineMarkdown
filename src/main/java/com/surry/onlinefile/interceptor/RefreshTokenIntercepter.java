package com.surry.onlinefile.interceptor;

import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.surry.onlinefile.common.info.TimeInfo;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.entity.po.User;
import com.surry.onlinefile.utils.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 刷新token的拦截器
 */
@Component
public class RefreshTokenIntercepter implements HandlerInterceptor {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("----------经过");
        // 获取请求头中的token
        String token = request.getHeader("authorization");
        // 如果没有token，直接通过
        if("".equals(token) || token == null) {
            return true;
        }
        // 如果有token，获取userId，并保存在线程变量中
        Claims claims = TokenUtil.checkToken(token);
        UserHolder.setCurrentUserId((Long) claims.get("userId"));
        // 然后刷新token的保质期
        redisTemplate.expire(token, TimeInfo.LOGIN_TIME_TTL.getLength(), TimeInfo.LOGIN_TIME_TTL.getTimeUnit());
        // 然后通过
        return true;

    }

}
