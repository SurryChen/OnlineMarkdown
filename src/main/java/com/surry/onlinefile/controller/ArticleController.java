package com.surry.onlinefile.controller;

import com.surry.onlinefile.common.ApiMsg;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.common.info.ArticleInfo;
import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.common.info.ControllerInfo;
import com.surry.onlinefile.common.info.FolderInfo;
import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.vo.ArticleAllPeople;
import com.surry.onlinefile.entity.vo.ArticleVo;
import com.surry.onlinefile.service.ArticleService;
import com.surry.onlinefile.utils.SingleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    ArticleService articleService;

    static final String netName = "106.52.74.37:8088/";
//    static final String netName = "localhost:8088/";

    /**
     * 创建一篇文章
     * 发送：
     * 创建的文章的名字
     * 创建的目录id
     * 返回：
     * 创建后的文章id
     */
    @PostMapping
    public ApiMsg createArticle(Article article) {

        if (article.getArticleName() != null && article.getFatherFolderId() != null) {
            // 说明不为空
            Long aLong = articleService.addOneArticle(article);
            if (aLong == null) {
                // 说明不存在这个父文件夹
                return new ApiMsg(FolderInfo.NO_HAVE_THIS_FOLDER);
            } else {
                // 存在
                return new ApiMsg(ArticleInfo.ADD_SUCCESS, SingleUtil.single("articleId", aLong));
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 删除一篇文章
     * 发送：
     * 想要删除的文章的id
     * 返回：
     * 是否删除成功
     */
    @DeleteMapping
    public ApiMsg deleteArticle(Article article) {

        Long articleId = article.getArticleId();
        if (articleId != null) {
            // 说明不为空
            BaseInfo baseInfo = articleService.deleteOneArticle(articleId);
            if (baseInfo == null) {
                // 出现位置错误
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                // 存在
                return new ApiMsg(baseInfo);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 修改文章标题
     * 发送：
     * 想要修改的文章id
     * 想要修改的标题
     * 返回：
     * 是否修改成功
     */
    @PutMapping("/modifyName")
    public ApiMsg modifyName(Article article) {

        if (article.getArticleId() != null && article.getArticleName() != null) {
            // 说明不为空
            BaseInfo baseInfo = articleService.modifyArticleTitle(article);
            if (baseInfo == null) {
                // 出现位置错误
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                // 存在
                return new ApiMsg(baseInfo);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 修改文章内容
     * 发送：
     * 想要修改的文章id
     * 想要修改的内容
     * 返回：
     * 是否修改成功
     */
    @PutMapping("/modifyContent")
    public ApiMsg modifyContent(Article article) {

        if (article.getArticleId() != null && article.getArticleContent() != null) {
            // 说明不为空
            BaseInfo baseInfo = articleService.modifyArticleNovel(article);
            if (baseInfo == null) {
                // 出现位置错误
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                // 存在
                return new ApiMsg(baseInfo);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 上传文件
     * 发送：
     * 想要上传的文件
     * 返回：
     * 上传结果
     */
    @PostMapping("/file")
    public ApiMsg updateUserPortraitPath(@RequestParam("file") MultipartFile file) throws IOException {


        if (file.isEmpty()) {
            return new ApiMsg(ControllerInfo.NO_CHOOSE_FILE);
        }
        // 获取当天日期
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd");
        String date = sdf.format(new Date());
        // 获取上传文件原来的名称
        String oldFileName = file.getOriginalFilename();
        // 获取上传文件的UUID名称
        String filename = UUID.randomUUID().toString().replace("-", "") + "_"
                + oldFileName;
        // 获取上传文件的类型
        String type = filename.substring(filename.length() - 4);
        if (".jpg".equals(type) || ".png".equals(type) || ".JPG".equals(type) || ".PNG".equals(type) || ".mp4".equals(type) || ".MP4".equals(type)) {
        } else {
            // 说明格式不对
            return new ApiMsg(ControllerInfo.TYPE_ERROR);
        }
        // 写死
//        String pictureFilePath = File.separator + "root" + File.separator + "Documents" + File.separator + "resources" + File.separator + "article";
        String pictureFilePath = "C:\\Users\\Administrator\\Desktop\\onlinefile\\article";
        // 判断日期文件夹是否有创建
        File dateFile = new File(pictureFilePath + File.separator + date);
        // 不存在就创建
        if (!dateFile.exists()){
            dateFile.mkdirs();
        }
        // 存在
        File localFile = new File(dateFile + File.separator + filename);
        // 下载
        file.transferTo(localFile);
        // 返回链接
        return new ApiMsg(ControllerInfo.SUCCESS, "{\"url\":\"" + netName + "article/" + date + "/" + filename + "\"}");

    }

    /**
     * 获取一篇文章的参与者
     * 发送：
     * 文章id
     * 返回：
     * 获取结果
     */
    @GetMapping("/requireAllWriter")
    public ApiMsg requireAllWriter(@RequestParam("articleId") Long articleId) {

        if (articleId != null) {
            // 说明不为空
            ArticleAllPeople userByArticleJoin = articleService.findUserByArticleJoin(articleId);
            if (userByArticleJoin == null) {
                // 这篇文章不存在
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                // 存在
                return new ApiMsg(ArticleInfo.FIND_SUCCESS, userByArticleJoin);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 添加某个用户为参与编写者
     * 发送：
     * 文章id
     * 参与者邮箱，最多添加五个人
     * 返回：
     * 添加结果
     */
    @PostMapping("/addWriter")
    public ApiMsg addWriter(@RequestParam("userEmail") String userEmail, @RequestParam("articleId") Long articleId) {

        if (userEmail != null && !"".equals(userEmail)) {
            // 说明不为空
            BaseInfo baseInfo = articleService.addPeopleToWrite(articleId, userEmail);
            if (baseInfo == null) {
                // 这篇文章不存在或者该用户不存在
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                // 存在
                return new ApiMsg(baseInfo);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 删除某个用户为参与者
     * 发送：
     * 某个用户的id
     * 返回：
     * 删除结果
     */
    @PostMapping("/deleteWriter")
    public ApiMsg deleteWriter(@RequestParam("userId") Long userId, @RequestParam("articleId") Long articleId) {

        if (userId != null && articleId != null) {
            // 说明不为空
            BaseInfo baseInfo = articleService.deletePeopleToWrite(articleId, userId);
            if (baseInfo == null) {
                // 这篇文章不存在或者该用户不存在
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                // 存在
                return new ApiMsg(baseInfo);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 查找某个用户参与的所有文章
     */
    @GetMapping("/requireAllJoinArticle")
    public ApiMsg requireAllJoinArticle() {

        if (UserHolder.getCurrentUserId() != null) {
            // 说明不为空
            List<Article> articleByUserJoin = articleService.findArticleByUserJoin();
            if (articleByUserJoin == null) {
                // 用户没有参与文章
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                // 存在
                List<ArticleVo> articleVoList = new ArrayList<>();
                // 这还不需要发送内容
                for (Article article : articleByUserJoin) {
                    articleVoList.add(new ArticleVo(article));
                }
                return new ApiMsg(ArticleInfo.FIND_SUCCESS, articleVoList);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 一篇文章的详细内容
     * 发送：
     * articleId
     * 返回：
     * article实体
     */
    @GetMapping("/requireOneArticle")
    public ApiMsg requireOneArticle(@RequestParam("articleId") Long articleId) {

        if (articleId != null) {
            // 说明不为空
            Article article = articleService.findArticleById(articleId);
            if (article == null) {
                // 用户没有参与文章
                return new ApiMsg(ArticleInfo.NO_HAVE_OR_NO_POWER);
            } else {
                return new ApiMsg(ArticleInfo.FIND_SUCCESS, article);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

}
