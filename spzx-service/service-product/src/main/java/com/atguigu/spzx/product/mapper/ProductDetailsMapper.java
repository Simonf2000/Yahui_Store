package com.atguigu.spzx.product.mapper;

import com.atguigu.spzx.model.entity.product.ProductDetails;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/18/11:28
 * @Description:
 */
@Mapper
public interface ProductDetailsMapper {
    ProductDetails getByProductId(Long productId);
}
