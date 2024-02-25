package com.atguigu.spzx.manager.service;

import com.atguigu.spzx.model.dto.system.AssginRoleDto;
import com.atguigu.spzx.model.dto.system.LoginDto;
import com.atguigu.spzx.model.dto.system.SysUserDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.system.LoginVo;
import com.github.pagehelper.PageInfo;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/20/18:32
 * @Description:
 */
public interface SysUserService {
    LoginVo login(LoginDto loginDto);

    SysUser getUserInfo(String token);

    void logout(String token);

    PageInfo<SysUser> findPage(SysUserDto sysUserDto, Integer pageNum, Integer pageSize);

    void save(SysUser sysUser);

    SysUser getById(Long id);

    void udpate(SysUser sysUser);

    void deleteById(Long id);

    Map<String, Object> findRoleByUserId(Long id);

    void doAssign(AssginRoleDto assginRoleDto);
}
