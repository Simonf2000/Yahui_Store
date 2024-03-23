package com.atguigu.spzx.product.controller;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/23/10:29
 * @Description:
 */

import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/product/test")
public class TestController {

    //  注入服务层方法
    @Autowired
    private TestService testService;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 测试分布式锁
     *
     * @return
     */
    @GetMapping(value = "/testLock01")
    public Result testLock01() throws InterruptedException {

        // 加锁
        RLock lock = redissonClient.getLock("redisson-lock");

        // 一直等待获取锁，直到获取到锁为止! 默认锁的存活时间为30s
        // lock.lock();
        // lock.lock(10 , TimeUnit.SECONDS);   // 指定锁的过期时间为10s
        boolean tryLock = lock.tryLock();   // 尝试获取锁，如果可以获取到锁就返回true，否则返回false， 默认情况下锁的过期时间为30s
        //boolean tryLock = lock.tryLock(3, 20, TimeUnit.SECONDS);  // 指定加锁的等待超时时间为3s
        if (tryLock) {
            try {
                // 获取到了锁
                System.out.println("获取到了锁" + Thread.currentThread().getName());
                // 执行业务操作
                System.out.println("执行业务操作");
                TimeUnit.SECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // 释放锁
                lock.unlock();
                System.out.println("释放了锁" + Thread.currentThread().getName());
            }
        } else {
            System.out.println("没有获取到锁---->" + Thread.currentThread().getName());
        }
        // 返回结果
        return Result.build(null, ResultCodeEnum.SUCCESS);

    }
}