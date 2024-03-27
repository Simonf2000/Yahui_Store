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
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductSkuMapper productSkuMapper;

    @Autowired
    ProductDetailsMapper productDetailsMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

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


        //********重置布隆过滤器 start***********************************
        //1.创建一个新的布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.PRODUCT_NEW_BLOOM_FILTER);
        bloomFilter.tryInit(1000000,0.00001); //插入数据量，误判率

        //2.查询所有上架并且没有被软删除的skuId集合，放置到新的布隆过滤器中
        List<Long> skuIdList =  productSkuMapper.findSkuIdAll();
        for (Long skuId : skuIdList) {
            bloomFilter.add(skuId);
        }

        //3.删除旧的布隆过滤器，将新的布隆过滤器名称修改为旧的布隆过滤器的名称。就更新布隆过滤器数据。保证原子性：lua脚本
        System.out.println("skuIdList.toString() = " + skuIdList.toString());
        // 定义lua脚本
        String script = "if redis.call(\"exists\", KEYS[1]) == 1 then\n"+
                "    redis.call(\"del\" , KEYS[1])\n"+
                "end\n"+
                "if redis.call(\"exists\", \"{\"..KEYS[1]..\"}:config\") == 1 then\n"+
                "    redis.call(\"del\" , \"{\"..KEYS[1]..\"}:config\")\n"+
                "end\n"+
                "if redis.call(\"exists\", KEYS[2]) == 1 then\n"+
                "    redis.call(\"rename\" , KEYS[2] , KEYS[1])\n"+
                "end\n"+
                "if redis.call(\"exists\", \"{\"..KEYS[2]..\"}:config\") == 1 then\n"+
                "    redis.call(\"rename\" , \"{\"..KEYS[2]..\"}:config\" , \"{\"..KEYS[1]..\"}:config\")\n"+
                "end\n"+
                "return 1";
        System.out.println("script = " + script);
        // 执行lua脚本
        //Long.class 表示脚本执行结果：1表示成功
        redisTemplate.execute(new DefaultRedisScript<>(script , Long.class) ,
                Arrays.asList(RedisConst.PRODUCT_BLOOM_FILTER , RedisConst.PRODUCT_NEW_BLOOM_FILTER)) ;

        // 打印日志
        log.info("reset bloomFilter成功了............");

        //********重置布隆过滤器 end***********************************
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
        if(status == 1) {
            product.setStatus(1);
        } else {
            product.setStatus(-1);
        }
        productMapper.updateById(product);
        productSkuMapper.updateStatusByProductId(id,status);
        if(status == 1){
            List<Long> skuIdList = productSkuMapper.findSkuIdListByProductId(id);
            for (Long skuId : skuIdList) {
                // 将商品skuId保存到布隆过滤器中
                RBloomFilter<Object> bloomFilter = redissonClient
                        .getBloomFilter(RedisConst.PRODUCT_BLOOM_FILTER);
                bloomFilter.add(skuId);
            }
        }
    }
}
