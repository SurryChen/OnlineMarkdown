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
public enum ControllerInfo implements BaseInfo {

    // 从2001开始
    SUCCESS(2001,"操作成功"),
    WRONG(2002,"参数缺失，操作失败"),
    TYPE_ERROR(2003,"文件格式错误，请选择.jpg或者.png文件"),
    File_OVERLOAD(2004,"文件过大，支持10MB的文件"),
    LOGIN_STATE_ERROR(2005,"登录状态过期，请重新登录"),
    UNKNOWN_ERROR(2006, "未知错误，请联系管理员！"),
    NO_CHOOSE_FILE(2007, "未选择文件"),
    SERVER_WRONG(2008, "服务器出错，请联系管理员"),
    HTTP_WRONG(2009, "请求访问方式出错"),
    NO_PARA(2010, "没有请求参数");

    int code;
    String message;
}
