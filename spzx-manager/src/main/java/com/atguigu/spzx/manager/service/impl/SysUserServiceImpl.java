package com.atguigu.spzx.manager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.manager.constant.CacheConstant;
import com.atguigu.spzx.manager.mapper.SysUserMapper;
import com.atguigu.spzx.manager.service.SysUserService;
import com.atguigu.spzx.model.dto.system.LoginDto;
import com.atguigu.spzx.model.dto.system.SysUserDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.LoginVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/20/18:36
 * @Description:
 */
@Service
@Transactional
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public LoginVo login(LoginDto loginDto) {

        // 校验验证码是否正确
        String captcha = loginDto.getCaptcha();     // 用户输入的验证码
        String codeKey = loginDto.getCodeKey();     // redis中验证码的数据key

        // 从Redis中获取验证码
        String redisCode = redisTemplate.opsForValue().get(CacheConstant.USER_LOGIN_VALIDATECODE_PREFIX + codeKey);
        if (!StringUtils.hasText(redisCode) || !StrUtil.equalsIgnoreCase(redisCode, captcha)) {
            throw new GuiguException(ResultCodeEnum.VALIDATECODE_ERROR);
        }

        // 验证通过删除redis中的验证码
        redisTemplate.delete(CacheConstant.USER_LOGIN_VALIDATECODE_PREFIX + codeKey);

        // 根据用户名查询用户
        SysUser sysUser = sysUserMapper.selectByUserName(loginDto.getUserName());
        if (sysUser == null) {
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        // 验证密码是否正确
        String inputPassword = loginDto.getPassword();//明文密码
        String md5InputPassword = DigestUtils.md5DigestAsHex(inputPassword.getBytes());//数据库密码
        if (!md5InputPassword.equalsIgnoreCase(sysUser.getPassword())) {
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        // 生成令牌，保存数据到Redis中 TODO 临时有效时间
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(CacheConstant.USER_LOGIN_PREFIX + token, JSON.toJSONString(sysUser), 3000, TimeUnit.MINUTES);

        // 构建响应结果对象
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setRefresh_token("");

        // 返回
        return loginVo;
    }

    @Override
    public SysUser getUserInfo(String token) {
//        String sysUserJson =  redisTemplate.opsForValue().get(CacheConstant.USER_LOGIN_PREFIX+token);
//        if (StringUtils.hasText(sysUserJson)) {
//            SysUser sysUser = JSON.parseObject(sysUserJson,SysUser.class);
//
//            sysUser.setPassword(null);
//            return sysUser;
//        }
//
//        return null;
        SysUser sysUser = AuthContextUtil.get();
        sysUser.setPassword(null);
        return sysUser;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(CacheConstant.USER_LOGIN_PREFIX + token);
    }

    @Override
    public PageInfo<SysUser> findPage(SysUserDto sysUserDto, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<SysUser> list = sysUserMapper.findPage(sysUserDto);

        return new PageInfo<SysUser>(list);
    }

    @Override
    public void save(SysUser sysUser) {
        sysUserMapper.insert(sysUser);
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.getById(id);
    }

    @Override
    public void udpate(SysUser sysUser) {
        sysUserMapper.update(sysUser);
    }

    @Override
    public void deleteById(Long id) {
        sysUserMapper.deleteById(id);
    }
}
