package com.atguigu.spzx.product;

import com.atguigu.spzx.common.aspect.GuiguCacheAspect;
import com.atguigu.spzx.common.config.Knife4jConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/08/17:40
 * @Description:
 */
@SpringBootApplication
@Import(value = {
        Knife4jConfig.class, GuiguCacheAspect.class
})
@EnableCaching //开启缓存功能
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class);
    }
}
