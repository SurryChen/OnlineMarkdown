package com.surry.onlinefile.entity.vo;

import com.surry.onlinefile.entity.po.Article;
import com.surry.onlinefile.entity.po.Folder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 某一目录下的返回内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderAndArticle {

    private List<Folder> folderList = new ArrayList<>();
    private List<Article> articleList = new ArrayList<>();

}
