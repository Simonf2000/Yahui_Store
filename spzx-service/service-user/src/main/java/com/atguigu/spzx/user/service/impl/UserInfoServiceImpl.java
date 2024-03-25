package com.atguigu.spzx.user.service.impl;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.model.dto.h5.UserRegisterDto;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.user.mapper.UserInfoMapper;
import com.atguigu.spzx.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/11:39
 * @Description:
 */
@Service
@Transactional
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    RedisTemplate<String,String> redisTemplate;


    @Override
    public void register(UserRegisterDto userRegisterDto, String ipAddress) throws Exception {
        String nickName = userRegisterDto.getNickName();
        String password = userRegisterDto.getPassword();
        String username = userRegisterDto.getUsername(); //手机号
        String code = userRegisterDto.getCode();
        //1.验证数据是否合法
        if (!StringUtils.hasText(nickName) || !StringUtils.hasText(password) || !StringUtils.hasText(username) || !StringUtils.hasText(code)) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }


        //2.验证验证码是否正确
        String fromRedisCode = redisTemplate.opsForValue().get(RedisConst.PHONE_CODE_PREFIX + username);
        if (!code.equals(fromRedisCode)) {
            throw new GuiguException(ResultCodeEnum.VALIDATECODE_ERROR);
        }

        //3.验证用户名称是否存在，存在不能注册
        UserInfo userInfo = userInfoMapper.getUserInfoByUserName(username);
        if (userInfo != null) {
            throw new GuiguException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }

        //4.加密密码
        userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword(MD5Utils.md5Hex(password.getBytes()));
        userInfo.setNickName(nickName);
        userInfo.setAvatar("http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eoj0hHXhgJNOTSOFsS4uZs8x1ConecaVOB8eIl115xmJZcT4oCicvia7wMEufibKtTLqiaJeanU2Lpg3w/132");
        userInfo.setPhone(username);
        userInfo.setLastLoginIp(ipAddress);
        Date date = new Date();
        date.setHours(date.getHours() + 8);
        userInfo.setLastLoginTime(date);
        userInfo.setStatus(1);

        userInfoMapper.insert(userInfo);
    }

}
