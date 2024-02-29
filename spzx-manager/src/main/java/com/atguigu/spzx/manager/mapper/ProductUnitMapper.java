package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.base.ProductUnit;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/29/18:00
 * @Description:
 */
@Mapper
public interface ProductUnitMapper {
    List<ProductUnit> findAll();
}
