package com.atguigu.spzx.product.service;

import com.atguigu.spzx.model.entity.product.ProductSku;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/08/20:31
 * @Description:
 */
public interface ProductSkuService {
     List<ProductSku> findProductSkuBySale();
}
