package com.surry.onlinefile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.common.info.ArticleInfo;
import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.common.info.FolderInfo;
import com.surry.onlinefile.dao.ArticleDao;
import com.surry.onlinefile.dao.UserAssociationArticleDao;
import com.surry.onlinefile.dao.UserDao;
import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.po.Folder;
import com.surry.onlinefile.entity.po.User;
import com.surry.onlinefile.entity.po.UserAssociationArticle;
import com.surry.onlinefile.entity.vo.ArticleAllPeople;
import com.surry.onlinefile.entity.vo.UserVo;
import com.surry.onlinefile.service.ArticleService;
import com.surry.onlinefile.service.FolderService;
import com.surry.onlinefile.service.UserAssociationArticleService;
import com.surry.onlinefile.service.UserService;
import com.surry.onlinefile.utils.FillUtil;
import com.surry.onlinefile.utils.RedisUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional(rollbackFor = Exception.class)
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, Article> implements ArticleService {

    @Autowired
    ArticleDao articleDao;

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserAssociationArticleDao association;

    @Autowired
    FolderService folderService;

    @Override
    public Long addOneArticle(Article article) {

        // 首先判断父目录是否存在
        Folder folder = folderService.findFolderById(article.getFatherFolderId());
        User user = userService.findUserById(article.getFatherFolderId());
        if(folder == null && user == null) {
            return null;
        }
        // 传入的内容：articleName，父文件id
        // 先给填充信息
        FillUtil.fillInsertArticle(article);
        // 不判断是否重名
        // 直接插入
        articleDao.insert(article);
        // 返回articleId
        return article.getArticleId();

    }

    @Override
    public BaseInfo deleteOneArticle(Long articleId) {

        // 只有创建者才有的权限
        ArticleAllPeople userByArticleJoin = findUserByArticleJoin(articleId);
        if(userByArticleJoin == null) {
            return null;
        }
        if(!UserHolder.getCurrentUserId().equals(userByArticleJoin.getCreate().getUserId())) {
            return null;
        }
        // 删除一篇文章
        // 在数据库中删除实体
        articleDao.deleteById(articleId);
        // 在数据库中删除所有参与者关联
        LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
        lam.eq(UserAssociationArticle::getArticleId, articleId);
        association.delete(lam);
        // 去redis中删除
        deleteArticleFromRedis(articleId);
        // 删除目录标记
        folderService.deleteOneArticleRedis(articleId);
        return ArticleInfo.DELETE_SUCCESS;

    }

    @Override
    public BaseInfo modifyArticleTitle(Article article) {

        if(!havePower(article.getArticleId())) {
            return null;
        }
        // 修改一篇文章标题
        // 修改数据库中内容
        UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("article_id", article.getArticleId());
        updateWrapper.set("article_name", article.getArticleName());
        updateWrapper.set("article_last_modify", LocalDateTime.now());
        update(updateWrapper);
        // 去redis中删除实体
        redisTemplate.delete(article.getArticleId());
        return ArticleInfo.MODIFY_NAME_SUCCESS;
    }

    @Override
    public BaseInfo modifyArticleNovel(Article article) {

        if(!havePower(article.getArticleId())) {
            return null;
        }
        // 修改一篇文章内容
        // 修改数据库中内容
        UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("article_id", article.getArticleId());
        updateWrapper.set("article_content", article.getArticleContent());
        updateWrapper.set("article_last_modify", LocalDateTime.now());
        update(updateWrapper);
        // 去redis中删除实体
        redisTemplate.delete(article.getArticleId());
        return ArticleInfo.MODIFY_CONTENT_SUCCESS;

    }

    @Override
    public BaseInfo addPeopleToWrite(Long articleId, String email) {

        // 只有创建者才有的权限
        ArticleAllPeople userByArticleJoin = findUserByArticleJoin(articleId);
        if(!UserHolder.getCurrentUserId().equals(userByArticleJoin.getCreate().getUserId())) {
            return null;
        }
        if(findArticleById(articleId) == null) {
            return null;
        }
        // 找到这个用户
        User userByEmail = userService.findUserByEmail(email);
        // 如果没有这个用户，返回null
        if(userByEmail == null) {
            return null;
        }
        // 将userId写入到数据库中
        UserAssociationArticle as = new UserAssociationArticle();
        as.setArticleId(articleId);
        as.setUserId(userByEmail.getUserId());
        association.insert(as);
        // 添加后，为了保证一致性，要么删除，要么更新
        // 因为添加后，都会去查看，这里可能更新会比较好
        // 更新之前需要查看该键是否存在，不存在就直接查数据库，把数据库的信息放入
        // 更新userId_join_articleId
        // 更新articleId_join_userId
        updateTwoRedisKeyRedis(as);
        return ArticleInfo.ADD_SUCCESS;

    }

    @Override
    public BaseInfo deletePeopleToWrite(Long articleId, Long userId) {

        // 只有创建者才有的权限
        ArticleAllPeople userByArticleJoin = findUserByArticleJoin(articleId);
        if(!UserHolder.getCurrentUserId().equals(userByArticleJoin.getCreate().getUserId())) {
            return null;
        }
        if(findArticleById(articleId) == null) {
            return null;
        }
        if(userService.findUserById(userId) == null) {
            return null;
        }
        // 修改数据库
        LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
        lam.eq(UserAssociationArticle::getArticleId, articleId);
        lam.eq(UserAssociationArticle::getUserId, userId);
        association.delete(lam);
        // 更新之前需要查看该键是否存在，不存在就直接查数据库，把数据库的信息放入
        // 更新userId_join_articleId
        // 更新articleId_join_userId
        // 先查看是否存在，防止key已经失效了，原有的key失效了，这个时候再插入，后续就不会查询数据库了
        // 先查询文章的key
        String articleKey = articleId + "_join_userId";
        String userKey = userId + "_join_articleId";
        if(redisTemplate.opsForSet().members(articleKey) == null) {
            // 说明是空，应该查询数据库
            // 因为这一步更新操作必然是在数据库操作之后
            // 所以这里不需要更新redis的操作
            LambdaQueryWrapper<UserAssociationArticle> lamb = new LambdaQueryWrapper<>();
            lamb.eq(UserAssociationArticle::getArticleId, articleId);
            List<UserAssociationArticle> list = association.selectList(lamb);
            // 转化成只有userId的数据
            List<Long> listLong = new ArrayList<>();
            for(UserAssociationArticle as: list) {
                listLong.add(as.getArticleId());
            }
            // 保存
            redisTemplate.opsForSet().add(articleKey, listLong);
        } else {
            // 找到就更新
            redisTemplate.opsForSet().remove(articleKey, userId);
        }
        if(redisTemplate.opsForSet().members(userKey) == null) {
            // 说明是空，应该查询数据库
            // 因为这一步更新操作必然是在数据库操作之后
            // 所以这里不需要更新redis的操作
            LambdaQueryWrapper<UserAssociationArticle> lamb = new LambdaQueryWrapper<>();
            lamb.eq(UserAssociationArticle::getUserId, userId);
            List<UserAssociationArticle> list = association.selectList(lamb);
            // 转化成只有userId的数据
            List<Long> listLong = new ArrayList<>();
            for(UserAssociationArticle as: list) {
                listLong.add(as.getArticleId());
            }
            // 保存
            redisTemplate.opsForSet().add(userKey, listLong);
        } else {
            // 找到就更新
            redisTemplate.opsForSet().remove(userKey, articleId);
        }
        return ArticleInfo.DELETE_PEOPLE_SUCCESS;

    }

    @SneakyThrows
    @Override
    public Article findArticleById(Long articleId) {

        // 先看是不是组员
        // 非组员也可以观看
//        if(!havePower(articleId)) {
//            return null;
//        }
        // 先查询redis
        Article bean = (Article) RedisUtil.findBean(articleId, new Article());
        // 如果找到，返回
        if(bean.getArticleId() != null) {
            return bean;
        }
        // 如果没有找到，查询数据库
        Article article = articleDao.selectById(articleId);
        // 将结果写入到redis中
        if(article != null) {
            RedisUtil.saveBean(article.getArticleId(), article);
            return article;
        }
        // 将结果返回
        return null;

    }

    @Override
    public List<Article> findArticleByUserJoin() {

        String userKey = UserHolder.getCurrentUserId() + "_join_articleId";
        // 查询userId_join_articleId
        Set<Long> set = redisTemplate.opsForSet().members(userKey);
        if(set == null) {
            // 说明是空，应该查询数据库
            // 因为这一步更新操作必然是在数据库操作之后
            // 所以这里不需要更新redis的操作
            LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
            lam.eq(UserAssociationArticle::getUserId, UserHolder.getCurrentUserId());
            List<UserAssociationArticle> list = association.selectList(lam);
            // 转化成只有userId的数据
            List<Long> listLong = new ArrayList<>();
            for(UserAssociationArticle as: list) {
                listLong.add(as.getArticleId());
            }
            // 保存
            redisTemplate.opsForSet().add(userKey, listLong);
            set = new HashSet<>(listLong);
        }
        // 到这里set里面一定是有内容的，但是只有articleId，所以还需要根据articleId去找到文章内容
        List<Article> articleList = new ArrayList<>();
        for(Long articleId: set) {
            articleList.add(findArticleById(articleId));
        }
        return articleList;

    }

    @Override
    public ArticleAllPeople findUserByArticleJoin(Long articleId) {

        // 先看是不是组员
        if(!havePower(articleId)) {
            return null;
        }
        if(findArticleById(articleId) == null) {
            // 说明该篇文章不存在
            return null;
        }
        String articleKey = articleId + "_join_userId";
        // 查询articleId_join_userId
        Set<Long> set = redisTemplate.opsForSet().members(articleKey);
        if(set == null) {
            // 说明是空，应该查询数据库
            // 因为这一步更新操作必然是在数据库操作之后
            // 所以这里不需要更新redis的操作
            LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
            lam.eq(UserAssociationArticle::getArticleId, articleId);
            List<UserAssociationArticle> list = association.selectList(lam);
            // 转化成只有articleId的数据
            List<Long> listLong = new ArrayList<>();
            for(UserAssociationArticle as: list) {
                listLong.add(as.getUserId());
            }
            // 保存
            redisTemplate.opsForSet().add(articleKey, listLong);
            set = new HashSet<>(listLong);
        }
        // 到这里set里面一定是有内容的，但是只有userId，所以还需要根据userId去找到用户信息
        List<UserVo> userVoList = new ArrayList<>();
        for(Long userVoId: set) {
            userVoList.add(new UserVo(userService.findUserById(userVoId)));
        }
        ArticleAllPeople articleAllPeople = new ArticleAllPeople();
        articleAllPeople.setList(userVoList);
        // 还缺少一个创建者
        Article article = findArticleById(articleId);
        User user = userService.findUserById(article.getArticleCreateUserId());
        articleAllPeople.setCreate(new UserVo(user));
        return articleAllPeople;

    }

    // 将UserVo的list集合以set集合保存到redis中
    public void saveUserVoIdRedis(Long articleId, List<UserVo> userVoList) {

        // 只需要保存userId
        List<Long> list = new ArrayList<>();
        for(UserVo userVo: userVoList) {
            list.add(userVo.getUserId());
        }
        // 保存
        redisTemplate.opsForSet().add(articleId + "_join_userId", userVoList);

    }

    // 将Article的list集合以set集合保存redis中
    public void saveArticleIdRedis(Long userVoId, List<Article> articleList) {

        // 只需要保存articleId
        List<Long> list = new ArrayList<>();
        for(Article article: articleList) {
            list.add(article.getArticleId());
        }
        // 保存
        redisTemplate.opsForSet().add(userVoId + "_join_articleId", articleList);

    }

    // 添加参与者的时候，更新redis中的两个键
    public void updateTwoRedisKeyRedis(UserAssociationArticle userAssociationArticle) {

        // 先查看是否存在，防止key已经失效了，原有的key失效了，这个时候再插入，后续就不会查询数据库了
        // 先查询文章的key
        String articleKey = userAssociationArticle.getArticleId() + "_join_userId";
        String userKey = userAssociationArticle.getUserId() + "_join_articleId";
        if(redisTemplate.opsForSet().members(articleKey) == null) {
            // 说明是空，应该查询数据库
            // 因为这一步更新操作必然是在数据库操作之后
            // 所以这里不需要更新redis的操作
            LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
            lam.eq(UserAssociationArticle::getArticleId, userAssociationArticle.getArticleId());
            List<UserAssociationArticle> list = association.selectList(lam);
            // 转化成只有userId的数据
            List<Long> listLong = new ArrayList<>();
            for(UserAssociationArticle as: list) {
                listLong.add(as.getArticleId());
            }
            // 保存
            redisTemplate.opsForSet().add(articleKey, listLong);
        } else {
            // 找到就更新
            redisTemplate.opsForSet().add(articleKey, userAssociationArticle.getUserId());
        }
        if(redisTemplate.opsForSet().members(userKey) == null) {
            // 说明是空，应该查询数据库
            // 因为这一步更新操作必然是在数据库操作之后
            // 所以这里不需要更新redis的操作
            LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
            lam.eq(UserAssociationArticle::getUserId, userAssociationArticle.getUserId());
            List<UserAssociationArticle> list = association.selectList(lam);
            // 转化成只有userId的数据
            List<Long> listLong = new ArrayList<>();
            for(UserAssociationArticle as: list) {
                listLong.add(as.getArticleId());
            }
            // 保存
            redisTemplate.opsForSet().add(userKey, listLong);
        } else {
            // 找到就更新
            redisTemplate.opsForSet().add(userKey, userAssociationArticle.getArticleId());
        }


    }

    // 删除从redis中某篇文章
    public void deleteArticleFromRedis(Long articleId) {

        // 删除文章实体
        redisTemplate.delete(articleId);
        // 删除文章包括用户id的键
        // 先获取
        Set<Long> members = redisTemplate.opsForSet().members(articleId + "_join_userId");
        // 删除
        redisTemplate.delete(articleId + "_join_userId");
        // 删除用户id下的参与文章
        for(Long userId: members) {
            redisTemplate.opsForSet().remove(userId + "_join_article", articleId);
        }

    }

    /**
     * 做一个小的权限管理，对于需要articleId的操作，都需要查看是否有权限去使用
     */
    public boolean havePower(Long articleId) {

        // 查询该篇文章都有哪些用户
//        ArticleAllPeople userByArticleJoin = findUserByArticleJoin(articleId);

        // 先判断这篇文章是否存在
        Article articleByIdOther = findArticleByIdOther(articleId);
        if(articleByIdOther == null) {
            return false;
        }
        // 产生了循环调用
        ArticleAllPeople userByArticleJoin = findUserByArticleJoinOther(articleId);
        // 查看是不是创建者
        if(UserHolder.getCurrentUserId().equals(userByArticleJoin.getCreate().getUserId())) {
            return true;
        }
        // 看看参与者
        List<UserVo> list = userByArticleJoin.getList();
        for(UserVo userVo: list) {
            if(UserHolder.getCurrentUserId().equals(userVo.getUserId())) {
                return true;
            }
        }
        return false;

    }

    public ArticleAllPeople findUserByArticleJoinOther(Long articleId) {

        String articleKey = articleId + "_join_userId";
        // 查询articleId_join_userId
        Set<Long> set = redisTemplate.opsForSet().members(articleKey);
        if(set == null) {
            // 说明是空，应该查询数据库
            // 因为这一步更新操作必然是在数据库操作之后
            // 所以这里不需要更新redis的操作
            LambdaQueryWrapper<UserAssociationArticle> lam = new LambdaQueryWrapper<>();
            lam.eq(UserAssociationArticle::getArticleId, articleId);
            List<UserAssociationArticle> list = association.selectList(lam);
            // 转化成只有articleId的数据
            List<Long> listLong = new ArrayList<>();
            for(UserAssociationArticle as: list) {
                listLong.add(as.getUserId());
            }
//            // 保存
//            redisTemplate.opsForSet().add(articleKey, listLong);
            set = new HashSet<>(listLong);
        }
        // 到这里set里面一定是有内容的，但是只有userId，所以还需要根据userId去找到用户信息
        List<UserVo> userVoList = new ArrayList<>();
        for(Long userVoId: set) {
            userVoList.add(new UserVo(userService.findUserById(userVoId)));
        }
        ArticleAllPeople articleAllPeople = new ArticleAllPeople();
        articleAllPeople.setList(userVoList);
        // 还缺少一个创建者
        Article article = findArticleByIdOther(articleId);
        User user = userService.findUserById(article.getArticleCreateUserId());
        articleAllPeople.setCreate(new UserVo(user));
        return articleAllPeople;

    }

    @SneakyThrows
    public Article findArticleByIdOther(Long articleId) {

        // 先查询redis
        Article bean = (Article) RedisUtil.findBean(articleId, new Article());
        // 如果找到，返回
        if(bean.getArticleId() != null) {
            return bean;
        }
        // 如果没有找到，查询数据库
        Article article = articleDao.selectById(articleId);
        // 将结果写入到redis中
        if(article != null) {
            RedisUtil.saveBean(article.getArticleId(), article);
            return article;
        }
        // 将结果返回
        return null;

    }

}
