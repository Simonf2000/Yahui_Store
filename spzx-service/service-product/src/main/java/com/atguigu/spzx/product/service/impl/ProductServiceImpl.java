package com.atguigu.spzx.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.spzx.model.dto.h5.ProductSkuDto;
import com.atguigu.spzx.model.entity.product.Product;
import com.atguigu.spzx.model.entity.product.ProductDetails;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.vo.h5.ProductItemVo;
import com.atguigu.spzx.product.mapper.ProductDetailsMapper;
import com.atguigu.spzx.product.mapper.ProductMapper;
import com.atguigu.spzx.product.mapper.ProductSkuMapper;
import com.atguigu.spzx.product.service.ProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/09/18:06
 * @Description:
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductMapper productMapper;

    @Autowired
    ProductSkuMapper productSkuMapper;

    @Autowired
    ProductDetailsMapper productDetailsMapper;

    @Override
    public PageInfo<ProductSku> findByPage(Integer page, Integer limit, ProductSkuDto productSkuDto) {
        PageHelper.startPage(page, limit);
        List<ProductSku> productSkuList = productMapper.findPage(productSkuDto);
        return new PageInfo<ProductSku>(productSkuList);
    }

    @Override
    public ProductItemVo item(Long skuId) {
        ProductItemVo productItemVo = new ProductItemVo();

        ProductSku productSku = productSkuMapper.getById(skuId);
        Long productId = productSku.getProductId();
        Product product = productMapper.getById(productId);

        String sliderUrls = product.getSliderUrls();
        String[] sliderUrlsArray = sliderUrls.split(",");
        List<String> sliderUrlList = Arrays.asList(sliderUrlsArray);

        ProductDetails productDetails = productDetailsMapper.getByProductId(productId);
        String imageUrls = productDetails.getImageUrls();
        String[] imageUrlsArray = imageUrls.split(",");
        List<String> detailsImageUrlList = Arrays.asList(imageUrlsArray);

        String specValue = product.getSpecValue();
        JSONArray sepcValueList = JSON.parseArray(specValue);

        List<ProductSku> productSkuList = productSkuMapper.findByProductId(productId);
        Map<String, Object> skuSpecValueMap = new HashMap<>();
        for (ProductSku sku :
                productSkuList) {
            skuSpecValueMap.put(sku.getSkuSpec(), sku.getId());
        }

        productItemVo.setProductSku(productSku);
        productItemVo.setProduct(product);
        productItemVo.setSliderUrlList(sliderUrlList);
        productItemVo.setDetailsImageUrlList(detailsImageUrlList);
        productItemVo.setSpecValueList(sepcValueList);
        productItemVo.setSkuSpecValueMap(skuSpecValueMap);

        return productItemVo;
    }
}
