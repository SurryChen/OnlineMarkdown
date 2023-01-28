package com.surry.onlinefile.common.info;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回信息到客户端的枚举类型
 * 不加注释会加黄显示一块区域
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum UserInfo implements BaseInfo {

    // 从1001开始
    MAILBOX_NOTFOUND(1001,"该邮箱未注册"),
    PASSWORD_WRONG(1002,"密码错误"),
    CHECK_WRONG(1003,"验证码错误"),
    LOGIN_SUCCESS(1004,"登录成功"),
    REGISTER_SUCCESS(1005,"注册成功"),
    PASSWORD_MODIFY_SUCCESS(1006,"修改密码成功"),
    MAILBOX_FOUND(1007,"该邮箱已被注册"),
    USERNAME_MODIFY_SUCCESS(1008, "修改用户名成功"),
    SECOND_LOGIN_SUCCESS(1009, "本账号已经登录，本次登录已经将上一次登录下线，如非本人操作，请注意修改密码！"),
    PATH_MODIFY_SUCCESS(1010, "修改头像成功");

    int code;
    String message;

}
