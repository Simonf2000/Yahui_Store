package com.atguigu.spzx.common.interceptor;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/15:32
 * @Description:
 */
import com.alibaba.fastjson2.JSON;
import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.model.entity.user.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * 作用：
 *      拦截路径中带auth的请求。获取redis中登录的用户信息，把用户信息绑定到线程上，共享数据。
 */
@Component
@Slf4j
public class UserLoginAuthInterceptor implements HandlerInterceptor {

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    //OpenFeign远程调用时会丢失请求头。
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token"); //令牌一定能够获取到。因为：配置拦截器中，只拦截   /api/**/auth/**
        String userInfoJsonStr = redisTemplate.opsForValue().get(RedisConst.USER_LOGIN + token);
        AuthContextUtil.setUserInfo(JSON.parseObject(userInfoJsonStr, UserInfo.class));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthContextUtil.removeUserInfo();
    }
}