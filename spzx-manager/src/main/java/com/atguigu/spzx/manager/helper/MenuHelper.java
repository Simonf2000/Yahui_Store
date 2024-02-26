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

    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        List<SysMenu> sysMenuParentList = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList
        ) {
            if (sysMenu.getParentId().intValue() == 0) {
                sysMenuParentList.add(findChildren(sysMenu, sysMenuList));
            }
        }
        return sysMenuParentList;
    }

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
