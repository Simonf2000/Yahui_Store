package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.common.constant.RedisConst;
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
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
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

    @Autowired
    RedissonClient redissonClient;

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

    @Override
    public Product getById(Long id) {
        Product product = productMapper.getById(id);

        List<ProductSku> productSkuList = productSkuMapper.selectByProductId(id);
        product.setProductSkuList(productSkuList);

        ProductDetails productDetails = productDetailsMapper.selectByProductId(id);
        product.setDetailsImageUrls(productDetails.getImageUrls());
        return product;
    }

    @Override
    public void updateById(Product product) {
        productMapper.updateById(product);

        List<ProductSku> productSkuList = product.getProductSkuList();
        for (ProductSku productSku :
                productSkuList) {
            productSkuMapper.updateById(productSku);
        }

        ProductDetails productDetails = productDetailsMapper.selectByProductId(product.getId());
        productDetails.setImageUrls(product.getDetailsImageUrls());
        productDetailsMapper.updateById(productDetails);
    }

    @Override
    public void deleteById(Long id) {
        productMapper.deleteById(id);
        productSkuMapper.deleteByProductId(id);
        productDetailsMapper.deletedByProductId(id);
    }

    @Override
    public void updateAuditStatus(Long id, Integer auditStatus) {
        Product product = new Product();
        product.setId(id);

        if (auditStatus == 1) {
            product.setAuditStatus(1);
            product.setAuditMessage("审批通过");
        } else {
            product.setAuditStatus(-1);
            product.setAuditMessage("审批拒绝");
        }

        productMapper.updateById(product);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);

        if (status == 1) {
            product.setStatus(1);
        } else {
            product.setStatus(-1);
        }

        productMapper.updateById(product);

        //商品上架：修改product和product_sku 这两个表的status=1
        productSkuMapper.updateStatusByProductId(id, status);

        //根据商品id查询所有skuId集合，将id存储到布隆过滤器中
        List<Long> skuIdList = productSkuMapper.findSkuIdListByProductId(id);

        for (Long skuId : skuIdList) {
            RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.PRODUCT_BLOOM_FILTER);
            bloomFilter.add(skuId);
        }
    }
}
