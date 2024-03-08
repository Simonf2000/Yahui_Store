package com.atguigu.spzx.product.service.impl;

import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.product.mapper.CategoryMapper;
import com.atguigu.spzx.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/08/20:32
 * @Description:
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> findOneCategory() {
        return categoryMapper.findOneCategory();
    }

    @Override
    public List<Category> findCategoryTree() {
        //所有分类列表：703条
        List<Category> categoryList =  categoryMapper.findAll();

        //通过循环进行父子关系组合

        //1.查询所有一级分类
        List<Category> oneCategoryList = categoryList.stream().filter(category -> category.getParentId() == 0).collect(Collectors.toList());

        //2.找每个一级分类对象对应的二级分类对象，然后存储在chilren集合,组装一级和二级分类的父子关系
        oneCategoryList.forEach(oneCategory -> {
            List<Category> twoCategoryList  = categoryList.stream()
                    .filter(category -> category.getParentId() == oneCategory.getId()).collect(Collectors.toList());
            oneCategory.setChildren(twoCategoryList);

            //2.找每个二级分类对象对应的三级分类对象，然后存储在chilren集合,组装二级和三级分类的父子关系
            twoCategoryList.forEach(twoCategory->{
                List<Category> threeCategoryList  = categoryList.stream()
                        .filter(category -> category.getParentId() == twoCategory.getId()).collect(Collectors.toList());
                twoCategory.setChildren(threeCategoryList);
            });
        });

        return oneCategoryList;
    }
}