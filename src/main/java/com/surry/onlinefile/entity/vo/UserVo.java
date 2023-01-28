package com.surry.onlinefile.entity.vo;

import com.surry.onlinefile.entity.po.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {

    private Long userId;
    private String userName;
    private String userEmail;
    private String portraitPath;

    public UserVo(User user) {
        this.userId = user.getUserId();
        this.userEmail = user.getUserEmail();
        this.userName = user.getUserName();
        this.portraitPath = user.getPortraitPath();
    }

}
