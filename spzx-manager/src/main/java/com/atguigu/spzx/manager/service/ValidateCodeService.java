package com.atguigu.spzx.manager.service;

import com.atguigu.spzx.model.vo.system.ValidateCodeVo;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/20/19:57
 * @Description:
 */
public interface ValidateCodeService {
    ValidateCodeVo generateValidateCode();
}
