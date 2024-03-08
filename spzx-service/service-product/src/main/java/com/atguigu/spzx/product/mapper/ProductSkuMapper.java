package com.atguigu.spzx.product.mapper;

import com.atguigu.spzx.model.entity.product.ProductSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/08/20:43
 * @Description:
 */
@Mapper
public interface ProductSkuMapper {
    List<ProductSku> findProductSkuBySale();
}
