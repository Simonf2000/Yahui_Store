package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.manager.mapper.ProductDetailsMapper;
import com.atguigu.spzx.manager.mapper.ProductMapper;
import com.atguigu.spzx.manager.mapper.ProductSkuMapper;
import com.atguigu.spzx.manager.mapper.ProductSpecMapper;
import com.atguigu.spzx.manager.service.ProductSpecService;
import com.atguigu.spzx.model.entity.product.ProductSpec;
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
public class ProductSpecServiceImpl implements ProductSpecService {

    @Autowired
    private ProductSpecMapper productSpecMapper ;

    @Autowired
    private ProductMapper productMapper ;

    @Autowired
    ProductSkuMapper productSkuMapper;

    @Autowired
    ProductDetailsMapper productDetailsMapper;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Override
    public PageInfo<ProductSpec> findByPage(Integer page, Integer limit) {
        PageHelper.startPage(page , limit) ;
        List<ProductSpec> productSpecList = productSpecMapper.findByPage() ;
        return new PageInfo<>(productSpecList);
    }

    @Override
    public void save(ProductSpec productSpec) {
        productSpecMapper.save(productSpec);
    }

    @Override
    public void updateById(ProductSpec productSpec) {
        productSpecMapper.updateById(productSpec);
    }

    @Override
    public void deleteById(Long id) {
        //1.软删除商品信息
        productMapper.deleteById(id);

        //2.软删除sku信息
        productSkuMapper.deleteByProductId(id);

        //3.软删除详情图片信息
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
    public List<ProductSpec> findAll() {
        return productSpecMapper.findAll();
    }
}
