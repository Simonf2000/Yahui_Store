package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.manager.service.SysRoleService;
import com.atguigu.spzx.model.dto.system.SysRoleDto;
import com.atguigu.spzx.model.entity.system.SysRole;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/22/20:20
 * @Description:
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping(value = "/admin/system/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Operation(summary = "分页")
    @PostMapping("/findPage/{pageNum}/{pageSize}")
    public Result<PageInfo<SysRole>> findPage(@RequestBody SysRoleDto sysRoleDto,
                                              @PathVariable(value = "pageNum") Integer pageNum,
                                              @PathVariable(value = "pageSize") Integer pageSize) {
        PageInfo<SysRole> pageInfo = sysRoleService.findByPage(sysRoleDto, pageNum, pageSize);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "保存")
    @PostMapping("/save")
    public Result Save(@RequestBody SysRole sysRole) {
        sysRoleService.save(sysRole);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "根据ID查询角色")
    @GetMapping("/getById/{id}")
    public Result getById(@PathVariable("id") Long id) {
        SysRole sysRole = sysRoleService.getById(id);
        return Result.build(sysRole, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "修改")
    @PutMapping("/update")
    public Result update(@RequestBody SysRole sysRole) {
        sysRoleService.udpate(sysRole);
        return Result.build(sysRole, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "根据id删除角色")
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        sysRoleService.deleteById(id);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }



}