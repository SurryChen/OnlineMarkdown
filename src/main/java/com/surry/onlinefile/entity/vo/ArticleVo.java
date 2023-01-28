package com.surry.onlinefile.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.surry.onlinefile.entity.po.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleVo {

    private Long articleId;
    private String articleName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime articleCreateTime;
    private Long articleCreateUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime articleLastModify;
    private Long fatherFolderId;

    public ArticleVo(Article article) {
        this.articleId = article.getArticleId();
        this.articleName = article.getArticleName();
        this.articleCreateUserId = article.getArticleCreateUserId();
        this.articleLastModify = article.getArticleLastModify();
        this.fatherFolderId = article.getFatherFolderId();
        this.articleCreateTime = article.getArticleCreateTime();
    }

}
