package com.atguigu.spzx.manager.lisener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.atguigu.spzx.manager.mapper.CategoryMapper;
import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.model.vo.product.CategoryExcelVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/26/18:25
 * @Description:
 */
public class ExcelListener<T> extends AnalysisEventListener<T> {

    private static final int BATCH_COUNT = 100;

    CategoryMapper categoryMapper;

    List<CategoryExcelVo> categoryExcelVoList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    public  ExcelListener(CategoryMapper categoryMapper){
        this.categoryMapper = categoryMapper;
    }

    @Override
    public void invoke(T data, AnalysisContext analysisContext) {
        categoryExcelVoList.add((CategoryExcelVo)data);
        if (categoryExcelVoList.size() >= BATCH_COUNT) {
            saveData();
            categoryExcelVoList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (categoryExcelVoList.size() >= 0) {
            saveData();
        }
    }

    public void saveData(){
        categoryMapper.saveBatch(categoryExcelVoList);
    }
}
