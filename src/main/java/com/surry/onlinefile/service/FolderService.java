package com.surry.onlinefile.service;

import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.po.Folder;
import com.surry.onlinefile.entity.vo.FolderAndArticle;

import java.util.List;

public interface FolderService {

    /**
     * 新建文件夹的接口
     * 发送：
     * 当前文件夹id
     * 文件夹名字
     * 返回：
     * 新建的文件夹的id
     * 异常：
     * 同一个用户有同名文件夹，可以存在吗，由于id不一样，所以可以存在
     */
    public Long addOneFolder(Folder folder);

    /**
     * 删除文件夹的接口
     * 发送：
     * 要删除的文件夹的id
     * 返回：
     * 是否删除成功
     */
    public BaseInfo deleteOneFolder(Long folderId);

    /**
     * 修改文件夹的名字
     * 发送：
     * 文件夹的id
     * 文件夹修改后的名字
     * 返回：
     * 修改是否成功
     */
    public BaseInfo modifyOneFolderName(Folder folder);

    /**
     * 查询某个文件夹下面的内容
     * 发送：
     * 文件夹的id
     * 返回：
     * 文件夹下面的所有文章和文件夹的id
     */
    public FolderAndArticle requireFolder(Long folderId);

    /**
     * 查询文件夹
     * 发送：
     * 文件夹名字
     * 返回：
     * 文件夹名字带有搜索字的文件夹实体或者文章
     * 备注：
     * 模糊查询
     * 需要根据userId搜索
     */
    public FolderAndArticle requireFolder(String name);

    /**
     * 把数据库中查询出来的文件夹和文章，按照父文件夹的id，保存在父文件夹的id中
     * 同时将文件夹或者文章内容保存在reids中
     * 参数：
     * 所有需要保存的article集合和folder集合
     * 返回：
     * 保存成功
     * 备注：
     * 因为内容不可重复，所以使用set集合
     */
    public void saveManyFolderDownRedis(List<Article> articleSet, List<Folder> folderSet);

    /**
     * 保存一个文件夹的内容
     */
    public void saveOneFolderDownRedis(Folder folder);

    /**
     * 在redis中删除一个文件夹
     * 发送：
     * 文件夹id
     * 操作：
     * 删除这个文件夹
     * 删除父文件夹中集合的文件夹id
     * 返回：
     * 是否删除
     */
    public void deleteOneFolderRedis(Long folderId);

    /**
     * 查询某个文件夹下的所有文件夹内容或者文章
     * 发送：
     * 文件夹id
     * 操作：
     */
    public FolderAndArticle findAllFolderAndArticleRedis(Long folderId);

    /**
     * 查找上一级目录的内容
     * 发送：
     * 当前目录文件夹id
     * 返回上一级目录的所有内容
     */
    public FolderAndArticle folderBefore(Long folderId);

    /**
     * 根据文件夹id去查询某个文件夹的名字等内容，可以用来判断某个文件夹是否存在
     */
    public Folder findFolderById(Long folderId);

    public void deleteOneArticleRedis(Long articleId);

}
