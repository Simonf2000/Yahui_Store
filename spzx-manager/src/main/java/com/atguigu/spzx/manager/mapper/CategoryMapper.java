package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.product.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> findCategoryByParentId(Long id);

    int countCategoryByParentId(Long id);

    List<Category> selectAll();
}
