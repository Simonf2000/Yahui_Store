package com.atguigu.spzx.test;

import com.alibaba.excel.EasyExcel;
import com.atguigu.spzx.manager.lisener.ExcelListener;
import com.atguigu.spzx.model.vo.product.CategoryExcelVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/26/18:31
 * @Description:
 */
public class EasyExcelTest {
    public static void main(String[] args) {
        //readExcel();
        writeDataToExcel();
    }

//    private static void readExcel() {
//        String filename = "E:/分类数据.xlsx";
//        ExcelListener<CategoryExcelVo> excelVoExcelListener = new ExcelListener<>();
//        EasyExcel.read(filename, CategoryExcelVo.class, excelVoExcelListener).sheet().doRead();
//
//        List<CategoryExcelVo> categoryExcelVoList = excelVoExcelListener.getDatas();
//        categoryExcelVoList.forEach(categoryExcelVo -> System.out.println("categoryExcelVo = " + categoryExcelVo));
//    }

    public static void writeDataToExcel() {
        List<CategoryExcelVo> list = new ArrayList<>() ;
        list.add(new CategoryExcelVo(1L , "数码办公" , "",0L, 1, 1)) ;
        list.add(new CategoryExcelVo(11L , "华为手机" , "",1L, 1, 2)) ;
        EasyExcel.write("E:/分类数据1.xlsx" , CategoryExcelVo.class).sheet("分类数据1").doWrite(list);
    }
}
