package com.atguigu.spzx.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/9:03
 * @Description:
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {
        "com.atguigu.spzx.feign.cart"
})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class) ;
    }

}