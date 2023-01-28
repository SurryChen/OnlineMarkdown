package com.surry.onlinefile.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("onlineFile_user_association_article")
public class UserAssociationArticle {

    private Long userId;
    private Long articleId;
    
}
