package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.CategoryMapper;
import com.atguigu.spzx.manager.service.CategoryService;
import com.atguigu.spzx.model.entity.product.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> findCategoryByParentId(Long id) {
        List<Category> categoryList = categoryMapper.findCategoryByParentId(id);

        categoryList.forEach(category -> {
            int count = categoryMapper.countCategoryByParentId(category.getId());
            if (count > 0) {
                category.setHasChildren(true);
            } else {
                category.setHasChildren(false);
            }

        });

        return categoryList;
    }
}
