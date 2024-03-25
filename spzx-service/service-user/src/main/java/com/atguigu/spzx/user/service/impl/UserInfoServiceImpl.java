package com.atguigu.spzx.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.model.dto.h5.UserLoginDto;
import com.atguigu.spzx.model.dto.h5.UserRegisterDto;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.h5.UserInfoVo;
import com.atguigu.spzx.user.mapper.UserInfoMapper;
import com.atguigu.spzx.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Override
    public String login(UserLoginDto userLoginDto, String ip) {
        String username = userLoginDto.getUsername();
        String password = userLoginDto.getPassword();

        //校验参数
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        //判断用户是否存在
        UserInfo userInfo = userInfoMapper.getUserInfoByUserName(username);
        if (null == userInfo) {
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        //校验密码
        String md5InputPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5InputPassword.equals(userInfo.getPassword())) {
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        //校验是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new GuiguException(ResultCodeEnum.ACCOUNT_STOP);
        }

        //更新登录信息
        userInfo.setLastLoginIp(ip);
        Date date = new Date();
        date.setHours(date.getHours() + 8);
        userInfo.setLastLoginTime(date);
        userInfoMapper.updateById(userInfo);

        //生成令牌
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        //保存用户信息到redis缓存
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN + token, JSON.toJSONString(userInfo), 30, TimeUnit.DAYS);
        return token;
    }

    @Override
    public UserInfoVo getCurrentUserInfo(String token) {
        String userInfoJsonStr = redisTemplate.opsForValue().get(RedisConst.USER_LOGIN + token);
        if(!StringUtils.hasText(userInfoJsonStr)){
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        UserInfo userInfo = JSON.parseObject(userInfoJsonStr, UserInfo.class);
        UserInfoVo vo = new UserInfoVo();
        vo.setNickName(userInfo.getNickName());
        vo.setAvatar(userInfo.getAvatar());
        //BeanUtils.copyProperties(userInfo,vo);
        return vo;
    }
}
