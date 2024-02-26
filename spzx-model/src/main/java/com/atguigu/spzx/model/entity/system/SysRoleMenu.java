package com.atguigu.spzx.model.entity.system;

import com.atguigu.spzx.model.entity.base.BaseEntity;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/26/13:58
 * @Description:
 */
@Data
public class SysRoleMenu extends BaseEntity {
    private Long roleId;
    private Long menuId;
    private Integer isHalf;
}
