package com.surry.onlinefile.service;

import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.entity.po.User;

public interface UserService {

    /**
     * 用户创建
     */
    public BaseInfo register(User user);

    /**
     * 用户登录
     */
    public BaseInfo login(User user);

    /**
     * 修改账号名字
     */
    public BaseInfo modifyUsername(String username);

    /**
     * 修改密码
     */
    public BaseInfo modifyPassword(String password);

    /**
     * 修改头像
     */
    public BaseInfo modifyPortraitPath(String portraitPath);

    /**
     * 根据id查询某个用户
     */
    public User findUserById(Long userId);

    /**
     * 根据邮箱查询用户
     */
    public User findUserByEmail(String email);

}
