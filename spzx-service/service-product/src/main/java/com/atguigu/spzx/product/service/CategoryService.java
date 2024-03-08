package com.atguigu.spzx.product.service;

import com.atguigu.spzx.model.entity.product.Category;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/08/20:31
 * @Description:
 */
public interface CategoryService {
    public List<Category> findOneCategory();

    List<Category> findCategoryTree();
}
