package com.atguigu.spzx.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.spzx.common.anno.GuiGuCache;
import com.atguigu.spzx.model.dto.h5.ProductSkuDto;
import com.atguigu.spzx.model.dto.product.SkuSaleDto;
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
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    //@Autowired
    //StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Autowired
    RedissonClient redissonClient;


    @Override
    public ProductSku getBySkuId(Long skuId) {
        return productSkuMapper.getById(skuId);
    }

    @Override
    public PageInfo<ProductSku> findByPage(Integer page, Integer limit, ProductSkuDto productSkuuDto) {
        PageHelper.startPage(page,limit);
        List<ProductSku> productSkuList =  productMapper.findPage(productSkuuDto);
        return new PageInfo<ProductSku>(productSkuList);
    }

    @GuiGuCache(
            cacheKey = RedisConst.SKUKEY_PREFIX + "#{#params[0]}" + RedisConst.SKUKEY_SUFFIX,
            lockName = RedisConst.PRODUCT_LOCK_SUFFIX + "#{#params[0]}",
            enableLock = true
    )
    @Override
    public ProductItemVo item(Long skuId) {
        //只保留查询数据库的代码，其余代码通过切面类环绕通知进行功能扩展。
        return getFromDb(skuId);
    }




    /*@Override
    public ProductItemVo item(Long skuId) {

        //*****BloomFilter过滤器判断 start*************************************************
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.PRODUCT_BLOOM_FILTER);
        if(!bloomFilter.contains(skuId)){
            log.info("布隆过滤器中没有这个数据:skuId="+skuId);
            return new ProductItemVo();
        }

        //******BloomFilter过滤器判断 end************************************************

        //1.先找redis缓存，有直接返回
        ProductItemVo vo = null;
        String productItemVoJsonStr = redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX);
        if (StringUtils.hasText(productItemVoJsonStr)) {
            vo = JSON.parseObject(productItemVoJsonStr, ProductItemVo.class);
            log.info("商品详情数据来自-redis:"+vo);
            return vo;
        }

        String key = RedisConst.PRODUCT_LOCK_SUFFIX+skuId;
        RLock lock = redissonClient.getLock(key);
        boolean tryLock = lock.tryLock();
        if(tryLock){
            try {
                //2.没有则查询数据库，然后，放在缓存，下次利用缓存提高效率。
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock(); //释放锁
            }
        }else{
            item(skuId); //自旋
        }

        return new ProductItemVo();
    }*/



    /**
     *  1.数据库中没有skuId的数据时，往缓存中存储"null",没有意义。
     *          解决办法：数据库中没有数据时，存储空javabean对象（new ProductItemVo();）
     */
    /*@Override
    public ProductItemVo item(Long skuId) {

        //*****BloomFilter过滤器判断 start*************************************************
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.PRODUCT_BLOOM_FILTER);
        if(!bloomFilter.contains(skuId)){
            log.info("布隆过滤器中没有这个数据:skuId="+skuId);
            return new ProductItemVo();
        }

        //******BloomFilter过滤器判断 end************************************************

        //1.先找redis缓存，有直接返回
        ProductItemVo vo = null;
        String productItemVoJsonStr = redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX);
        if (StringUtils.hasText(productItemVoJsonStr)) {
            vo = JSON.parseObject(productItemVoJsonStr, ProductItemVo.class);
            log.info("商品详情数据来自-redis:"+vo);
            return vo;
        }

        //2.没有则查询数据库，然后，放在缓存，下次利用缓存提高效率。
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
    }*/

    //@NotNull //方法不能返回null
    private ProductItemVo getFromDb(Long skuId) {
        try {
            ProductItemVo productItemVo = new ProductItemVo();
            //1.根据skuId查询ProductSku对象
            ProductSku productSku = productSkuMapper.getById(skuId);
            Long productId = productSku.getProductId();
            //2.根据ProductSku对象productId外键作为Product表主键，查询Product对象。
            Product product = productMapper.getById(productId);


            //3.从Product对象中获取sliderUrls属性，对字符串进行split分解就可以得到轮播图片路径集合
            String sliderUrls = product.getSliderUrls();
            String[] sliderUrlsArray = sliderUrls.split(",");
            List<String> sliderUrlList = Arrays.asList(sliderUrlsArray);


            //4.根据ProductSku对象productId外键作为Product表主键，查询ProductDetails对象。
            //获取imageUrls属性进行split分解就可以得到详情图片路径集合
            ProductDetails productDetails = productDetailsMapper.getByProductId(productId);
            String imageUrls = productDetails.getImageUrls();
            String[] imageUrlsArray = imageUrls.split(",");
            List<String> detailsImageUrlList = Arrays.asList(imageUrlsArray);

            //5.从Product对象中获取specValue字符串，转换为json数组
            //字符串 => [{"key":"颜色","valueList":["白色","红色","黑色"]},{"key":"内存","valueList":["8G","18G"]}]
            String specValue = product.getSpecValue();
            JSONArray specValueList = JSON.parseArray(specValue);

            //6.根据productId查询它的所有List<ProductSku>,遍历处理就可以得到skuSpecValueMap
            List<ProductSku> productSkuList = productSkuMapper.findByProductId(productId);
            Map<String,Object> skuSpecValueMap = new HashMap<>();
            for (ProductSku sku : productSkuList) {
                skuSpecValueMap.put(sku.getSkuSpec(),sku.getId());
            }

            //7.数据封装
            productItemVo.setProductSku(productSku);
            productItemVo.setProduct(product);
            productItemVo.setSliderUrlList(sliderUrlList);
            productItemVo.setDetailsImageUrlList(detailsImageUrlList);
            productItemVo.setSpecValueList(specValueList);
            productItemVo.setSkuSpecValueMap(skuSpecValueMap);

            return productItemVo;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean updateSkuSaleNum(List<SkuSaleDto> skuSaleDtoList) {
        if(!CollectionUtils.isEmpty(skuSaleDtoList)) {
            for(SkuSaleDto skuSaleDto : skuSaleDtoList) {
                productSkuMapper.updateSale(skuSaleDto.getSkuId(), skuSaleDto.getNum());
            }
        }
        return true;
    }
}
