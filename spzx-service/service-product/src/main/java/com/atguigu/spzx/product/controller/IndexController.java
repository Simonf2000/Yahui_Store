package com.atguigu.spzx.product.controller;

import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.h5.IndexVo;
import com.atguigu.spzx.product.service.CategoryService;
import com.atguigu.spzx.product.service.ProductSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/08/20:23
 * @Description:
 */
@Tag(name = "首页接口管理")
@RestController
@RequestMapping(value="/api/product/index")
//@CrossOrigin
@SuppressWarnings({"unchecked", "rawtypes"})
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductSkuService productSkuService;

    @Operation(summary = "首页数据加载")
    @GetMapping
    public Result<IndexVo> findData(){
        //查询一级分类
        List<Category> categoryList = categoryService.findOneCategory();
        //查询前20条畅销商品
        List<ProductSku> productSkuList = productSkuService.findProductSkuBySale();

        //封装IndexVo对象
        IndexVo indexVo = new IndexVo() ;
        indexVo.setCategoryList(categoryList);
        indexVo.setProductSkuList(productSkuList);
        return Result.build(indexVo , ResultCodeEnum.SUCCESS);
    }

}