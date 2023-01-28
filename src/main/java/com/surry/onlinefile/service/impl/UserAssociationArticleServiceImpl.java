package com.surry.onlinefile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.common.info.UserAssociationArticleInfo;
import com.surry.onlinefile.dao.UserAssociationArticleDao;
import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.po.User;
import com.surry.onlinefile.entity.po.UserAssociationArticle;
import com.surry.onlinefile.service.UserAssociationArticleService;
import com.surry.onlinefile.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Transactional(rollbackFor = Exception.class)
@Service
public class UserAssociationArticleServiceImpl extends ServiceImpl<UserAssociationArticleDao, UserAssociationArticle> implements UserAssociationArticleService {

    @Autowired
    UserAssociationArticleDao associationArticleDao;

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 添加某个用户某篇文章
     */
    @Override
    public BaseInfo addUserJoinArticle(UserAssociationArticle userAssociationArticle) {

        // 添加到数据库中
        associationArticleDao.insert(userAssociationArticle);
        // 将用户添加到其中
        redisTemplate.opsForSet().add(userAssociationArticle.getArticleId() + "_join_user", userAssociationArticle.getUserId());
        // 在那个用户那里加上这篇文章
        redisTemplate.opsForSet().add(userAssociationArticle.getUserId() + "_join_article", userAssociationArticle.getArticleId());
        // 返回成功
        return UserAssociationArticleInfo.ADD_SUCCESS;

    }

    /**
     * 删除某个用户参与某篇文章
     */
    @Override
    public BaseInfo deleteUserInArticle(Long articleId, Long userId) {

        // 删除数据库中的记录
        LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper();
        lam.eq(UserAssociationArticle::getArticleId, articleId);
        lam.eq(UserAssociationArticle::getUserId, userId);
        associationArticleDao.delete(lam);
        // 删除redis中的记录
        // 在文章那里删除userId
        redisTemplate.opsForSet().remove(articleId + "_join_user", userId);
        // 在用户那里删除文章id
        redisTemplate.opsForSet().remove(userId + "_join_article", articleId);
        return UserAssociationArticleInfo.DELETE_SUCCESS;

    }

    @Override
    public List<Article> findUserJoiner(Long userId) {

        return null;
    }

    /**
     * 查询某篇文章的参与者
     */
    @Override
    public List<User> findArticleJoiner(Long articleId) {

        // 去redis中查询
        Set members = redisTemplate.opsForSet().members(articleId + "_join_userId");
        // 如果没有
        if(members == null) {
            // 说明没有
            // 去查询数据库
            LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
            lam.eq(UserAssociationArticle::getArticleId, articleId);
            List<UserAssociationArticle> list = associationArticleDao.selectList(lam);
            // 顺便把id存到存入到redis中
            redisTemplate.opsForSet().add(articleId + "_join_user", list.toArray());
            // 根据Userid去找
            List<User> userList = new ArrayList<>();
            for(UserAssociationArticle userAssociationArticle: list) {
                User user = new User();
                user.setUserId(userAssociationArticle.getUserId());
                user = userService.findUserById(user.getUserId());
                userList.add(user);
            }
            return userList;
        } else {
            // 说明找到
            return new ArrayList<>(members);
        }
    }
}
