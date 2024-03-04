package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.common.log.service.AsyncOperLogService;
import com.atguigu.spzx.manager.mapper.SysOperLogMapper;
import com.atguigu.spzx.model.entity.system.SysOperLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/04/9:02
 * @Description:
 */
@Service
@Transactional
public class AsyncOperLogServiceImpl implements AsyncOperLogService {

    @Autowired
    SysOperLogMapper sysOperLogMapper;

    @Async
    @Override
    public void save(SysOperLog sysOperLog) {
sysOperLogMapper.insert(sysOperLog);
    }
}
