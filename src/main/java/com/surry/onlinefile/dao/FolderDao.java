package com.surry.onlinefile.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.surry.onlinefile.entity.po.Folder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FolderDao extends BaseMapper<Folder> {
}
