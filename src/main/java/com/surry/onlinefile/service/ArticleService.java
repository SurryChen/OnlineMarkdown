package com.surry.onlinefile.service;

import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.po.User;
import com.surry.onlinefile.entity.vo.ArticleAllPeople;
import com.surry.onlinefile.entity.vo.UserVo;

import java.util.List;

public interface ArticleService {

    /**
     * 创建一篇文章
     * 发送：
     * 创建的文章的名字
     * 创建的目录id
     * 返回：
     * 创建后的文章id
     */
    public Long addOneArticle(Article article);

    /**
     * 删除一篇文章
     * 发送：
     * 想要删除的文章的id
     * 返回：
     * 是否删除成功
     */
    public BaseInfo deleteOneArticle(Long articleId);

    /**
     * 修改文章标题
     * 发送：
     * 想要修改的文章id
     * 想要修改的标题
     * 返回：
     * 是否修改成功
     */
    public BaseInfo modifyArticleTitle(Article article);

    /**
     * 修改文章内容
     * 发送：
     * 想要修改的文章id
     * 想要修改的内容
     * 返回：
     * 是否修改成功
     */
    public BaseInfo modifyArticleNovel(Article article);

    /**
     * 上传文件
     * 发送：
     * 想要上传的文件
     * 返回：
     * 上传结果
     */

//    /**
//     * 获取一篇文章的参与者
//     * 发送：
//     * 文章id
//     * 返回：
//     * 获取结果
//     */
//    public List<User> requireArticleAllPeople(Long articleId);
//
//    public List<Article> requireUserAllArticle();

    /**
     * 添加某个用户为参与编写者
     * 发送：
     * 参与者邮箱，最多添加五个人
     * 返回：
     * 添加结果
     */
    public BaseInfo addPeopleToWrite(Long articleId, String email);

    /**
     * 删除某个用户为参与者
     * 发送：
     * 某个用户的id
     * 返回：
     * 删除结果
     */
    public BaseInfo deletePeopleToWrite(Long article, Long userId);

    /**
     * 根据id查询文章
     * 发送：
     *
     */
    public Article findArticleById(Long articleId);

    /**
     * 查询用户参与的文章(非创建)
     */
    public List<Article> findArticleByUserJoin();

    /**
     * 查询参与文章的用户
     * @return
     */
    public ArticleAllPeople findUserByArticleJoin(Long articleId);

}
