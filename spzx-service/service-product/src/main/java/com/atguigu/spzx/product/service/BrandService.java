package com.atguigu.spzx.product.service;

import com.atguigu.spzx.model.entity.product.Brand;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/18/10:06
 * @Description:
 */
public interface BrandService {
    List<Brand> findAll();
}