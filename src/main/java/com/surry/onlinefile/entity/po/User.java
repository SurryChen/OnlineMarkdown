package com.surry.onlinefile.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.surry.onlinefile.entity.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("onlineFile_user")
public class User {

    // MyBatis-plus会自动识别下面的小驼峰命名，对应数据库中的字段**_**
    // 比如说userId，会自动对应user_id
    @TableId(type = IdType.NONE)
    private Long userId;
    private String userName;
    private String userPassword;
    private String userEmail;
    private String portraitPath;

}
