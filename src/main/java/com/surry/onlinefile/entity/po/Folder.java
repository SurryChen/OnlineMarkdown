package com.surry.onlinefile.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件夹表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("onlineFile_folder")
public class Folder {

    @TableId(type = IdType.NONE)
    private Long folderId;
    private Long fatherFolderId;
    private Long userId;
    private String folderName;

}
