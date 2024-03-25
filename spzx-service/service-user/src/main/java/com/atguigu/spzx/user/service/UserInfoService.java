package com.atguigu.spzx.user.service;

import com.atguigu.spzx.model.dto.h5.UserRegisterDto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/11:39
 * @Description:
 */
public interface UserInfoService {
    void register(UserRegisterDto userRegisterDto,String ipAddress) throws Exception;
}
