package com.atguigu.spzx.cart;

import com.atguigu.spzx.common.anno.EnableUserWebMvcConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/16:21
 * @Description:
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)  // 排除数据库的自动化配置，Cart微服务不需要访问数据库
@EnableFeignClients(basePackages = {
        "com.atguigu.spzx.feign.product"
})
@EnableUserWebMvcConfiguration
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class);
    }

}