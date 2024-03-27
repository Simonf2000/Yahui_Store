package com.atguigu.spzx.order.service.impl;

import com.atguigu.spzx.feign.cart.CartFeignClient;
import com.atguigu.spzx.model.entity.h5.CartInfo;
import com.atguigu.spzx.model.entity.order.OrderItem;
import com.atguigu.spzx.model.vo.h5.TradeVo;
import com.atguigu.spzx.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/9:09
 * @Description:
 */
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.feign.cart.CartFeignClient;
import com.atguigu.spzx.feign.product.ProductFeignClient;
import com.atguigu.spzx.feign.user.UserFeignClient;
import com.atguigu.spzx.model.dto.h5.OrderInfoDto;
import com.atguigu.spzx.model.entity.h5.CartInfo;
import com.atguigu.spzx.model.entity.order.OrderInfo;
import com.atguigu.spzx.model.entity.order.OrderItem;
import com.atguigu.spzx.model.entity.order.OrderLog;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.entity.user.UserAddress;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.h5.TradeVo;
import com.atguigu.spzx.order.mapper.OrderInfoMapper;
import com.atguigu.spzx.order.mapper.OrderItemMapper;
import com.atguigu.spzx.order.mapper.OrderLogMapper;
import com.atguigu.spzx.order.service.OrderInfoService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderInfoServiceImpl implements OrderInfoService {

    //保存订单，需要用到哪些调用哪些远程服务

    @Autowired
    ProductFeignClient productFeignClient; //根据skuId查询ProductSku对象，来校验库存

    @Autowired
    UserFeignClient userFeignClient; //根据用户地址id查询用户地址对象，将地址信息保存到订单表中

    @Autowired
    CartFeignClient cartFeignClient; //下单后，需要清理掉购物车中买走的商品

    @Autowired
    OrderInfoMapper orderInfoMapper; //保存订单。主键回填

    @Autowired
    OrderItemMapper orderItemMapper; //保存订单项。 一个订单对应多个订单项。

    @Autowired
    OrderLogMapper orderLogMapper; //保存订单日志。一个订单对应多个日志数据。保存，修改，都会生成日志数据。

    @Override
    public TradeVo getTrade() {
        List<OrderItem> orderItemList = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal(0);

        List<CartInfo> CartInfoCkeckedList = cartFeignClient.getAllCkecked(); //获取当前用户购物车中打钩所有商品List<CartInfo>

        for (CartInfo cartInfo : CartInfoCkeckedList) {
            OrderItem orderItem = new OrderItem();
            //orderItem.setOrderId(null);
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setThumbImg(cartInfo.getImgUrl());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItemList.add(orderItem);
        }

        for (OrderItem orderItem : orderItemList) {
            BigDecimal oneOrderItemTotalPrice = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()));
            totalPrice = totalPrice.add(oneOrderItemTotalPrice);
        }

        TradeVo tradeVo = new TradeVo();
        tradeVo.setOrderItemList(orderItemList);
        tradeVo.setTotalAmount(totalPrice);
        return tradeVo;
    }


    @Override
    public Long submitOrder(OrderInfoDto orderInfoDto) {
        //1.校验库存
        List<OrderItem> orderItemList = orderInfoDto.getOrderItemList();
        if (CollectionUtils.isEmpty(orderItemList)) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        for (OrderItem orderItem : orderItemList) {
            Long skuId = orderItem.getSkuId();
            Integer skuNum = orderItem.getSkuNum(); //购买数量  是否  大于库存
            ProductSku productSku = productFeignClient.getBySkuId(skuId);
            if (productSku == null) {
                throw new GuiguException(ResultCodeEnum.DATA_ERROR);
            }
            if (skuNum > productSku.getStockNum()) {
                throw new GuiguException(ResultCodeEnum.STOCK_LESS);
            }
        }

        //2.获取远程用户地址
        UserAddress userAddress = userFeignClient.getUserAddress(orderInfoDto.getUserAddressId());

        //3.保存订单
        UserInfo userInfo = AuthContextUtil.getUserInfo();

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userInfo.getId());
        orderInfo.setNickName(userInfo.getNickName());
        orderInfo.setOrderNo(String.valueOf(new Date().getTime())); //将来使用雪花算法，保证唯一
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderItem orderItem : orderItemList) {
            totalAmount = totalAmount.add(orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum())));
        }
        orderInfo.setTotalAmount(totalAmount); //实付总金额
        orderInfo.setOriginalTotalAmount(totalAmount); //不设计运费和优惠卷
        orderInfo.setFeightFee(orderInfoDto.getFeightFee());
        //orderInfo.setPayType(2);  //支付方式： 1微信支付   2支付宝支付     待支付页面需要选择类型，支付时更新这个字段。
        orderInfo.setOrderStatus(0); //订单状态【0->待付款；1->待发货；2->已发货；3->待用户收货，已完成；-1->已取消】

        orderInfo.setReceiverName(userAddress.getName());
        orderInfo.setReceiverPhone(userAddress.getPhone());
        orderInfo.setReceiverTagName(userAddress.getTagName());
        orderInfo.setReceiverProvince(userAddress.getProvinceCode());
        orderInfo.setReceiverCity(userAddress.getCityCode());
        orderInfo.setReceiverDistrict(userAddress.getDistrictCode());
        orderInfo.setReceiverAddress(userAddress.getFullAddress());

        orderInfo.setRemark(orderInfoDto.getRemark());

        orderInfo.setCouponAmount(new BigDecimal(0));
        orderInfo.setCouponId(0L);

        orderInfoMapper.save(orderInfo); //主键回填  useGeneratedKeys="true" keyProperty="id"

        Long orderInfoId = orderInfo.getId();

        //4.保存订单项
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(orderInfoId);
            orderItemMapper.save(orderItem);
        }

        //5.保存日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderInfoId);
        orderLog.setOperateUser("用户");
        orderLog.setProcessStatus(0);
        orderLog.setNote("提交订单");
        orderLogMapper.save(orderLog);

        //6.清理购物车
        cartFeignClient.deleteChecked();

        return orderInfoId;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return orderInfoMapper.getById(orderId);
    }

    @Override
    public TradeVo buy(Long skuId) {
        // 查询商品
        ProductSku productSku = productFeignClient.getBySkuId(skuId);

        if (productSku == null) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = new OrderItem();
        orderItem.setSkuId(skuId);
        orderItem.setSkuName(productSku.getSkuName());
        orderItem.setSkuNum(1);
        orderItem.setSkuPrice(productSku.getSalePrice());
        orderItem.setThumbImg(productSku.getThumbImg());
        orderItemList.add(orderItem);

        // 计算总金额
        BigDecimal totalAmount = productSku.getSalePrice();
        TradeVo tradeVo = new TradeVo();
        tradeVo.setTotalAmount(totalAmount);
        tradeVo.setOrderItemList(orderItemList);

        // 返回
        return tradeVo;
    }

    @Override
    public PageInfo<OrderInfo> findUserPage(Integer page,
                                            Integer limit,
                                            Integer orderStatus) {
        PageHelper.startPage(page, limit);
        Long userId = AuthContextUtil.getUserInfo().getId();
        List<OrderInfo> orderInfoList = orderInfoMapper.findUserPage(userId, orderStatus);

        orderInfoList.forEach(orderInfo -> {
            List<OrderItem> orderItem = orderItemMapper.findByOrderId(orderInfo.getId());
            orderInfo.setOrderItemList(orderItem);
        });

        return new PageInfo<>(orderInfoList);
    }

    @Override
    public OrderInfo getByOrderNo(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.getByOrderNo(orderNo);
        List<OrderItem> orderItem = orderItemMapper.findByOrderId(orderInfo.getId());
        orderInfo.setOrderItemList(orderItem);
        return orderInfo;
    }

    @Transactional
    @Override
    public void updateOrderStatus(String orderNo, Integer payType) {

        // 更新订单状态
        OrderInfo orderInfo = orderInfoMapper.getByOrderNo(orderNo);
        orderInfo.setOrderStatus(1);
        orderInfo.setPayType(payType);
        orderInfo.setPaymentTime(new Date());
        orderInfoMapper.updateById(orderInfo);

        // 记录日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderInfo.getId());
        orderLog.setProcessStatus(1);
        orderLog.setNote("支付宝支付成功");
        orderLogMapper.save(orderLog);
    }


}