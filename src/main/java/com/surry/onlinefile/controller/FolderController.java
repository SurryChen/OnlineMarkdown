package com.surry.onlinefile.controller;

import com.surry.onlinefile.common.ApiMsg;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.common.info.ControllerInfo;
import com.surry.onlinefile.common.info.FolderInfo;
import com.surry.onlinefile.entity.po.Folder;
import com.surry.onlinefile.entity.vo.FolderAndArticle;
import com.surry.onlinefile.service.FolderService;
import com.surry.onlinefile.utils.SingleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    FolderService folderService;

    /**
     * 新建文件夹的接口
     * 发送：
     * 当前文件夹id，如果是根目录发送用户id
     * 文件夹名字
     * 返回：
     * 新建的文件夹的id
     */
    @PostMapping("")
    public ApiMsg addOneFolder(Folder folder) {

        // 如果没有父文件，说明建立在userId上
        if(folder.getFatherFolderId() == null) {
            folder.setFatherFolderId(UserHolder.getCurrentUserId());
        }
        if(folder.getFatherFolderId() != null && folder.getFolderName() != null) {
            // 通过service层去新加，返回一个folderId
            Long folderId = folderService.addOneFolder(folder);
            // 找不到
            if(folderId == null) {
                return new ApiMsg(FolderInfo.NO_HAVE_THIS_FOLDER);
            } else {
                return new ApiMsg(FolderInfo.ADD_SUCCESS, SingleUtil.single("folderId", folderId));
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 删除文件夹的接口
     * 发送：
     * 要删除的文件夹的id
     * 返回：
     * 是否删除成功
     */
    @DeleteMapping("")
    public ApiMsg deleteFolder(@RequestParam("folderId") Long folderId) {

        if(folderId != null) {
            BaseInfo baseInfo = folderService.deleteOneFolder(folderId);
            if(baseInfo == null) {
                return new ApiMsg(ControllerInfo.UNKNOWN_ERROR);
            } else {
                return new ApiMsg(baseInfo);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 修改文件夹的名字
     * 发送：
     * 文件夹的id
     * 文件夹修改后的名字
     * 返回：
     * 修改是否成功
     */
    @PutMapping("")
    public ApiMsg modifyFolderName(Folder folder) {

        if(folder.getFolderId() != null && folder.getFolderName() != null) {
            BaseInfo baseInfo = folderService.modifyOneFolderName(folder);
            if(baseInfo == null) {
                return new ApiMsg(ControllerInfo.UNKNOWN_ERROR);
            } else {
                return new ApiMsg(baseInfo);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 查询某个文件夹下面的内容
     * 发送：
     * 文件夹的id
     * 返回：
     * 文件夹下面的所有文章和文件夹的id
     */
    @GetMapping("")
    public ApiMsg getOneFolderAll(@RequestParam("folderId") Long folderId) {

        if(folderId != null) {
            FolderAndArticle folderAndArticle = folderService.requireFolder(folderId);
            if(folderAndArticle == null) {
                return new ApiMsg(FolderInfo.NO_HAVE_THIS_FOLDER);
            } else {
                return new ApiMsg(FolderInfo.FIND_ALL_SUCCESS, folderAndArticle);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 查询文件夹
     * 发送：
     * 文件夹名字
     * 返回：
     * 文件夹名字带有搜索字的文件夹实体
     * 备注：
     * 模糊查询
     * 需要根据userId搜索
     */
    @GetMapping("/name")
    public ApiMsg getFolderName(@RequestParam("words") String name) {

        if(name != null && !"".equals(name)) {
            FolderAndArticle folderAndArticle = folderService.requireFolder(name);
            if(folderAndArticle == null) {
                return new ApiMsg(FolderInfo.NO_HAVE_THIS_FOLDER);
            } else {
                return new ApiMsg(FolderInfo.FIND_ALL_SUCCESS, folderAndArticle);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

    /**
     * 获取上一级的目录
     * 发送：
     * 此时的目录
     * 返回：
     * 上一级目录的内容
     */
    @GetMapping("/before")
    public ApiMsg getBefore(@RequestParam("folderId") Long folderId) {

        if(folderId != null) {
            FolderAndArticle folderAndArticle = folderService.folderBefore(folderId);
            if(folderAndArticle == null) {
                return new ApiMsg(FolderInfo.FIRST_FOLDER);
            } else {
                return new ApiMsg(FolderInfo.FIND_ALL_SUCCESS, folderAndArticle);
            }
        } else {
            return new ApiMsg(ControllerInfo.WRONG);
        }

    }

}
