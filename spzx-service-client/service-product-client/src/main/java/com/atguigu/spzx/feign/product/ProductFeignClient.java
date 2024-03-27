package com.atguigu.spzx.feign.product;

import com.atguigu.spzx.model.dto.product.SkuSaleDto;
import com.atguigu.spzx.model.entity.product.ProductSku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/16:36
 * @Description:
 */
@FeignClient(name="service-product",fallback = ProductFeignClientFallback.class) //指定微服务名称
//@FeignClient(name="service-product") //指定微服务名称
public interface ProductFeignClient {

    @GetMapping("/api/product/getBySkuId/{skuId}")
    public ProductSku getBySkuId(@PathVariable Long skuId); //声明与远程调用的微服务的controller中方法一致。



    /**
     * 更新商品sku销量
     * @param skuSaleDtoList
     * @return
     */
    @PostMapping("/api/product/updateSkuSaleNum")
    Boolean updateSkuSaleNum(@RequestBody List<SkuSaleDto> skuSaleDtoList);
}