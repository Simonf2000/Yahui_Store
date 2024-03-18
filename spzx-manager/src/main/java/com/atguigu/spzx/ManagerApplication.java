package com.atguigu.spzx;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/20/18:24
 * @Description:
 */

import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.common.log.annotation.EnableLogAspect;
import com.atguigu.spzx.manager.properties.MinioProperties;
import com.atguigu.spzx.manager.properties.UserAuthProperties;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(value = {UserAuthProperties.class
        , MinioProperties.class})
@EnableScheduling
@EnableLogAspect
@EnableAsync
public class ManagerApplication implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.PRODUCT_BLOOM_FILTER);
        bloomFilter.tryInit(1000000,0.00001); //数据数量，误判率
    }
}