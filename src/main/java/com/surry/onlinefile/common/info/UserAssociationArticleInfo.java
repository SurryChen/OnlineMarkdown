package com.surry.onlinefile.common.info;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum UserAssociationArticleInfo implements BaseInfo {

    // 4001开头
    ADD_SUCCESS(4001, "添加成功"),
    DELETE_SUCCESS(4002, "删除成功");

    int code;
    String message;

}
