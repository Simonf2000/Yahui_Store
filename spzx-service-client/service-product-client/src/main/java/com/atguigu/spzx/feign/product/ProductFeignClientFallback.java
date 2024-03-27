package com.atguigu.spzx.feign.product;

import com.atguigu.spzx.model.dto.product.SkuSaleDto;
import com.atguigu.spzx.model.entity.product.ProductSku;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/27/10:49
 * @Description:
 */
@Slf4j
@Component
public class ProductFeignClientFallback implements ProductFeignClient{
    @Override
    public ProductSku getBySkuId(Long skuId) {
        log.info("远程调用【service-product】服务,【getBySkuId】方法出问题了,降级处理一下");
        return new ProductSku();//兜底数据
    }

    @Override
    public Boolean updateSkuSaleNum(List<SkuSaleDto> skuSaleDtoList) {
        log.info("远程调用【service-product】服务,【updateSkuSaleNum】方法出问题了,降级处理一下");
        return false;
    }
}