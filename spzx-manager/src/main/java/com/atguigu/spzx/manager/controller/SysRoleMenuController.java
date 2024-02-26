package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.manager.service.SysRoleMenuService;
import com.atguigu.spzx.model.dto.system.AssginMenuDto;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "角色菜单模块")
@RestController
@RequestMapping(value = "/admin/system/sysRoleMenu")
public class SysRoleMenuController {

    @Autowired
    private SysRoleMenuService sysRoleMenuService ;

    /**
     * 查询两个部分数据：
     *  1.查询所有菜单
     *  2.查询这个角色之前分配过的菜单id集合
     * @param id 角色id
     * @return 角色对应的菜单数据
     */
    @Operation(summary = "根据角色查询菜单数据")
    @GetMapping("/findSysRoleMenuByRoleId/{id}")
    public Result<Map<String,Object>> findSysRoleMenuByRoleId(@PathVariable("id") Long id){
        Map<String,Object> sysRoleMenusMap =  sysRoleMenuService.findSysRoleMenuByRoleId(id);
        return Result.build(sysRoleMenusMap, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "给角色分配菜单")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginMenuDto assginMenuDto) {
        sysRoleMenuService.doAssign(assginMenuDto);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }
}
