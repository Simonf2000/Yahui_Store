package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.product.ProductSku;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/29/18:42
 * @Description:
 */
@Mapper
public interface ProductSkuMapper {
    void insert(ProductSku productSku);
}
