package com.atguigu.spzx.user.service;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/10:46
 * @Description:
 */
public interface SmsService {
    void sendValidateCode(String phone);
}
