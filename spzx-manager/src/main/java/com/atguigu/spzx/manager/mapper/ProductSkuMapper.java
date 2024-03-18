package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.product.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<ProductSku> selectByProductId(Long id);

    void updateById(ProductSku productSku);

    void deleteByProductId(Long id);

    void updateStatusByProductId(@Param("productId") Long id,@Param("status") Integer status);

    List<Long> findSkuIdListByProductId(Long id);

    List<Long> findSkuIdAll();
}
