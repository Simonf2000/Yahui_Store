package com.atguigu.spzx.pay;

import com.atguigu.spzx.common.anno.EnableUserWebMvcConfiguration;
import com.atguigu.spzx.feign.product.ProductFeignClientFallback;
import com.atguigu.spzx.pay.properties.AlipayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/17:59
 * @Description:
 */
@SpringBootApplication
@EnableUserWebMvcConfiguration
@EnableFeignClients(basePackages = {
        "com.atguigu.spzx.feign.order",
        "com.atguigu.spzx.feign.product"
})
@EnableConfigurationProperties(value = {
        AlipayProperties.class
})
@Import(ProductFeignClientFallback.class)
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class , args) ;
    }

}