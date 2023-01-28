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
public enum FolderInfo implements BaseInfo {

    DELETE_FOLDER_SUCCESS(5001, "删除文件夹成功"),
    MODIFY_FOLDER_SUCCESS(5002, "修改文件夹名字成功"),
    FIRST_FOLDER(5003, "没有上一层目录了"),
    NO_HAVE_THIS_FOLDER(5004, "不存在该目录"),
    ADD_SUCCESS(5005, "添加文件夹成功"),
    FIND_ALL_SUCCESS(5006, "查找成功");

    // 5001开头
    int code;
    String message;

}
