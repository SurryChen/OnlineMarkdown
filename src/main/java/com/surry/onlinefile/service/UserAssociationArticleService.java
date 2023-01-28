package com.surry.onlinefile.service;

import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.po.User;
import com.surry.onlinefile.entity.po.UserAssociationArticle;

import java.util.List;

public interface UserAssociationArticleService {

    /**
     * 添加某个用户某篇文章
     */
    public BaseInfo addUserJoinArticle(UserAssociationArticle userAssociationArticle);

    /**
     * 删除某个用户参与某篇文章
     */
    public BaseInfo deleteUserInArticle(Long articleId, Long userId);

    /**
     * 查询某个用户参与的文章
     */
    public List<Article> findUserJoiner(Long userId);

    /**
     * 查询某篇文章的参与者
     */
    public List<User> findArticleJoiner(Long articleId);

}

