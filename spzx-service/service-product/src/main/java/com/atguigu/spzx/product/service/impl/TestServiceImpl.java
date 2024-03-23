package com.atguigu.spzx.product.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/23/10:30
 * @Description:
 */
import com.atguigu.spzx.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Override
    public void testLock() {

        // 加锁
        synchronized (this) {
            //1.从Redis缓存中获取key="num"的值
            String value = redisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(value)) {
                redisTemplate.opsForValue().set("num" , "1");
            }

            //2.对获取到值进行+1操作
            int num = Integer.parseInt(value);
            redisTemplate.opsForValue().set("num", String.valueOf(++num));
        }

    }
}

