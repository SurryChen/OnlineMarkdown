package com.surry.onlinefile.common;

import com.surry.onlinefile.entity.po.User;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class UserHolder {

    private static ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    /**
     * 设置值
     */
    public static void setCurrentUserId(Long userId){
        currentUserId.set(userId);
    }

    /**
     * 获取值
     */
    public static Long getCurrentUserId(){
        return currentUserId.get();
    }
}