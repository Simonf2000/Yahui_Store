package com.atguigu.spzx.feign.cart;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/21:24
 * @Description:
 */
import com.atguigu.spzx.model.entity.h5.CartInfo;
import com.atguigu.spzx.model.vo.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "service-cart")
public interface CartFeignClient {

    @GetMapping(value = "/api/order/cart/auth/getAllCkecked")
    public List<CartInfo> getAllCkecked();


    @GetMapping(value = "/api/order/cart/auth/deleteChecked")
    public Result deleteChecked();
}