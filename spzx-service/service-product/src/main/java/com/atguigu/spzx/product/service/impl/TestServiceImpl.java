package com.atguigu.spzx.product.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/23/10:30
 * @Description:
 */
import com.atguigu.spzx.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    RedisTemplate<String,String> redisTemplate;


    @Autowired
    RedissonClient redissonClient;

    public void testLock() {
        try {
            RLock lock = redissonClient.getLock("lock"); //获取锁对象
            boolean tryLock = lock.tryLock(3, 20, TimeUnit.SECONDS);//加锁  默认30秒自动释放
            if(tryLock){
                try {
                    //1.从Redis缓存中获取key="num"的值
                    String value = redisTemplate.opsForValue().get("num");
                    if (StringUtils.isEmpty(value)) {
                        redisTemplate.opsForValue().set("num" , "1");
                    }

                    //2.对获取到值进行+1操作
                    int num = Integer.parseInt(value);
                    redisTemplate.opsForValue().set("num", String.valueOf(++num));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock(); //释放锁
                }
            }else{
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                testLock();
            }



        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}

