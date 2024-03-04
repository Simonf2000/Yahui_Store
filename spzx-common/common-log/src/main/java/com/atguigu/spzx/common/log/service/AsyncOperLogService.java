package com.atguigu.spzx.common.log.service;

import com.atguigu.spzx.model.entity.system.SysOperLog;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/04/8:57
 * @Description:
 */
public interface AsyncOperLogService {
    void save(SysOperLog sysOperLog);
}
