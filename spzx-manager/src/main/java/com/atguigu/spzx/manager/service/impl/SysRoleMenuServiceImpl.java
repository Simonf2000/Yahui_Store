package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.SysMenuMapper;
import com.atguigu.spzx.manager.mapper.SysRoleMenuMapper;
import com.atguigu.spzx.manager.service.SysMenuService;
import com.atguigu.spzx.manager.service.SysRoleMenuService;
import com.atguigu.spzx.model.dto.system.AssginMenuDto;
import com.atguigu.spzx.model.entity.system.SysMenu;
import com.atguigu.spzx.model.entity.system.SysRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SysRoleMenuServiceImpl implements SysRoleMenuService {

    @Autowired
    SysRoleMenuMapper sysRoleMenuMapper;

//    @Autowired
//    SysMenuMapper sysMenuMapper;


    @Autowired
    SysMenuService sysMenuService;

    @Override
    public Map<String, Object> findSysRoleMenuByRoleId(Long id) {
        Map<String, Object> map = new HashMap<>();
//        List<SysMenu> sysMenuList = sysMenuMapper.selectAll();
        List<SysMenu> sysMenuList = sysMenuService.findNodes();

        List<Long> roleMenuIds = sysRoleMenuMapper.findMenuIdsByRoleId(id);

        map.put("sysMenuList", sysMenuList);
        map.put("roleMenuIds", roleMenuIds);
        return map;
    }

    @Override
    public void doAssign(AssginMenuDto assginMenuDto) {
        sysRoleMenuMapper.deleteRoleMenuRelationship(assginMenuDto.getRoleId());

        List<Map<String, Number>> menuIdList = assginMenuDto.getMenuIdList();

        if (!CollectionUtils.isEmpty(menuIdList)) {
            for (Map<String, Number> map : menuIdList) {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();

                sysRoleMenu.setRoleId(assginMenuDto.getRoleId());
                sysRoleMenu.setMenuId(map.get("menuId").longValue());
                sysRoleMenu.setIsHalf(map.get("isHalf").intValue());

                sysRoleMenuMapper.insert(sysRoleMenu);
            }
        }
    }
}
