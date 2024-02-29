package com.atguigu.spzx.manager.helper;

import com.atguigu.spzx.model.entity.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/25/20:52
 * @Description:
 */
public class MenuHelper {

    /**
    * @Description: 将菜单信息封装返回
    * @Param: [sysMenuList]
    * @return: java.util.List<com.atguigu.spzx.model.entity.system.SysMenu>
    * @Author: simonf
    */
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        List<SysMenu> sysMenuParentList = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList
        ) {
            /**
            * @Description: 寻找递归入口(ParentId为1的菜单)
            */
            if (sysMenu.getParentId().intValue() == 0) {
                sysMenuParentList.add(findChildren(sysMenu, sysMenuList));
            }
        }
        return sysMenuParentList;
    }

    /**
    * @Description: 如果一个菜单的id是另一个菜单的ParentId，则另一个菜单就是他的孩子
    * @Param: [sysMenuParent, sysMenuList]
    * @return: com.atguigu.spzx.model.entity.system.SysMenu
    * @Author: simonf
    */
    private static SysMenu findChildren(SysMenu sysMenuParent, List<SysMenu> sysMenuList) {
        sysMenuParent.setChildren(new ArrayList<>());
        for (SysMenu menu : sysMenuList
        ) {
            if (menu.getParentId().intValue() == sysMenuParent.getId()) {
                sysMenuParent.getChildren().add(findChildren(menu,sysMenuList));
            }
        }
        return sysMenuParent;
    }
}
