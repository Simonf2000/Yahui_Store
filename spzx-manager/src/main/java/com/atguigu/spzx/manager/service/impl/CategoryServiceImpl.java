package com.atguigu.spzx.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.spzx.manager.lisener.ExcelListener;
import com.atguigu.spzx.manager.mapper.CategoryMapper;
import com.atguigu.spzx.manager.service.CategoryService;
import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.model.vo.product.CategoryExcelVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");

            String fileName = URLEncoder.encode("分类数据", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

       List<Category> categoryList = categoryMapper.selectAll();
       List<CategoryExcelVo> categoryExcelVoList = new ArrayList<>();

       categoryList.forEach(category -> {
           CategoryExcelVo categoryExcelVo = new CategoryExcelVo();
           BeanUtils.copyProperties(category,categoryExcelVo,CategoryExcelVo.class);
           categoryExcelVoList.add(categoryExcelVo);
       });
            EasyExcel.write(response.getOutputStream(), CategoryExcelVo.class).sheet("分类数据").doWrite(categoryExcelVoList);

        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    @Override
    public void importData(MultipartFile file) {
        try {
            ExcelListener<CategoryExcelVo> excelListener = new ExcelListener<>(categoryMapper);
            EasyExcel.read(file.getInputStream(),CategoryExcelVo.class,excelListener).sheet().doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
