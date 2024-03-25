package com.atguigu.spzx.user.controller;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/14:02
 * @Description:
 */
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.common.util.IpUtil;
import com.atguigu.spzx.model.dto.h5.UserLoginDto;
import com.atguigu.spzx.model.dto.h5.UserRegisterDto;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.h5.UserInfoVo;
import com.atguigu.spzx.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "会员用户接口")
@RestController
@RequestMapping("/api/user/userInfo")
public class UserInfoController {

    @Autowired
    UserInfoService userInfoService;

    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDto userRegisterDto, HttpServletRequest request) throws Exception {
        String ipAddress = IpUtil.getIpAddress(request);
        userInfoService.register(userRegisterDto,ipAddress);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }

    @Operation(summary = "会员登录")
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDto userLoginDto, HttpServletRequest request) {
        String ip = IpUtil.getIpAddress(request);
        String token = userInfoService.login(userLoginDto, ip);
        return Result.build(token, ResultCodeEnum.SUCCESS);
    }

}