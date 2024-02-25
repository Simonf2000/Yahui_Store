package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.dto.system.SysUserDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/20/18:38
 * @Description:
 */
@Mapper
public interface SysUserMapper {
    SysUser selectByUserName(String userName);

    List<SysUser> findPage(@Param("dto") SysUserDto sysUserDto);

    void insert(SysUser sysUser);

    SysUser getById(Long id);

    void update(SysUser sysUser);

    void deleteById(Long id);
}
