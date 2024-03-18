package com.atguigu.spzx.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.spzx.model.dto.h5.ProductSkuDto;
import com.atguigu.spzx.model.entity.product.Product;
import com.atguigu.spzx.model.entity.product.ProductDetails;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.vo.h5.ProductItemVo;
import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.product.mapper.ProductDetailsMapper;
import com.atguigu.spzx.product.mapper.ProductMapper;
import com.atguigu.spzx.product.mapper.ProductSkuMapper;
import com.atguigu.spzx.product.service.ProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/09/18:06
 * @Description:
 */
@Service
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductMapper productMapper;

    @Autowired
    ProductSkuMapper productSkuMapper;

    @Autowired
    ProductDetailsMapper productDetailsMapper;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Override
    public PageInfo<ProductSku> findByPage(Integer page, Integer limit, ProductSkuDto productSkuDto) {
        PageHelper.startPage(page, limit);
        List<ProductSku> productSkuList = productMapper.findPage(productSkuDto);
        return new PageInfo<ProductSku>(productSkuList);
    }

    @Override
    public ProductItemVo item(Long skuId) {
        ProductItemVo vo = null;
        String productItemVoJsonStr = redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX);
        if (StringUtils.hasText(productItemVoJsonStr)) {
            vo = JSON.parseObject(productItemVoJsonStr, ProductItemVo.class);
            log.info("商品详情数据来自-redis:" + vo);
            return vo;
        }
        ProductItemVo productItemVoFromDb = getFromDb(skuId);
        if(productItemVoFromDb==null){
            productItemVoFromDb = new ProductItemVo();
            redisTemplate.opsForValue()
                    .set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX,JSON.toJSONString(productItemVoFromDb),
                            RedisConst.SKUKEY_EMPTY_TIMEOUT, TimeUnit.SECONDS); //空值只缓存30秒
            log.info("商品详情数据来自-mysql,但是为null,存储默认值到Redis:"+productItemVoFromDb);
        }else{
            redisTemplate.opsForValue()
                    .set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX,JSON.toJSONString(productItemVoFromDb),
                            RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS); //非空值只缓存24*60*60秒
            log.info("商品详情数据来自-mysql:"+productItemVoFromDb);
        }
        return productItemVoFromDb;
    }

   // @NotNull
    private ProductItemVo getFromDb(Long skuId) {
        try {
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
        } catch (Exception e) {
            return null;
        }
    }
}
