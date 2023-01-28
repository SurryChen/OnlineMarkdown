package com.surry.onlinefile.common.info;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ArticleInfo implements BaseInfo {

    CREATE_ARTICLE_SUCCESS(3001, "新建文章成功"),
    DELETE_SUCCESS(3002, "删除文章成功"),
    MODIFY_NAME_SUCCESS(3003, "修改文章名字成功"),
    ADD_SUCCESS(3004, "添加成功"),
    DELETE_PEOPLE_SUCCESS(3005, "删除参与者成功"),
    NO_HAVE_NOVEL(3006, "该篇文章不存在"),
    FIND_SUCCESS(3007, "查找成功"),
    NO_JOIN_ARTICLE(3008, "用户没有参与的文章"),
    ARTICLE_OR_USER_NO_HAVE(3009, "该篇文章不存在或者用户不存在"),
    NO_HAVE_OR_NO_POWER(3010, "该篇文章不存在或者用户没有权限查看"),
    MODIFY_CONTENT_SUCCESS(3011, "修改文章内容成功");

    int code;
    String message;

}
