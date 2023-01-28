package com.surry.onlinefile.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.surry.onlinefile.common.ApiInfo;
import com.surry.onlinefile.common.ApiMsg;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.common.info.ControllerInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 需要登录才能访问的接口的过滤器
 */
@Component
public class LoginIntercepter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取用户，看看是否为空
        if(UserHolder.getCurrentUserId() == null) {
            // 为空，不给通过
            // 返回错误信息
            response.getWriter().write(JSONObject.toJSONString(new ApiMsg<>(ControllerInfo.LOGIN_STATE_ERROR)));
            return false;
        } else {
            // 通过
            return true;
        }

    }
}
