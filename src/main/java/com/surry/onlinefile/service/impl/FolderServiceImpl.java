package com.surry.onlinefile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.common.info.FolderInfo;
import com.surry.onlinefile.dao.ArticleDao;
import com.surry.onlinefile.dao.FolderDao;
import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.po.Folder;
import com.surry.onlinefile.entity.vo.FolderAndArticle;
import com.surry.onlinefile.service.ArticleService;
import com.surry.onlinefile.service.FolderService;
import com.surry.onlinefile.utils.RedisUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class FolderServiceImpl extends ServiceImpl<FolderDao, Folder> implements FolderService {

    @Autowired
    FolderDao folderDao;
    @Autowired
    ArticleDao articleDao;

    @Autowired
    ArticleService articleService;

    @Autowired
    RedisTemplate redisTemplate;

    @SneakyThrows
    @Override
    public Long addOneFolder(Folder folder) {

        folder.setUserId(UserHolder.getCurrentUserId());
        // 需要看看父类文件夹是否存在，注意父类id可能是用户id
        if (UserHolder.getCurrentUserId().equals(folder.getFatherFolderId())) {
            folderDao.insert(folder);
            saveOneFolderDownRedis(folder);
            return folder.getFolderId();
        }
        // 如果不看的话会导致在文件id创建文件夹的情况
        Folder bean = (Folder) RedisUtil.findBean(folder, new Folder());
        if (bean.getFolderId() == null) {
            // 说明不在redis中
            // 查询数据库
            Folder folderSelect = folderDao.selectById(folder.getFolderId());
            if (folderSelect == null) {
                // 说明不存在
                return null;
            }
        }
        // 说明存在
        // 保存在数据库就可以了
        folderDao.insert(folder);
        // 也需要加入在redis中
        saveOneFolderDownRedis(folder);
        return folder.getFolderId();

    }

    @Override
    public BaseInfo deleteOneFolder(Long folderId) {

        // 先删除数据库中的
        folderDao.deleteById(folderId);
        // 再删除redis中的
        deleteOneFolderRedis(folderId);
        // 不删除文件，把文件放置在根目录下
        // 修改数据中父文件为folder的个体的父文件为userId
        // 修改完后，redis中还需要删除
        // redis中不修改等待过期删除也行
        // 找出所有的内容
        LambdaQueryWrapper<Folder> lam = new LambdaQueryWrapper();
        lam.eq(Folder::getFatherFolderId, folderId);
        List<Folder> folders = folderDao.selectList(lam);
        for (Folder folder : folders) {
            // 使用Redis删除，不能直接下面的方法，因为下面的方法是所有与该条有关的
            redisTemplate.delete(folder.getFolderId());
        }
        // 使用SQL语句一次性改变fatherFolderId
        UpdateWrapper<Folder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("father_folder_id", UserHolder.getCurrentUserId());
        updateWrapper.eq("father_folder_id", folderId);
        update(updateWrapper);
        return FolderInfo.DELETE_FOLDER_SUCCESS;

    }

    @Override
    public BaseInfo modifyOneFolderName(Folder folder) {

        // 修改数据库中的
        UpdateWrapper<Folder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("folder_name", folder.getFolderName());
        updateWrapper.eq("folder_id", folder.getFolderId());
        update(updateWrapper);
        // 然后删除redis中的实体
        redisTemplate.delete(folder.getFolderId());
        return FolderInfo.MODIFY_FOLDER_SUCCESS;

    }

    @Override
    public FolderAndArticle requireFolder(Long folderId) {

        // 先去Redis中查找
        FolderAndArticle allFolderAndArticleRedis = findAllFolderAndArticleRedis(folderId);
        // 如果都是为null
        // 第一种可能，确实是没有东西
        // 第二种可能，有东西但是还没更新到Redis中
        // 所以增加数据库中操作
        if (allFolderAndArticleRedis.getFolderList() == null || allFolderAndArticleRedis.getFolderList().size() == 0) {
            LambdaQueryWrapper<Folder> lam = new LambdaQueryWrapper<>();
            lam.eq(Folder::getFatherFolderId, folderId);
            List<Folder> folderList = folderDao.selectList(lam);
            allFolderAndArticleRedis.setFolderList(folderList);
            // 保存在redis中
            saveManyFolderDownRedis(null, folderList);
        }
        if (allFolderAndArticleRedis.getArticleList() == null || allFolderAndArticleRedis.getArticleList().size() == 0) {
            LambdaQueryWrapper<Article> lam = new LambdaQueryWrapper<>();
            lam.eq(Article::getFatherFolderId, folderId);

            List<Article> articleList = articleDao.selectList(lam);
            allFolderAndArticleRedis.setArticleList(articleList);
            // 保存在redis中
            saveManyFolderDownRedis(articleList, null);
        }
        return allFolderAndArticleRedis;

    }

    @Override
    public FolderAndArticle requireFolder(String name) {

        // 不适合在redis中查找
        LambdaQueryWrapper<Folder> lam = new LambdaQueryWrapper<>();
        lam.like(Folder::getFolderName, name);
        List<Folder> folders = folderDao.selectList(lam);
        // 查找文章
        LambdaQueryWrapper<Article> lamA = new LambdaQueryWrapper<>();
        lamA.like(Article::getArticleName, name);
        List<Article> articleList = articleDao.selectList(lamA);
        return new FolderAndArticle(folders, articleList);

    }

    // 在Redis中保存一系列文章，重复操作保存一篇也行
    @Override
    public void saveManyFolderDownRedis(List<Article> articleSet, List<Folder> folderSet) {

        // 循环
        if (articleSet != null && articleSet.size() != 0) {
            // 调用别的方法的实体的
            for(Article article: articleSet) {
                saveOneArticleDownRedis(article);
            }
        }

        if (folderSet != null && folderSet.size() != 0) {
            for (Folder folder : folderSet) {
                saveOneFolderDownRedis(folder);
            }
        }

    }

    // 在Redis中保存一篇文件夹
    @SneakyThrows
    @Override
    public void saveOneFolderDownRedis(Folder folder) {

        // 保存文件id到父文件夹下
        redisTemplate.opsForSet().add(folder.getFatherFolderId() + "_sonFolder", folder.getFolderId());
        // 保存文章实体
        RedisUtil.saveBean(folder.getFolderId(), folder);

    }

    // 在Redis中保存一篇文章
    @SneakyThrows
    public void saveOneArticleDownRedis(Article article) {

        // 保存文件id到父文件夹下
        redisTemplate.opsForSet().add(article.getFatherFolderId() + "_sonArticle", article.getArticleId());
        // 保存文章实体
        RedisUtil.saveBean(article.getArticleId(), article);

    }

    @Override
    public void deleteOneFolderRedis(Long folderId) {

        // 删除父文件夹中的子文件集合
        // 键名为分文件夹id+_+son
        redisTemplate.opsForSet().remove(folderId + "_sonFolder", folderId);
        // 删除文件夹实体
        redisTemplate.delete(folderId);

    }

    @Override
    public void deleteOneArticleRedis(Long articleId) {

        // 删除父文件夹中的子文件集合
        // 键名为分文件夹id+_+son
        redisTemplate.opsForSet().remove(articleId + "_sonArticle", articleId);
        // 删除文件夹实体
        redisTemplate.delete(articleId);

    }

    @SneakyThrows
    @Override
    public FolderAndArticle findAllFolderAndArticleRedis(Long folderId) {

        // 找到以该文件夹为id的父文件夹下的文章id或文件夹id
        Set<Long> folderSet = redisTemplate.opsForSet().members(folderId + "_sonFolder");
        List<Folder> folderList = new ArrayList<>();
        // 循环通过id查找
        for (Long folderOneId : folderSet) {
            // 先去redis中找
            Folder folder = (Folder) RedisUtil.findBean(folderOneId, new Folder());
            // 如果找不到
            if (folder.getFolderId() == null) {
                // 去数据库中找
                Folder folderMidden = folderDao.selectById(folderOneId);
                folderList.add(folderMidden);
            } else {
                folderList.add(folder);
            }
        }
        Set<Long> articleSet = redisTemplate.opsForSet().members(folderId + "_sonArticle");
        List<Article> articleList = new ArrayList<>();
        // 循环通过id查找
        for (Long articleOneId : articleSet) {
            // 先去redis中找
            Article article = (Article) RedisUtil.findBean(articleOneId, new Article());
            // 如果找不到
            if (article.getArticleId() == null) {
                // 去数据库中找
                Article articleMidden = articleDao.selectById(articleOneId);
                articleList.add(articleMidden);
            } else {
                articleList.add(article);
            }
        }
        return new FolderAndArticle(folderList, articleList);

    }


    @Override
    @SneakyThrows
    public FolderAndArticle folderBefore(Long folderId) {

        // 如果等于userId，说明没有上一级目录，属于错误发送
        if (UserHolder.getCurrentUserId().equals(folderId)) {
            return null;
        }
        // 先根据id找到一个文件夹内容
        Folder folder = (Folder) RedisUtil.findBean(folderId, new Folder());
        // 如果里面没啥东西，说明没找着
        if (folder.getFatherFolderId() == null) {
            // 查询数据库
            folder = folderDao.selectById(folderId);
            // 如果找不到
            if (folder == null) {
                return null;
            } else {
                // 找到了，获取上一级目录
                // 上一级目录
                Long fatherFolderId = folder.getFatherFolderId();
                return requireFolder(fatherFolderId);
            }
        } else {
            // 找到了
            return requireFolder(folder.getFatherFolderId());
        }
    }

    @SneakyThrows
    @Override
    public Folder findFolderById(Long folderId) {

        // 先去redis中查找
        Folder bean = (Folder) RedisUtil.findBean(folderId, new Folder());
        if(bean.getFolderId() != null) {
            // 说明找到了
            return bean;
        }
        // 说明没有找到，去数据库那里找
        Folder folder = folderDao.selectById(folderId);
        if(folder != null) {
            return folder;
        }
        return null;
    }

}
