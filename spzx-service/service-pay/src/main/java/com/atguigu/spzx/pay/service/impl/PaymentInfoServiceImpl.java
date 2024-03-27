package com.atguigu.spzx.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.feign.order.OrderFeignClient;
import com.atguigu.spzx.feign.product.ProductFeignClient;
import com.atguigu.spzx.model.dto.product.SkuSaleDto;
import com.atguigu.spzx.model.entity.order.OrderInfo;
import com.atguigu.spzx.model.entity.order.OrderItem;
import com.atguigu.spzx.model.entity.pay.PaymentInfo;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.pay.mapper.PaymentInfoMapper;
import com.atguigu.spzx.pay.service.PaymentInfoService;
import kotlin.jvm.internal.SerializedIr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @Override
    public PaymentInfo savePaymentInfo(String orderNo) {
        //先查询PaymentInfo是否存在，存在就不再保存。不存在才会保存。原因：支付时可能取消了，然后又再次支付。避免重复保存支付信息。
        PaymentInfo paymentInfo = paymentInfoMapper.getByOrderNo(orderNo);
        if(paymentInfo == null){
            paymentInfo =  new PaymentInfo();
            Result<OrderInfo> orderInfoByOrderNo = orderFeignClient.getOrderInfoByOrderNo(orderNo);
            OrderInfo orderInfo = orderInfoByOrderNo.getData();

            paymentInfo.setUserId(orderInfo.getUserId()); //不考虑代付功能
            paymentInfo.setOrderNo(orderInfo.getOrderNo());
            paymentInfo.setPayType(2); //暂时写死参数。支付类型：1微信  2支付宝  3其他
            paymentInfo.setOutTradeNo(""); // 微信或支付宝支付的交易流水号。异步回调时，回传获取。
            paymentInfo.setAmount(orderInfo.getTotalAmount()); //实付总金额
            paymentInfo.setPaymentStatus(0); //支付状态： 0 未支付     1 已支付
            List<OrderItem> orderItemList = orderInfo.getOrderItemList();
            String content = "";
            for (OrderItem orderItem : orderItemList) {
                content = orderItem.getSkuName() +" ";
            }
            paymentInfo.setContent(content);

            //paymentInfo.setCallbackTime(null); //异步回调时，更新支付信息时，填这两个字段值。
            //paymentInfo.setCallbackContent(null);
            paymentInfoMapper.save(paymentInfo);
        }
        return paymentInfo;
    }

    //验签后，调用该方法，完成业务逻辑操作
    @Override
    public void updatePaymentStatus(Map<String, String> map, Integer payType) {
        //1.更新支付信息
        // 查询PaymentInfo
        PaymentInfo paymentInfo = paymentInfoMapper.getByOrderNo(map.get("out_trade_no")); //out_trade_no调用时传递给支付宝订单好，回调时又返回了。
        if (paymentInfo.getPaymentStatus() == 1) { //支付状态如果更新过了，多次被异步通知时，也只执行一次更新。
            return;
        }

        //更新支付信息
        paymentInfo.setPaymentStatus(1);
        paymentInfo.setOutTradeNo(map.get("trade_no")); //map.get("trade_no")支付宝返回的交易流水号。注意，不是订单编号。
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(JSON.toJSONString(map));
        paymentInfo.setPayType(payType); //payType =1 微信支付     payType=2 支付宝支付
        paymentInfoMapper.updateById(paymentInfo);

        //2.更新订单状态
        orderFeignClient.updateOrderStatus(map.get("out_trade_no"),2);


        //3.更新库存和销量
        OrderInfo orderInfo = orderFeignClient.getOrderInfoByOrderNo(paymentInfo.getOrderNo()).getData();
        List<SkuSaleDto> skuSaleDtoList = orderInfo.getOrderItemList().stream().map(item -> {
            SkuSaleDto skuSaleDto = new SkuSaleDto();
            skuSaleDto.setSkuId(item.getSkuId());
            skuSaleDto.setNum(item.getSkuNum());
            return skuSaleDto;
        }).collect(Collectors.toList());
        productFeignClient.updateSkuSaleNum(skuSaleDtoList);
        System.out.println("****************skuSaleDtoList = " + skuSaleDtoList);
    }
}
