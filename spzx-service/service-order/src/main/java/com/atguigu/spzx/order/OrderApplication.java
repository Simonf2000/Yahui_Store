package com.atguigu.spzx.order;

import com.atguigu.spzx.common.anno.EnableUserTokenFeignInterceptor;
import com.atguigu.spzx.common.anno.EnableUserWebMvcConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.atguigu.spzx.order","com.atguigu.spzx.feign.product"})
@EnableFeignClients(basePackages = {
        "com.atguigu.spzx.feign.cart",
        "com.atguigu.spzx.feign.user",
        "com.atguigu.spzx.feign.product"
})
@EnableUserWebMvcConfiguration
@EnableUserTokenFeignInterceptor //负责发请求前往请求头上挂token
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }
}
