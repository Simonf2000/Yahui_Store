package com.atguigu.spzx.manager.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper {
    List<Long> findMenuIdsByRoleId(Long id);

//    void insert(SysRoleMenu sysRoleMenu);

    void deleteRoleMenuRelationship(Long roleId);
}
