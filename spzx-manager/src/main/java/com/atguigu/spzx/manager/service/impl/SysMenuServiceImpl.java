package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.manager.helper.MenuHelper;
import com.atguigu.spzx.manager.mapper.SysMenuMapper;
import com.atguigu.spzx.manager.service.SysMenuService;
import com.atguigu.spzx.model.entity.system.SysMenu;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.SysMenuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> findNodes() {
        //1.查询所有菜单对象
        List<SysMenu> sysMenuList = sysMenuMapper.selectAll();
        if (CollectionUtils.isEmpty(sysMenuList)) {
            return null;
        }
        List<SysMenu> sysMenuTree = MenuHelper.buildTree(sysMenuList);
        return sysMenuTree;
    }

    @Override
    public void save(SysMenu sysMenu) {
        sysMenuMapper.insert(sysMenu);
    }

    @Override
    public void updateById(SysMenu sysMenu) {
        sysMenuMapper.updateById(sysMenu);
    }

    @Override
    public void removeById(Long id) {
        //删除菜单时，如果菜单是父菜单，有子菜单情况下，不允许删除。没有子菜单的菜单可以删除。
        int count = sysMenuMapper.countByParentId(id);
        //根据外键值，进行count统计。如果值大于0，说明这个菜单有孩子，不能删除
        if (count > 0) {
            throw new GuiguException(ResultCodeEnum.NODE_ERROR);
        }
        sysMenuMapper.removeById(id);
    }

    @Override
    public List<SysMenuVo> findUserMenuList() {
        //1.从线程上获取用户信息

        //2.根据用户id查询他所拥有的菜单权限    user -> role -> menu  关联查询获取数据

        //3.数据类型转换

        // 3.2 将菜单列表转换为父子关系数据 List<SysMenu>(放父和子)=>>  List<SysMenu> (只放父)

        // 3.2 List<SysMenu>  =>>  List<SysMenuVo>

        //返回数据结果
        return null;
    }

    // 将List<SysMenu>对象转换成List<SysMenuVo>对象
    private List<SysMenuVo> buildMenus(List<SysMenu> menus) {

        return null;
    }
}