package com.atguigu.spzx.manager.service;

import com.atguigu.spzx.model.dto.system.SysRoleDto;
import com.atguigu.spzx.model.entity.system.SysRole;
import com.github.pagehelper.PageInfo;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/22/20:22
 * @Description:
 */
public interface SysRoleService {
    PageInfo<SysRole> findByPage(SysRoleDto sysRoleDto, Integer pageNum, Integer pageSize);
}
