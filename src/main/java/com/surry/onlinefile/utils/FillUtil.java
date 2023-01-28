package com.surry.onlinefile.utils;

import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.entity.po.Article;

import java.time.LocalDateTime;

/**
 * 默认填充工具类
 */
public class FillUtil {

    /**
     * 插入的时候填充
     * 填充内容：
     * 创建时间
     * 最后修改时间
     * 创建者id
     * 内容
     */
    public static void fillInsertArticle(Article article) {
        article.setArticleCreateTime(LocalDateTime.now());
        article.setArticleContent("");
        article.setArticleCreateUserId(UserHolder.getCurrentUserId());
        article.setArticleLastModify(LocalDateTime.now());
    }

    /**
     * 修改的时候填充
     * 填充内容：
     * 最后修改时间
     */
    public static void fillUpdateArticle(Article article) {
        article.setArticleLastModify(LocalDateTime.now());
    }

}
