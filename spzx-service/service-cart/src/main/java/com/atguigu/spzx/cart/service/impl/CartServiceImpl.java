package com.atguigu.spzx.cart.service.impl;

import com.alibaba.fastjson2.JSON;
import com.atguigu.spzx.cart.service.CartService;
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.feign.product.ProductFeignClient;
import com.atguigu.spzx.model.entity.h5.CartInfo;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.entity.user.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通过Redis的Hash类型数据结构实现购物车。
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    ProductFeignClient productFeignClient;

    private String getCartKey(Long userId) {
        //定义key user:cart:userId
        return "user:cart:" + userId;
    }

    @Override
    public void addToCart(Long skuId, Integer skuNum) {
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        String cartKey = getCartKey(userInfo.getId());
        CartInfo cartInfo = null;
        String cartInfoJsonStr = (String) redisTemplate.opsForHash().get(cartKey, Long.toString(skuId));
        if (StringUtils.hasText(cartInfoJsonStr)) { //之前添加过这个商品
            cartInfo = JSON.parseObject(cartInfoJsonStr, CartInfo.class);
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            cartInfo.setIsChecked(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        } else { //首次添加这个商品
            ProductSku productSku = productFeignClient.getBySkuId(skuId);

            cartInfo = new CartInfo();
            cartInfo.setUserId(userInfo.getId());
            cartInfo.setSkuId(productSku.getId());
            cartInfo.setCartPrice(productSku.getSalePrice());
            cartInfo.setSkuName(productSku.getSkuName());
            cartInfo.setImgUrl(productSku.getThumbImg());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setIsChecked(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }
        redisTemplate.opsForHash().put(cartKey, String.valueOf(skuId), JSON.toJSONString(cartInfo));
    }


    @Override
    public List<CartInfo> getCartList() {
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        String cartKey = getCartKey(userInfo.getId());
        List<Object> values = redisTemplate.opsForHash().values(cartKey);
        if (!CollectionUtils.isEmpty(values)) {
            return values.stream().map(cartInfoJsonObj -> JSON.parseObject(cartInfoJsonObj.toString(), CartInfo.class))
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void deleteCart(Long skuId) {
        // 获取当前登录的用户数据
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = getCartKey(userId);

        //获取缓存对象
        redisTemplate.opsForHash().delete(cartKey, String.valueOf(skuId));
    }

    @Override
    public void checkCart(Long skuId, Integer isChecked) {
        // 获取当前登录的用户数据
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = getCartKey(userId);

        Boolean hasKey = redisTemplate.opsForHash().hasKey(cartKey, String.valueOf(skuId));
        if (hasKey) {
            Object cartInfoJsonObj = redisTemplate.opsForHash().get(cartKey, String.valueOf(skuId));
            CartInfo cartInfo = JSON.parseObject(cartInfoJsonObj.toString(), CartInfo.class);
            cartInfo.setIsChecked(isChecked);
            redisTemplate.opsForHash().put(cartKey, String.valueOf(skuId), JSON.toJSONString(cartInfo));
        }
    }

    @Override
    public void allCheckCart(Integer isChecked) {
        // 获取当前登录的用户数据
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = getCartKey(userId);

        List<Object> values = redisTemplate.opsForHash().values(cartKey);
        if (!CollectionUtils.isEmpty(values)) {
            values.stream().map(cartInfoJsonObj -> {
                CartInfo cartInfo = JSON.parseObject(cartInfoJsonObj.toString(), CartInfo.class);
                cartInfo.setIsChecked(1);
                return cartInfo;
            }).forEach(cartInfo -> {
                redisTemplate.opsForHash().put(cartKey, String.valueOf(cartInfo.getId()), JSON.toJSONString(cartInfo));
            });
        }

    }

    @Override
    public void clearCart() {
        // 获取当前登录的用户数据
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    @Override
    public List<CartInfo> getAllCkecked() {
        // 获取当前登录的用户数据
        Long userId = AuthContextUtil.getUserInfo().getId();
        String cartKey = getCartKey(userId);
        List<Object> cartInfoJsonObjList = redisTemplate.opsForHash().values(cartKey);
        if (!CollectionUtils.isEmpty(cartInfoJsonObjList)) {
            return cartInfoJsonObjList.stream().map(cartInfoJsonObj -> JSON.parseObject(cartInfoJsonObj.toString(), CartInfo.class))
                    .filter(cartInfo -> cartInfo.getIsChecked() == 1).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}
