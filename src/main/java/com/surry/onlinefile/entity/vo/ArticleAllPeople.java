package com.surry.onlinefile.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
// 一篇文章的所有用户，包括了创建者和参与者
public class ArticleAllPeople {

    // 创造者
    private UserVo create;
    // 参与者
    private List<UserVo> list;

}
