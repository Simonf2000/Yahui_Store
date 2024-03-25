package com.atguigu.spzx.gateway.filter;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/15:13
 * @Description:
 */
import com.alibaba.fastjson.JSONObject;
import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.gateway.config.RedisConfig;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 作用：
 *      拦截路径中带auth的路径，表示必须登录，请求必须携带token,用户信息必须能够从redis中获取。否则，拒绝访问。
 *
 */
//               /**/auth/**
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {


    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("path="+path);
        if(antPathMatcher.match("/**/auth/**",path)){
            String token = exchange.getRequest().getHeaders().get("token").get(0);
            if(StringUtils.hasText(token)){
                String userInfoJsonStr = redisTemplate.opsForValue().get(RedisConst.USER_LOGIN + token);
                if(!StringUtils.hasText(userInfoJsonStr)){
                    //拒绝访问
                    return out(exchange.getResponse(),ResultCodeEnum.LOGIN_AUTH);
                }
            }
        }
        return chain.filter(exchange); //放行：   路径不带auth的不管放行。token有效，用户信息存在，放行。
    }


    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() { // 排序值相同时，过滤器执行顺序： 全局过滤器->默认过滤器->路由过滤器
        return 0;
    }
}