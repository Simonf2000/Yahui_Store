package com.atguigu.spzx.cart.controller;

import com.atguigu.spzx.cart.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/16:23
 * @Description:
 */
@Tag(name = "购物车接口")
@RestController
@RequestMapping("/api/order/cart")
public class CartController {

    @Autowired
    private CartService cartService;

}