package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.manager.service.SysUserService;
import com.atguigu.spzx.model.dto.system.SysUserDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/25/10:15
 * @Description:
 */
@Tag(name = "用户管理模块")
@RestController
@RequestMapping(value = "/admin/system/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService ;

    @Operation(summary = "分页")
    @PostMapping(value = "/findPage/{pageNum}/{pageSize}")
    public Result<PageInfo<SysUser>> findByPage(@RequestBody SysUserDto sysUserDto ,
                                                @PathVariable(value = "pageNum") Integer pageNum ,
                                                @PathVariable(value = "pageSize") Integer pageSize) {
        PageInfo<SysUser> pageInfo = sysUserService.findPage(sysUserDto,pageNum,pageSize);
        return Result.build(pageInfo , ResultCodeEnum.SUCCESS) ;
    }

    @Operation(summary = "保存")
    @PostMapping("/save")
    public Result Save(@RequestBody SysUser sysUser) {
        sysUserService.save(sysUser);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "根据ID查询角色")
    @GetMapping("/getById/{id}")
    public Result getById(@PathVariable("id") Long id) {
        SysUser sysUser = sysUserService.getById(id);
        return Result.build(sysUser, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "修改")
    @PutMapping("/update")
    public Result update(@RequestBody SysUser sysUser) {
        sysUserService.udpate(sysUser);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "根据id删除角色")
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        sysUserService.deleteById(id);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "根据用户id查询用户角色")
    @GetMapping("/findRoleByUserId/{id}")
    public Result<Map<String,Object>> findRoleByUserId(@PathVariable("id") Long id) {
        Map<String,Object> data = sysUserService.findRoleByUserId(id);
        return Result.build(data, ResultCodeEnum.SUCCESS);
    }


}
