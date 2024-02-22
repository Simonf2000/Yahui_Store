package com.atguigu.spzx.manager.interceptor;

import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.manager.constant.CacheConstant;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import io.swagger.v3.core.util.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/22/17:52
 * @Description:
 */
@Component
public class LoginAuthInterceptor implements HandlerInterceptor {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /**
     * @Param: [request, response, handler]
     * @return: boolean
     * @Author: simonf
     * @Date: 2024/2/22
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /**
         * @Description: 预检放行
         */
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        /**
         * @Description: 不携带token返回208
         */

        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            responseNoLoginInfo(response);
            return false;
        }

        /**
         * @Description: Redis里没有用户信息返回208
         */
        String s = redisTemplate.opsForValue().get(CacheConstant.USER_LOGIN_PREFIX + token);
        if (!StringUtils.hasText(s)) {
            responseNoLoginInfo(response);
            return false;
        }

        /**
         * @Description: ThreadLocal绑定
         */
        SysUser sysUser = JSON.parseObject(s, SysUser.class);
        AuthContextUtil.set(sysUser);

        /**
         * @Description: 刷新登录有效时间
         */
        redisTemplate.expire(CacheConstant.USER_LOGIN_PREFIX + token, 30, TimeUnit.MINUTES);

        //放行
        return true;
    }

    private static void responseNoLoginInfo(HttpServletResponse response) throws IOException {
        Result result = Result.build(null, ResultCodeEnum.LOGIN_AUTH);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        //输出流给前端返回数据
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print(JSON.toJSONString(result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthContextUtil.remove();
    }
}
