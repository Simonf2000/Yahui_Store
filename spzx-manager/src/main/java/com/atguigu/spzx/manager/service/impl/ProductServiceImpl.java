package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.ProductDetailsMapper;
import com.atguigu.spzx.manager.mapper.ProductMapper;
import com.atguigu.spzx.manager.mapper.ProductSkuMapper;
import com.atguigu.spzx.manager.service.ProductService;
import com.atguigu.spzx.model.dto.product.ProductDto;
import com.atguigu.spzx.model.entity.product.Product;
import com.atguigu.spzx.model.entity.product.ProductDetails;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductSkuMapper productSkuMapper;

    @Autowired
    ProductDetailsMapper productDetailsMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageInfo<Product> findByPage(Integer page, Integer limit, ProductDto productDto) {
        PageHelper.startPage(page, limit);
        List<Product> productList = productMapper.findByPage(productDto);
        return new PageInfo<>(productList);
    }

    @Override
    public void save(Product product) {
        product.setStatus(0);
        product.setAuditStatus(0);
        product.setAuditMessage("");
        productMapper.insert(product);

        Long productId = product.getId();

        List<ProductSku> productSkuList = product.getProductSkuList();

        for (int i = 0, size = productSkuList.size(); i < size; i++) {
            ProductSku productSku = productSkuList.get(i);
            productSku.setSkuCode(productId + "_" + i);
            productSku.setSkuName(product.getName() + " " + productSku.getSkuSpec());
            productSku.setProductId(productId);
            productSku.setSaleNum(0);
            productSku.setStatus(0);
            productSkuMapper.insert(productSku);
        }

        String detailsImageUrls = product.getDetailsImageUrls();
        ProductDetails productDetails = new ProductDetails();
        productDetails.setImageUrls(detailsImageUrls);
        productDetails.setProductId(productId);
        productDetailsMapper.insert(productDetails);
    }
}