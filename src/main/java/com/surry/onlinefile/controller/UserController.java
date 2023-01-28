package com.surry.onlinefile.controller;

import com.surry.onlinefile.common.ApiMsg;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.common.info.ControllerInfo;
import com.surry.onlinefile.entity.po.User;
import com.surry.onlinefile.entity.vo.UserVo;
import com.surry.onlinefile.service.UserService;
import com.surry.onlinefile.utils.MD5Util;
import com.surry.onlinefile.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    static final String netName = "106.52.74.37:8088/";

    /**
     * 注册接口
     * 发送：
     * userEmail 注册邮箱
     * userPassword 注册密码
     * userName 用户名字
     * 返回：
     * 是否成功
     */
    @PostMapping("/register")
    public ApiMsg register(User user) {

        if(user.getUserPassword() == null || user.getUserEmail() ==null || user.getUserName() == null) {
            return new ApiMsg(ControllerInfo.WRONG);
        }
        // 将password进行加密
        user.setUserPassword(MD5Util.getMD5(user.getUserPassword()));
        // 传入邮箱和密码进行注册
        BaseInfo baseInfo = userService.register(user);
        // 如果baseinfo为空说明userService发生了异常
        if(baseInfo == null) {
            return new ApiMsg(ControllerInfo.UNKNOWN_ERROR);
        }
        return new ApiMsg(baseInfo);

    }

    /**
     * 登录
     * 发送：
     * userEmail 登录邮箱
     * userPassword 登录密码
     * 返回：
     * 成功则返回token，不成功返回错误信息
     */
    @PostMapping("/login")
    public ApiMsg login(User user) {

        // 将密码设置为加密
        user.setUserPassword(MD5Util.getMD5(user.getUserPassword()));
        // 进行登录
        BaseInfo baseInfo = userService.login(user);
        // 判断是否出现问题
        if(baseInfo == null) {
            return new ApiMsg(ControllerInfo.UNKNOWN_ERROR);
        }
        // 没有出现问题，说明登录成功，想要找userId，去UserHolder里面找就行
        if(UserHolder.getCurrentUserId() != null) {
            // 说明登录成功
            // 生成token，存入到redis中，并返回给前端
            String token = TokenUtil.getToken(UserHolder.getCurrentUserId());
            return new ApiMsg(baseInfo, "{\"token\":\"" + token + "\"}");
        }
        // 没有登录成功
        return new ApiMsg(baseInfo);

    }

    /**
     * 修改用户名
     * 发送：
     * username 用户名
     * 返回：
     * 是否成功
     */
    @PutMapping("/modify/username")
    public ApiMsg updateUsername(@RequestParam("username") String username) {

        if(username != null && !"".equals(username)) {
            // 有值
            BaseInfo baseInfo = userService.modifyUsername(username);
            return new ApiMsg(baseInfo);
        }
        return new ApiMsg(ControllerInfo.WRONG);

    }

    /**
     * 修改密码
     * 发送：
     * password 密码
     * 返回：
     * 是否成功
     */
    @PutMapping("/modify/password")
    public ApiMsg updateUserPassword(@RequestParam("password") String password) {

        if(password != null && !"".equals(password)) {
            // 获取加密后的字符串
            password = MD5Util.getMD5(password);
            // 有值
            BaseInfo baseInfo = userService.modifyUsername(password);
            return new ApiMsg(baseInfo);
        }
        return new ApiMsg(ControllerInfo.WRONG);

    }

    /**
     * 使用token请求可以了
     */
    @GetMapping("/getUserInformation")
    public ApiMsg getInformation() {

        User userById = userService.findUserById(UserHolder.getCurrentUserId());
        UserVo userVo = new UserVo(userById);
        return new ApiMsg(ControllerInfo.SUCCESS, userVo);

    }

    /**
     * 上传头像
     * 发送：
     * portraitPath 一张图片的二进制数据
     * 返回：
     * 图片的URL
     */
    @PostMapping("/picture")
    public ApiMsg updateUserPortraitPath(@RequestParam("portraitPath") MultipartFile file) throws IOException {

        if (file.isEmpty()){
            return new ApiMsg(ControllerInfo.NO_CHOOSE_FILE);
        }
        // 获取当天日期
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd");
        String date = sdf.format(new Date());
        // 获取上传文件原来的名称
        String oldFileName = file.getOriginalFilename();
        // 获取上传文件的UUID名称
        String filename = UUID.randomUUID().toString().replace("-","") + "_"
                + oldFileName;
        // 获取上传文件的类型
        String type = filename.substring(filename.length() - 4);
        if (".jpg".equals(type) || ".png".equals(type) || ".JPG".equals(type) || ".PNG".equals(type)) {
        } else {
            // 说明格式不对
            return new ApiMsg(ControllerInfo.TYPE_ERROR);
        }
        // 文件放置如下：static/picture/日期/随机文件名字
        // 找到文件夹路径
//        String pictureFilePath = File.separator + "root" + File.separator + "Documents" + File.separator + "resources" + File.separator + "picture";
        String pictureFilePath = "C:\\Users\\Administrator\\Desktop\\onlinefile\\picture";
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
        // 修改头像URL
        userService.modifyPortraitPath(netName + "picture/" + date + "/" + filename);
        // 返回链接
        return new ApiMsg(ControllerInfo.SUCCESS, "{\"url\":\"" + netName + "picture/" + date + "/" + filename + "\"}");

    }

}
